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


/**
 * An {@link EventStudio} is a thread-safe central place allowing broadcast of events to {@link Listener}s to registered on a Station. Stations are created internally as soon as a
 * listener is added but it's up to the user to clear a station when it's not needed anymore. {@link Listener}s can be added multiple times either to different or the same station
 * and they will be called as many times as they have been added.
 * <p>
 * As a general rule <em>null</em> parameters are not allowed (either station names, listeners, supervisors.. )
 * </p>
 * 
 * @author Andrea Vacondio
 * 
 */
public interface EventStudio {

    String MAX_QUEUE_SIZE_PROP = "eventstudio.max.queue.size";

    /**
     * Adds the given {@link Listener} to the given station using default priority(0) ad default strength {@link ReferenceStrength#STRONG}.
     */
    <T> void add(Listener<T> listener, String station);

    /**
     * Adds the given {@link Listener} to the given station using the given priority (low values mean higher priority) and strength.
     */
    <T> void add(Listener<T> listener, String station, int priority, ReferenceStrength strength);

    /**
     * Adds the given {@link Listener}, listening for the given event class, to the given station using default priority(0) ad default strength {@link ReferenceStrength#STRONG}.
     * This add method is useful when a listener can listen for a hierarchy of events.
     * 
     * @see EventStudio#add(Class, Listener, String, int, ReferenceStrength)
     */
    <T> void add(Class<T> eventClass, Listener<T> listener, String station);

    /**
     * Adds the given {@link Listener}, listening for the given event class, to the given station using the given priority (low values mean higher priority) and strength. This add
     * method is useful when a listener can listen for a hierarchy of events:
     * 
     * <pre>
     * {@code
     * class BroadListener{@code <T extends ParentEvent> implements Listener<T>} { 
     *     void onEvent(ParentEvent e){
     *           LOG.debug(e);
     *     }
     * }
     * 
     * class X {
     *   public void init() {
     *     EventStudio studio = ....
     *     studio.add(ChildEvent.class, new BroadListener{@code <ChildEvent>}(), "mystation");
     *     studio.add(AnotherChildEvent.class, new BroadListener{@code <AnotherChildEvent>()}, "mystation");
     *     ...
     *   }
     * }
     * 
     * }
     * </pre>
     */
    <T> void add(Class<T> eventClass, Listener<T> listener, String station, int priority, ReferenceStrength strength);

    /**
     * Discovers annotated method on the the given bean and adds them as {@link Listener}s
     * 
     * @param bean
     * @see org.pdfsam.eventstudio.annotation.EventListener
     * @see org.pdfsam.eventstudio.annotation.EventStation
     */
    void addAnnotatedListeners(Object bean);

    /**
     * Sets a {@link Supervisor} for the given station. It will be notified of every event broadcasted to the station prior its delivery to the proper {@link Listener}s allowing
     * event inspection.
     * <p>
     * {@link Supervisor}s cannot be removed but they can be replaces. See {@link Supervisor#SLACKER}
     * </p>
     */
    <T> void supervisor(Supervisor supervisor, String station);

    /**
     * Removes the first occurrence of the given {@link Listener} from the given station.
     * 
     * @return true if the listener was successfully removed
     */
    <T> boolean remove(Listener<T> listener, String station);

    /**
     * Removes the first occurrence of the given {@link Listener} listening for the given event, from the given station
     * 
     * @return true if the listener was successfully removed
     */
    <T> boolean remove(Class<T> eventClass, Listener<T> listener, String station);

    /**
     * Clear the given station removing the whole station from the {@link EventStudio} which means that {@link Listener}s and {@link Supervisor} will not be notified anymore. A
     * station with the same name can be recreated.
     */
    void clear(String station);

    /**
     * Broadcasts the given event to the given station. {@link Listener}s listening the given station and bound to the event class will be notified.
     */
    void broadcast(Object event, String station);

    /**
     * Broadcasts the given event to every station. {@link Listener}s bound to the event class (no matter the station they are listening) will be notified.
     */
    void broadcastToEveryStation(Object event);
}
