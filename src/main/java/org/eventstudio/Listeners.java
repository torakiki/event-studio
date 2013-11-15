/* 
 * This file is part of the EventStudio source code
 * Created on 11/nov/2013
 * Copyright 2013 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.eventstudio;

import static org.eventstudio.util.RequireUtils.requireNotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private Map<Class<?>, LinkedList<ListenerReferenceHolder>> listeners = new HashMap<Class<?>, LinkedList<ListenerReferenceHolder>>();

    <T> void add(Class<T> eventClass, Listener<T> listener, int priority, ReferenceStrength strength) {
        lock.writeLock().lock();
        try {
            LinkedList<ListenerReferenceHolder> list = listeners.get(eventClass);
            if (list == null) {
                list = new LinkedList<ListenerReferenceHolder>();
                listeners.put(eventClass, list);
            }
            list.add(new ListenerReferenceHolder(priority, strength.getReference(new DefaultListenerWrapper(listener))));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes the listener if present. It also removes the listeners set from the map if the set is empty.
     * 
     * @param eventClass
     * @param listener
     * @return true if the listener was present and has been removed
     */
    <T> boolean remove(Class<T> eventClass, Listener<T> listener) {
        lock.readLock().lock();
        LinkedList<ListenerReferenceHolder> list = listeners.get(eventClass);
        if (list != null) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                DefaultListenerWrapper wrapper = new DefaultListenerWrapper(listener);
                for (ListenerReferenceHolder current : list) {
                    if (wrapper.equals(current.getListenerWrapper())) {
                        return removeListenerAndSetIfNeeded(eventClass, current, list);
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
     * Removes the listener if present. It also removes the listeners set from the map if the set is empty.
     * 
     * @param eventClass
     * @param listener
     * @return true if the listener was present and has been removed
     */
    boolean remove(Class<?> eventClass, ListenerReferenceHolder listener) {
        lock.readLock().lock();
        LinkedList<ListenerReferenceHolder> list = listeners.get(eventClass);
        if (list != null) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                return removeListenerAndSetIfNeeded(eventClass, listener, list);
            } finally {
                lock.writeLock().unlock();
            }
        }
        lock.readLock().unlock();
        return false;
    }

    private boolean removeListenerAndSetIfNeeded(Class<?> eventClass, ListenerReferenceHolder listener,
            LinkedList<ListenerReferenceHolder> list) {
        if (list.remove(listener)) {
            if (list.isEmpty()) {
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
    TreeSet<ListenerReferenceHolder> nullSafeGetListeners(Class<?> eventClass) {
        requireNotNull(eventClass);
        lock.readLock().lock();
        LinkedList<ListenerReferenceHolder> list = listeners.get(eventClass);
        if (list == null) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                list = listeners.get(eventClass);
                if (list == null) {
                    list = new LinkedList<ListenerReferenceHolder>();
                    listeners.put(eventClass, list);
                }
                return new TreeSet<ListenerReferenceHolder>(list);
            } finally {
                lock.writeLock().unlock();
            }
        }
        try {
            return new TreeSet<ListenerReferenceHolder>(list);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Wraps a listener defined either explicitly or picked up by the annotation processor
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
            requireNotNull(wrapped);
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
     * Holder for a {@link ListenerWrapper}
     * 
     * @author Andrea Vacondio
     * 
     */
    static class ListenerReferenceHolder implements Comparable<ListenerReferenceHolder> {
        private int priority = 0;
        private Entity<? extends ListenerWrapper> reference;

        public ListenerReferenceHolder(int priority, Entity<? extends ListenerWrapper> reference) {
            requireNotNull(reference);
            this.priority = priority;
            this.reference = reference;
        }

        public int compareTo(ListenerReferenceHolder o) {
            int retVal = this.priority - o.priority;
            // same priority
            if (retVal == 0) {
                retVal = this.hashCode() - o.hashCode();
                // same hashcode but not equals. This shouldn't happen but according to hascode documentation is not required and since we don't want ListenerReferenceHolder to
                // disappear from the TreeSet we return an arbitrary integer != from 0
                if (retVal == 0 && !this.equals(o)) {
                    retVal = 1;
                }
            }
            return retVal;
        }

        public ListenerWrapper getListenerWrapper() {
            return reference.get();
        }
    }

}
