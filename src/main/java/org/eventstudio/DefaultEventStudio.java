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

/**
 * @author Andrea Vacondio
 * 
 */
public class DefaultEventStudio implements EventStudio {
    private Stations stations = new Stations();

    public <T> boolean add(Listener<T> listener, String station, int priority, ReferenceStrength strength) {
        return stations.getStation(station).add(listener, priority, strength);
    }

    public <T> boolean add(Listener<T> listener, String station) {
        return stations.getStation(station).add(listener);
    }

    public <T> boolean supervisor(Supervisor<T> supervisor) {
        // TODO
        return true;
    }

    public <T> void stationSupervisor(StationSupervisor supervisor, String station) {
        stations.getStation(station).supervior(supervisor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eventstudio.EventStudio#remove(org.eventstudio.Listener, java.lang.String)
     */
    public <T> boolean remove(Listener<T> listener, String station) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eventstudio.EventStudio#remove(org.eventstudio.Listener)
     */
    public <T> boolean remove(Listener<T> listener) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eventstudio.EventStudio#clear(java.lang.String)
     */
    public boolean clear(String station) {
        // TODO Auto-generated method stub
        return false;
    }

    public void broadcast(Object event, String station) {
        stations.getStation(station).broadcast(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eventstudio.EventStudio#broadcastToEveryStation(java.lang.Object)
     */
    public boolean broadcastToEveryStation(Object event) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eventstudio.EventStudio#add(java.lang.Class, org.eventstudio.Listener, java.lang.String)
     */
    public <T> boolean add(Class<T> eventClass, Listener<T> listener, String station) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eventstudio.EventStudio#add(java.lang.Class, org.eventstudio.Listener, java.lang.String, int, org.eventstudio.ReferenceStrength)
     */
    public <T> boolean add(Class<T> eventClass, Listener<T> listener, String station, int priority,
            ReferenceStrength strength) {
        // TODO Auto-generated method stub
        return false;
    }

}
