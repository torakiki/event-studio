/* 
 * This file is part of the EventStudio source code
 * Created on 10/nov/2013
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

/**
 * Default implementation of {@link EventStudio}. It doesn't enforce a Singleton pattern and it's up to the user to decide how to use it and how many EventStudio the application
 * needs. Add and remove operations are expensive and best performances are obtained when the set of {@link Listener}s is relatively stable over time and when the number of
 * {@link Listener}s listening for the same event on the same station is relatively small.
 * <p>
 * <b>Hidden Station</b>: The hidden station is a pre-built station with <em>"hidden.station"</em> name that is used to hide the station abstraction. Helper method are provided by
 * {@link DefaultEventStudio} where the station name parameter is missing from the parameters list and the {@link DefaultEventStudio#HIDDEN_STATION} is used, providing a more
 * traditional event bus pub/sub pattern.
 * </p>
 * 
 * @author Andrea Vacondio
 * 
 */
public class DefaultEventStudio implements EventStudio {
    /**
     * A reserved station name that is used to hide the station abstraction. Using the provided helper methods the station concept remains totally hidden and {@link EventStudio}
     * can be used as a more traditional Event Bus with pub/sub pattern.
     */
    public static final String HIDDEN_STATION = "hidden.station";

    private Stations stations = new Stations();

    public <T> void add(Listener<T> listener, String station, int priority, ReferenceStrength strength) {
        stations.getStation(station).add(listener, priority, strength);
    }

    public <T> void add(Listener<T> listener, String station) {
        add(listener, station, 0, ReferenceStrength.STRONG);
    }

    public <T> void add(Class<T> eventClass, Listener<T> listener, String station) {
        add(eventClass, listener, station, 0, ReferenceStrength.STRONG);
    }

    public <T> void add(Class<T> eventClass, Listener<T> listener, String station, int priority,
            ReferenceStrength strength) {
        stations.getStation(station).add(eventClass, listener, priority, strength);
    }

    /**
     * Adds a {@link Listener} (with the given priority and strength ) to the hidden station listening for the given event class, hiding the station abstraction.
     * 
     * @see EventStudio#add(Listener, String, int, ReferenceStrength)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public <T> void add(Class<T> eventClass, Listener<T> listener, int priority, ReferenceStrength strength) {
        add(eventClass, listener, HIDDEN_STATION, priority, strength);
    }

    /**
     * Adds a {@link Listener} (with the given priority and strength ) to the hidden station, hiding the station abstraction.
     * 
     * @see EventStudio#add(Listener, String, int, ReferenceStrength)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public <T> void add(Listener<T> listener, int priority, ReferenceStrength strength) {
        add(listener, HIDDEN_STATION, priority, strength);
    }

    /**
     * Adds a {@link Listener} to the hidden station, hiding the station abstraction.
     * 
     * @see EventStudio#add(Listener, String)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public <T> void add(Listener<T> listener) {
        add(listener, HIDDEN_STATION);
    }

    /**
     * Adds a {@link Listener} to the hidden station listening for the given event class, hiding the station abstraction.
     * 
     * @see EventStudio#add(Class, Listener, String)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public <T> void add(Class<T> eventClass, Listener<T> listener) {
        add(eventClass, listener, HIDDEN_STATION);
    }

    /**
     * Adds a {@link Supervisor} to the hidden station, hiding the station abstraction.
     * 
     * @param supervisor
     * @see EventStudio#supervisor(Supervisor, String)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public void supervisor(Supervisor supervisor) {
        supervisor(supervisor, HIDDEN_STATION);
    }

    public void supervisor(Supervisor supervisor, String station) {
        requireNotNull(supervisor);
        stations.getStation(station).supervior(supervisor);
    }

    public <T> boolean remove(Listener<T> listener, String station) {
        return stations.getStation(station).remove(listener);
    }

    public <T> boolean remove(Class<T> eventClass, Listener<T> listener, String station) {
        return stations.getStation(station).remove(listener);
    }

    /**
     * Removes the given listener from the hidden station, hiding the station abstraction.
     * 
     * @return true if the listener was found and removed
     * @see EventStudio#remove(Listener, String)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public <T> boolean remove(Listener<T> listener) {
        return remove(listener, HIDDEN_STATION);
    }

    /**
     * Removes the given listener listening on the given event, from the hidden station, hiding the station abstraction.
     * 
     * @return true if the listener was found and removed
     * @see EventStudio#remove(Listener, String)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public <T> boolean remove(Class<T> eventClass, Listener<T> listener) {
        return remove(eventClass, listener, HIDDEN_STATION);
    }

    public void clear(String station) {
        stations.clear(station);
    }

    /**
     * Clears the hidden station
     * 
     * @see EventStudio#clear(String)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public void clear() {
        stations.clear(HIDDEN_STATION);
    }

    public void broadcast(Object event, String station) {
        stations.getStation(station).broadcast(event);
    }

    /**
     * Boradcast the event to the hidden station
     * 
     * @see EventStudio#broadcast(Object, String)
     * @see DefaultEventStudio#HIDDEN_STATION
     */
    public void broadcast(Object event) {
        stations.getStation(HIDDEN_STATION).broadcast(event);
    }

    public void broadcastToEveryStation(Object event) {
        for (Station station : stations.getStations()) {
            station.broadcast(event);
        }
    }
}
