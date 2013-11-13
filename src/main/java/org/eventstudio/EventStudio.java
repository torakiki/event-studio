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

/**
 * An {@link EventStudio} is the central place allowing {@link Broadcaster} to send their message and {@link Listener}s to listen the {@link Station}s they are interested in.
 * 
 * @author Andrea Vacondio
 * 
 */
public interface EventStudio {
    // TODO docs. Returns boolean because it's a set
    <T> boolean add(Listener<T> listener, String station, int priority, ReferenceStrength strength);
    <T> boolean add(Listener<T> listener, String station);

    <T> boolean add(Class<T> eventClass, Listener<T> listener, String station);

    <T> boolean add(Class<T> eventClass, Listener<T> listener, String station, int priority, ReferenceStrength strength);
    <T> boolean supervisor(Supervisor<T> supervisor);

    <T> void stationSupervisor(StationSupervisor supervisor, String station);
    
    <T> boolean remove(Listener<T> listener, String station);
    <T> boolean remove(Listener<T> listener);
    boolean clear(String station);
    
    void broadcast(Object event, String station);
    boolean broadcastToEveryStation(Object event);
    //TODO async?
}
