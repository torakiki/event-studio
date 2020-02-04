/* 
 * This file is part of the EventStudio source code
 * Created on 10/nov/2013
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

import static org.pdfsam.eventstudio.util.RequireUtils.requireNotBlank;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread safe holder for {@link Station}. Provides methods to access to the {@link Station}s of the {@link EventStudio} creating a new one when requred.
 * 
 * @author Andrea Vacondio
 * 
 */
class Stations {

    private static final Logger LOG = LoggerFactory.getLogger(Stations.class);

    private ConcurrentMap<String, Station> stations = new ConcurrentHashMap<>();

    /**
     * @param stationName
     * @return the station with the given name. It safely creates a new {@link Station} if a station with the given name does not exist.
     * @throws IllegalArgumentException
     *             if the station name is blank or null
     */
    Station getStation(String stationName) {
        requireNotBlank(stationName);
        Station station = stations.get(stationName);
        if (station == null) {
            final Station value = new Station(stationName);
            station = stations.putIfAbsent(stationName, value);
            if (station == null) {
                station = value;
                LOG.debug("Created station {}", station);
            }
        }
        return station;
    }
    
    /**
     * @return the collection of the configured stations
     * @see ConcurrentHashMap#values()
     */
    Collection<Station> getStations() {
        return stations.values();
    }

    void clear(String station) {
        LOG.debug("Clearing station {}", station);
        stations.remove(station);
    }
}
