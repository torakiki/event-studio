/* 
 * This file is part of the EventStudio source code
 * Created on 11/nov/2013
 *  Copyright 2020 by Sober Lemur S.a.s di Vacondio Andrea (info@pdfsam.org).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.pdfsam.eventstudio;

import static org.pdfsam.eventstudio.util.RequireUtils.requireNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.pdfsam.eventstudio.Annotations.ReflectiveListenerDescriptor;
import org.pdfsam.eventstudio.exception.BroadcastInterruptionException;
import org.pdfsam.eventstudio.exception.EventStudioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread-safe holder for the listeners
 * 
 * @author Andrea Vacondio
 * 
 */
class Listeners {

	private static final Logger LOG = LoggerFactory.getLogger(Listeners.class);

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Map<Class<?>, TreeSet<ListenerReferenceHolder>> listeners = new HashMap<>();

	<T> void add(Class<T> eventClass, Listener<T> listener, int priority, ReferenceStrength strength) {
		lock.writeLock().lock();
		try {
			TreeSet<ListenerReferenceHolder> set = nullSafeGetListenerHolders(eventClass);
			set.add(new ListenerReferenceHolder(priority, strength.getReference(new DefaultListenerWrapper(listener))));
		} finally {
			lock.writeLock().unlock();
		}
	}

	public Set<Class<?>> addAll(Object bean, List<ReflectiveListenerDescriptor> descriptors) {
		Set<Class<?>> updatedEventClasses = new HashSet<>();
		lock.writeLock().lock();
		try {
			for (ReflectiveListenerDescriptor current : descriptors) {
				Class<?> eventClass = current.getMethod().getParameterTypes()[0];
				TreeSet<ListenerReferenceHolder> set = nullSafeGetListenerHolders(eventClass);
				set.add(new ListenerReferenceHolder(current.getListenerAnnotation().priority(),
						current.getListenerAnnotation().strength()
								.getReference(new ReflectiveListenerWrapper(bean, current.getMethod()))));
				updatedEventClasses.add(eventClass);
			}

		} finally {
			lock.writeLock().unlock();
		}
		return updatedEventClasses;
	}

	private TreeSet<ListenerReferenceHolder> nullSafeGetListenerHolders(Class<?> eventClass) {
		TreeSet<ListenerReferenceHolder> set = listeners.get(eventClass);
		if (set == null) {
			set = new TreeSet<>();
			listeners.put(eventClass, set);
		}
		return set;
	}

	/**
	 * Removes the listener if present. It also removes the listeners set from
	 * the map if the set is empty.
	 * 
	 * @param eventClass
	 * @param listener
	 * @return true if the listener was present and has been removed
	 */
	<T> boolean remove(Class<T> eventClass, Listener<T> listener) {
		lock.readLock().lock();
		TreeSet<ListenerReferenceHolder> set = listeners.get(eventClass);
		if (set != null) {
			lock.readLock().unlock();
			lock.writeLock().lock();
			try {
				DefaultListenerWrapper wrapper = new DefaultListenerWrapper(listener);
				for (ListenerReferenceHolder current : set) {
					if (wrapper.equals(current.getListenerWrapper())) {
						return removeListenerAndSetIfNeeded(eventClass, current, set);
					}
				}
				return false;
			} finally {
				lock.writeLock().unlock();
			}
		}
		lock.readLock().unlock();
		return false;
	}

	/**
	 * Removes the listener if present. It also removes the listeners set from
	 * the map if the set is empty.
	 * 
	 * @param eventClass
	 * @param listener
	 * @return true if the listener was present and has been removed
	 */
	boolean remove(Class<?> eventClass, ListenerReferenceHolder listener) {
		lock.readLock().lock();
		TreeSet<ListenerReferenceHolder> set = listeners.get(eventClass);
		if (set != null) {
			lock.readLock().unlock();
			lock.writeLock().lock();
			try {
				return removeListenerAndSetIfNeeded(eventClass, listener, set);
			} finally {
				lock.writeLock().unlock();
			}
		}
		lock.readLock().unlock();
		return false;
	}

