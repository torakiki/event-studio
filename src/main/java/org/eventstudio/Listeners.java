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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A thread-safe holder for the listeners
 * 
 * @author Andrea Vacondio
 * 
 */
class Listeners {

    private ConcurrentMap<Class<?>, CopyOnWriteArraySet<ListenerReferenceHolder>> listeners = new ConcurrentHashMap<Class<?>, CopyOnWriteArraySet<ListenerReferenceHolder>>();

    boolean add(Class<?> eventClass, Listener<?> listener, int priority, ReferenceStrength strength) {
        return getQueue(eventClass).add(
                new ListenerReferenceHolder(priority, strength.getReference(new DefaultListenerWrapper(listener))));
    }

    boolean remove(Class<?> eventClass, ListenerWrapper listener) {
        return getQueue(eventClass).remove(listener);
    }


    /**
     * @param eventClass
     * @return the listeners queue for the given class. It safely creates a new listeners {@link CopyOnWriteArraySet} if does not exist already.
     */
    CopyOnWriteArraySet<ListenerReferenceHolder> getQueue(Class<?> eventClass) {
        CopyOnWriteArraySet<ListenerReferenceHolder> queue = listeners.get(eventClass);
        if (queue == null) {
            final CopyOnWriteArraySet<ListenerReferenceHolder> value = new CopyOnWriteArraySet<ListenerReferenceHolder>();
            queue = listeners.putIfAbsent(eventClass, value);
            if (queue == null) {
                queue = value;
            }
        }
        return queue;
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
            return this.priority - o.priority;
        }

        public ListenerWrapper getListenerWrapper() {
            return reference.get();
        }
    }

}
