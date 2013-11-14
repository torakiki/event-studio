/* 
 * This file is part of the EventStudio source code
 * Created on 14/nov/2013
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * @author Andrea Vacondio
 * 
 */
public class StationTest {
    private Station victim;

    @Before
    public void setUp() {
        victim = new Station("victim");
    }

    @Test
    public void name() {
        assertEquals("victim", victim.name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullListener() {
        victim.add(null, 0, ReferenceStrength.WEAK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNullListenerLong() {
        victim.add(Object.class, null, 0, ReferenceStrength.WEAK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullEvent() {
        @SuppressWarnings("unchecked")
        Listener<Object> mock = Mockito.mock(Listener.class);
        victim.add(null, mock, 0, ReferenceStrength.WEAK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullBroadcastEvent() {
        victim.broadcast(null);
    }

    @Test
    public void supervisor() {
        Supervisor supervisor = mock(Supervisor.class);
        Object event = new Object();
        victim.supervior(supervisor);
        victim.broadcast(event);
        verify(supervisor).inspect(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addAndBroadcast() {
        Object event = new Object();
        Listener<Object> mockListener = Mockito.mock(Listener.class);
        Listener<Object> anotherMockListener = Mockito.mock(Listener.class);
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        victim.add(Object.class, anotherMockListener, 0, ReferenceStrength.STRONG);
        victim.broadcast(event);
        verify(mockListener).onEvent(event);
        verify(anotherMockListener).onEvent(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void priority() {
        Object event = new Object();
        Listener<Object> mockListener = Mockito.mock(Listener.class);
        Listener<Object> anotherMockListener = Mockito.mock(Listener.class);
        InOrder inOrder = Mockito.inOrder(anotherMockListener, mockListener);
        victim.add(mockListener, 0, ReferenceStrength.STRONG);
        victim.add(anotherMockListener, -1, ReferenceStrength.STRONG);
        victim.broadcast(event);
        inOrder.verify(anotherMockListener).onEvent(event);
        inOrder.verify(mockListener).onEvent(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void broadcastAndAdd() {
        Object event = new Object();
        victim.broadcast(event);
        victim.broadcast(event);
        Listener<Object> mockListener = Mockito.mock(Listener.class);
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        verify(mockListener, times(2)).onEvent(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeAndBroadcast() {
        Object event = new Object();
        Listener<Object> mockListener = Mockito.mock(Listener.class);
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        victim.remove(mockListener);
        victim.broadcast(event);
        verify(mockListener, never()).onEvent(event);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeExplicitAndBroadcast() {
        Object event = new Object();
        Listener<Object> mockListener = Mockito.mock(Listener.class);
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        victim.remove(Object.class, mockListener);
        victim.broadcast(event);
        verify(mockListener, never()).onEvent(event);
    }
}
