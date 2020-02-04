/* 
 * This file is part of the EventStudio source code
 * Created on 09/nov/2013
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

import static org.pdfsam.eventstudio.EventStudio.MAX_QUEUE_SIZE_PROP;
import static org.pdfsam.eventstudio.util.ReflectionUtils.inferParameterClass;
import static org.pdfsam.eventstudio.util.RequireUtils.requireNotBlank;
import static org.pdfsam.eventstudio.util.RequireUtils.requireNotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.pdfsam.eventstudio.Annotations.ReflectiveListenerDescriptor;
import org.pdfsam.eventstudio.Listeners.ListenerReferenceHolder;
import org.pdfsam.eventstudio.Listeners.ListenerWrapper;
import org.pdfsam.eventstudio.exception.BroadcastInterruptionException;
import org.pdfsam.eventstudio.exception.EventStudioException;
import org.pdfsam.eventstudio.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A {@link Station} is a place where broadcaster events are actually transmitted to the registered {@link Listener}s
 * 
 * @author Andrea Vacondio
 * 
 */
class Station {

    private static final Logger LOG = LoggerFactory.getLogger(Station.class);

    private ConcurrentMap<Class<?>, BlockingQueue<Object>> queues = new ConcurrentHashMap<>();
    private Listeners listeners = new Listeners();
    private volatile Supervisor supervisor = Supervisor.SLACKER;
    private final String name;

    Station(String name) {
        requireNotBlank(name);
        this.name = name;
    }

    private BlockingQueue<Object> getQueue(Class<?> clazz) {
        BlockingQueue<Object> queue = queues.get(clazz);
        if (queue == null) {
            final BlockingQueue<Object> value = new LinkedBlockingQueue<>(Integer.getInteger(MAX_QUEUE_SIZE_PROP,
                    Integer.MAX_VALUE));
            queue = queues.putIfAbsent(clazz, value);
            if (queue == null) {
                queue = value;
            }
        }
        return queue;
    }

    public void broadcast(Object event) {
        LOG.debug("{}: Broadcasting {}", this, event);
        requireNotNull(event);
        LOG.trace("{}: Supervisor {} about to inspect", this, supervisor);
        supervisor.inspect(event);
        LOG.trace("{}: Listeners about to listen", this);
        try {
            doBroadcast(event);
        } catch (BroadcastInterruptionException e) {
            LOG.info("Broadcasting was interrupted.", e);
        }
    }

    private boolean doBroadcast(Object event) {
        List<ListenerReferenceHolder> eventListeners = listeners.nullSafeGetListeners(event.getClass());
        LOG.debug("{}: Found {} listeners", this, eventListeners.size());
        Envelope enveloped = new Envelope(event);
        for (ListenerReferenceHolder holder : eventListeners) {
            ListenerWrapper listener = holder.getListenerWrapper();
            if (listener != null) {
                LOG.trace("{}: Notifing event {} to {}", this, event, listener);
                listener.onEvent(enveloped);
            } else {
                LOG.debug("{}: Removing garbage collected listener from the station", this);
                listeners.remove(event.getClass(), holder);
            }
        }
        if (!enveloped.isNotified()) {
            LOG.debug("{}: No one is listening for {}, enqueuing for future listeners", this, event);
            if (!getQueue(event.getClass()).offer(event)) {
                LOG.warn("{}: Max capacity might be reached, unable to store unlistened event, it's going to be lost {}", this, event);
            }
        }
        return enveloped.isNotified();
    }

    <T> void add(Listener<T> listener, int priority, ReferenceStrength strength) {
        requireNotNull(listener);
        @SuppressWarnings("unchecked")
        Class<T> eventClass = ReflectionUtils.inferParameterClass(listener.getClass(), "onEvent");
        if (eventClass == null) {
            throw new EventStudioException("Unable to infer the listened event class.");
        }
        add(eventClass, listener, priority, strength);
    }

    <T> void add(Class<T> eventClass, Listener<T> listener, int priority, ReferenceStrength strength) {
        requireNotNull(eventClass);
        requireNotNull(listener);
        LOG.debug("{}: Adding listener {} [priority={} strength={}]", this, listener, priority, strength);
        listeners.add(eventClass, listener, priority, strength);
        broadcastEnqueuedEventsFor(eventClass);
    }

    void addAll(Object bean, List<ReflectiveListenerDescriptor> descriptors) {
        requireNotNull(descriptors);
        LOG.debug("{}: Adding {} reflective listeners for {}", this, descriptors.size(), bean);
        Set<Class<?>> updatedEventClasses = listeners.addAll(bean, descriptors);
        for (Class<?> updatedClass : updatedEventClasses) {
            broadcastEnqueuedEventsFor(updatedClass);
        }
    }

    private void broadcastEnqueuedEventsFor(Class<?> updatedClass) {
        BlockingQueue<Object> queue = getQueue(updatedClass);
        Object event = null;
        boolean keepBroadcasting = true;
        while (keepBroadcasting && (event = queue.poll()) != null) {
            LOG.debug("{}: Found enqueued event {}, now broadcasting it.", this, event);
            keepBroadcasting = doBroadcast(event);
        }
    }

    <T> boolean remove(Listener<T> listener) {
        requireNotNull(listener);
        @SuppressWarnings("unchecked")
        Class<T> eventClass = inferParameterClass(listener.getClass(), "onEvent");
        if (eventClass == null) {
            throw new EventStudioException("Unable to infer the listened event class.");
        }
        return remove(eventClass, listener);
    }

    <T> boolean remove(Class<T> eventClass, Listener<T> listener) {
        requireNotNull(eventClass);
        requireNotNull(listener);
        LOG.debug("{}: Removing listener {} [eventClass={}]", this, listener, eventClass);
        return listeners.remove(eventClass, listener);
    }

    /**
     * @return name of the station
     */
    String name() {
        return name;

    }

    public void supervior(Supervisor supervisor) {
        requireNotNull(supervisor);
        this.supervisor = supervisor;
    }

    @Override
    public String toString() {
        return String.format("Station[%s]", name);
    }
}
