/* 
 * This file is part of the EventStudio source code
 * Created on 15/nov/2013
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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Andrea Vacondio
 * 
 */
public class DefaultEventStudioTest {

    private static final String STATION = "station";
    @Mock
    private Station anotherStation, station, hidden;
    @Mock
    private Stations stations;
    @Mock
    private Supervisor supervisor;
    @Mock
    private Listener<Object> listener;
    @Mock
    private Object event;
    @InjectMocks
    private DefaultEventStudio victim;

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
        when(stations.getStation(DefaultEventStudio.HIDDEN_STATION)).thenReturn(hidden);
        when(stations.getStation(STATION)).thenReturn(station);
        when(stations.getStation("anotherStation")).thenReturn(anotherStation);
        List<Station> stationsCollection = new ArrayList<>();
        stationsCollection.add(hidden);
        stationsCollection.add(station);
        when(stations.getStations()).thenReturn(stationsCollection);
    }

    @Test
    public void addHiddenStation() {
        victim.add(listener);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).add(listener, 0, ReferenceStrength.STRONG);
    }

    @Test
    public void addHiddenStationWithEventClass() {
        victim.add(Object.class, listener);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).add(Object.class, listener, 0, ReferenceStrength.STRONG);
    }

    @Test
    public void addStation() {
        victim.add(listener, STATION);
        verify(stations).getStation(STATION);
        verify(station).add(listener, 0, ReferenceStrength.STRONG);
    }

    @Test
    public void addStationWithEventClass() {
        victim.add(Object.class, listener, STATION);
        verify(stations).getStation(STATION);
        verify(station).add(Object.class, listener, 0, ReferenceStrength.STRONG);
    }

    @Test
    public void addHiddenStationWithPriority() {
        victim.add(listener, 1, ReferenceStrength.SOFT);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).add(listener, 1, ReferenceStrength.SOFT);
    }

    @Test
    public void addHiddenStationWithPriorityAndEventClass() {
        victim.add(Object.class, listener, 1, ReferenceStrength.SOFT);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).add(Object.class, listener, 1, ReferenceStrength.SOFT);
    }

    @Test
    public void addStationWithPriority() {
        victim.add(listener, STATION, 1, ReferenceStrength.SOFT);
        verify(stations).getStation(STATION);
        verify(station).add(listener, 1, ReferenceStrength.SOFT);
    }

    @Test
    public void addStationWithPriorityAndEventClass() {
        victim.add(Object.class, listener, STATION, 1, ReferenceStrength.SOFT);
        verify(stations).getStation(STATION);
        verify(station).add(Object.class, listener, 1, ReferenceStrength.SOFT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSupervisor() {
        victim.supervisor(null);
    }

    @Test
    public void removeHidden() {
        victim.remove(listener);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).remove(listener);
    }

    @Test
    public void removeHiddenWithClass() {
        victim.remove(Object.class, listener);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).remove(Object.class, listener);
    }

    @Test
    public void removeFromStation() {
        victim.remove(listener, STATION);
        verify(stations).getStation(STATION);
        verify(station).remove(listener);
    }

    @Test
    public void removeFromStationWithClass() {
        victim.remove(Object.class, listener, STATION);
        verify(stations).getStation(STATION);
        verify(station).remove(Object.class, listener);
    }

    @Test
    public void SupervisorHidden() {
        victim.supervisor(supervisor);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).supervior(supervisor);
    }

    @Test
    public void Supervisor() {
        victim.supervisor(supervisor, STATION);
        verify(stations).getStation(STATION);
        verify(station).supervior(supervisor);
    }

    @Test
    public void clearHidden() {
        victim.clear();
        verify(stations).clear(DefaultEventStudio.HIDDEN_STATION);
    }

    @Test
    public void clear() {
        victim.clear(STATION);
        verify(stations).clear(STATION);
    }

    @Test
    public void broadcastHidden() {
        victim.broadcast(event);
        verify(stations).getStation(DefaultEventStudio.HIDDEN_STATION);
        verify(hidden).broadcast(event);
    }

    @Test
    public void broadcastStation() {
        victim.broadcast(event, STATION);
        verify(stations).getStation(STATION);
        verify(station).broadcast(event);
    }

    @Test
    public void broadcastAll() {
        victim.broadcastToEveryStation(event);
        verify(hidden).broadcast(event);
        verify(station).broadcast(event);
    }

    @Test
    public void addAnnotatedListeners() {
        TestAnnotatedBean bean = new TestAnnotatedBean();
        victim.addAnnotatedListeners(bean);
        verify(stations).getStation(STATION);
        verify(stations).getStation("anotherStation");
        verify(station).addAll(eq(bean), anyList());
        verify(anotherStation).addAll(eq(bean), anyList());
    }
}
