/* 
 * This file is part of the EventStudio source code
 * Created on 09/nov/2013
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

import static org.eventstudio.util.RequireUtils.requireNotBlank;
import static org.eventstudio.util.RequireUtils.requireNotNull;

import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.eventstudio.Listeners.ListenerReferenceHolder;
import org.eventstudio.Listeners.ListenerWrapper;
import org.eventstudio.exception.EventStudioException;
import org.eventstudio.util.ReflectionUtils;
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

    private ConcurrentMap<Class<?>, BlockingQueue<Object>> queues = new ConcurrentHashMap<Class<?>, BlockingQueue<Object>>();
    private Listeners listeners = new Listeners();
    private volatile Supervisor supervisor = Supervisor.SLACKER;
    private final String name;

    Station(String name) {
        requireNotBlank(name);
        this.name = name;
    }

    BlockingQueue<Object> getQueue(Class<?> clazz) {
        BlockingQueue<Object> queue = queues.get(clazz);
        if (queue == null) {
            final BlockingQueue<Object> value = new LinkedBlockingQueue<Object>();
            queue = queues.putIfAbsent(clazz, value);
            if (queue == null) {
                queue = value;
            }
        }
        return queue;
    }

    public void broadcast(Object event) {
        LOG.trace("{}: Supervisor {} about to inspect", this, supervisor);
        supervisor.inspect(event);
        LOG.trace("{}: Listeners about to listen", this);
        doBroadcast(event);
    }

    private void doBroadcast(Object event) {
        TreeSet<ListenerReferenceHolder> eventListeners = new TreeSet<ListenerReferenceHolder>(listeners.getQueue(event
                .getClass()));
        Envelope enveloped = new Envelope(event);
        for (ListenerReferenceHolder holder : eventListeners) {
            ListenerWrapper listener = holder.getListenerWrapper();
            if (listener != null) {
                LOG.trace("{}: Notifing event {} to {}", this, event, listener);
                listener.onEvent(enveloped);
            } else {
                LOG.trace("{}: Removing garbage collected listener from the station", this);
                listeners.remove(event.getClass(), listener);
            }
        }
        if (!enveloped.isNotified()) {
            LOG.debug("{}: No one is listening for {}, stored for future listeners", this, event);
            if (!getQueue(event.getClass()).offer(event)) {
                // this shouldn't happen since we don't have constraints on stored event queue capacity
                LOG.warn("{}: Unable to store unlistened event, it's going to be lost {}", this, event);
            }
        }
    }

    boolean add(Listener<?> listener, int priority, ReferenceStrength strength) {
        Class<?> eventClass = ReflectionUtils.inferParameterClass(listener.getClass(), "onEvent");
        if (eventClass == null) {
            throw new EventStudioException("Unable to infer the listened event class.");
        }
        return add(eventClass, listener, priority, strength);
    }

    boolean add(Listener<?> listener) {
        return add(listener, 0, ReferenceStrength.STRONG);
    }

    boolean add(Class<?> eventClass, Listener<?> listener) {
        return add(eventClass, listener, 0, ReferenceStrength.STRONG);
    }

    boolean add(Class<?> eventClass, Listener<?> listener, int priority, ReferenceStrength strength) {
        LOG.trace("{}: Adding listener {} [priority={} strength={}]", this, listener, priority, strength);
        return listeners.add(eventClass, listener, priority, strength);
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