	private boolean removeListenerAndSetIfNeeded(Class<?> eventClass, ListenerReferenceHolder listener,
			TreeSet<ListenerReferenceHolder> set) {
		if (set.remove(listener)) {
			if (set.isEmpty()) {
				listeners.remove(eventClass);
				LOG.trace("Removed empty listeners set for {}", eventClass);
			}
			return true;
		}
		return false;
	}

	/**
	 * @param eventClass
	 * @return A sorted set containing the listeners queue for the given class.
	 */
	List<ListenerReferenceHolder> nullSafeGetListeners(Class<?> eventClass) {
		requireNotNull(eventClass);
		lock.readLock().lock();
		try {
			TreeSet<ListenerReferenceHolder> set = listeners.get(eventClass);
			if (set == null) {
				return Collections.emptyList();
			}
			return new ArrayList<>(set);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Wraps a listener defined either explicitly or picked up by the annotation
	 * processor
	 * 
	 * @author Andrea Vacondio
	 * 
	 */
	interface ListenerWrapper {
		void onEvent(Envelope event);
	}

	/**
	 * Listener wrapper around an explicitly defined {@link Listener}
	 * 
	 * @author Andrea Vacondio
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final class DefaultListenerWrapper implements ListenerWrapper {
		private Listener wrapped;

		private DefaultListenerWrapper(Listener wrapped) {
			this.wrapped = wrapped;
		}

		public void onEvent(Envelope event) {
			wrapped.onEvent(event.getEvent());
			event.notified();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((wrapped == null) ? 0 : wrapped.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || !(o instanceof DefaultListenerWrapper)) {
				return false;
			}
			DefaultListenerWrapper other = (DefaultListenerWrapper) o;
			return wrapped.equals(other.wrapped);
		}
	}

	/**
	 * Reflective invocation of an annotated listener
	 * 
	 * @author Andrea Vacondio
	 * 
	 */
	private static final class ReflectiveListenerWrapper implements ListenerWrapper {
		private Object bean;
		private Method method;

		public ReflectiveListenerWrapper(Object bean, Method method) {
			this.bean = bean;
			this.method = method;
			this.method.setAccessible(true);
		}

		public void onEvent(Envelope event) {
			try {
				method.invoke(bean, event.getEvent());
			} catch (IllegalAccessException e) {
				throw new EventStudioException("Exception invoking reflective method", e);
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof BroadcastInterruptionException) {
					throw (BroadcastInterruptionException) e.getCause();
				}
				throw new EventStudioException("Reflective method invocation exception", e);
			}
			event.notified();
		}
	}

	/**
	 * Holder for a {@link ListenerWrapper}
	 * 
	 * @author Andrea Vacondio
	 * 
	 */
	static class ListenerReferenceHolder implements Comparable<ListenerReferenceHolder> {
		int priority = 0;
		private Entity<? extends ListenerWrapper> reference;

		public ListenerReferenceHolder(int priority, Entity<? extends ListenerWrapper> reference) {
			requireNotNull(reference);
			this.priority = priority;
			this.reference = reference;
		}

		public int compareTo(ListenerReferenceHolder o) {
			if (this.priority < o.priority) {
				return -1;
			}
			if (this.priority > o.priority) {
				return 1;
			}
			// same priority
			int retVal = this.hashCode() - o.hashCode();
			// same hashcode but not equals. This shouldn't happen but according
			// to hascode documentation is not required and since we don't want
			// ListenerReferenceHolder to
			// disappear from the TreeSet we return an arbitrary integer != from
			// 0
			if (retVal == 0 && !this.equals(o)) {
				retVal = -1;
			}
			return retVal;
		}

		public ListenerWrapper getListenerWrapper() {
			return reference.get();
		}
	}
}
