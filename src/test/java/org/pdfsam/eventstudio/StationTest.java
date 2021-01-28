/* 
 * This file is part of the EventStudio source code
 * Created on 14/nov/2013
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pdfsam.eventstudio.Annotations.ReflectiveMetadata;
import org.pdfsam.eventstudio.annotation.EventListener;
import org.pdfsam.eventstudio.exception.BroadcastInterruptionException;
import org.pdfsam.eventstudio.exception.EventStudioException;

/**
 * @author Andrea Vacondio
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class StationTest {

    private Station victim;

    @Mock
    private Listener<Object> mockListener;
    @Mock
    private Listener<Object> anotherMockListener;
    @Mock
    private Object bean;

    @Before
    public void setUp() {
        victim = new Station("victim");
    }

    @After
    public void tearDown() {
        victim.remove(mockListener);
        victim.remove(anotherMockListener);
        System.clearProperty(EventStudio.MAX_QUEUE_SIZE_PROP);
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
        victim.add(null, mockListener, 0, ReferenceStrength.WEAK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullBroadcastEvent() {
        victim.broadcast(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAddAll() {
        victim.addAll(bean, null);
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
    public void addAndBroadcast() {
        Object event = new Object();
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        victim.add(Object.class, anotherMockListener, 0, ReferenceStrength.STRONG);
        victim.broadcast(event);
        verify(mockListener).onEvent(event);
        verify(anotherMockListener).onEvent(event);
    }

    @Test
    public void annotatedAddAndBroadcast() throws IllegalAccessException, InvocationTargetException {
        Object event = new Object();
        TestPrioritizedAnnotatedBean bean = new TestPrioritizedAnnotatedBean();
        ReflectiveMetadata metadata = Annotations.process(bean);
        TestPrioritizedAnnotatedBean spy = spy(bean);
        victim.addAll(spy, metadata.getDescriptors().get(""));
        victim.broadcast(event);
        verify(spy).first(event);
        verify(spy).second(event);
    }

    @Test
    public void priority() {
        Object event = new Object();
        InOrder inOrder = Mockito.inOrder(anotherMockListener, mockListener);
        victim.add(mockListener, 0, ReferenceStrength.STRONG);
        victim.add(anotherMockListener, -1, ReferenceStrength.STRONG);
        victim.broadcast(event);
        inOrder.verify(anotherMockListener).onEvent(event);
        inOrder.verify(mockListener).onEvent(event);
    }

    @Test
    public void broadcastInterrupted() {
        Object event = new Object();
        doThrow(BroadcastInterruptionException.class).when(anotherMockListener).onEvent(any());
        victim.add(mockListener, 0, ReferenceStrength.STRONG);
        victim.add(anotherMockListener, -1, ReferenceStrength.STRONG);
        victim.broadcast(event);
        verify(anotherMockListener).onEvent(event);
        verify(mockListener, never()).onEvent(event);
    }

    @Test
    public void broadcastAndAdd() {
        Object event = new Object();
        victim.broadcast(event);
        victim.broadcast(event);
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        verify(mockListener, times(2)).onEvent(event);
    }

    @Test
    public void broadcastAndAddAnnotated() throws IllegalAccessException, InvocationTargetException {
        Object event = new Object();
        victim.broadcast(event);
        TestPrioritizedAnnotatedBean bean = new TestPrioritizedAnnotatedBean();
        ReflectiveMetadata metadata = Annotations.process(bean);
        TestPrioritizedAnnotatedBean spy = spy(bean);
        victim.addAll(spy, metadata.getDescriptors().get(""));
        verify(spy).first(event);
        verify(spy).second(event);
    }

    @Test
    public void broadcastInterruptedAnnotated() throws IllegalAccessException, InvocationTargetException {
        Object event = new Object();
        TestInterruptingPrioritizedAnnotatedBean bean = new TestInterruptingPrioritizedAnnotatedBean();
        ReflectiveMetadata metadata = Annotations.process(bean);
        TestInterruptingPrioritizedAnnotatedBean spy = spy(bean);
        victim.addAll(spy, metadata.getDescriptors().get(""));
        victim.broadcast(event);
        verify(spy).first(event);
        verify(spy, never()).second(event);
    }

    @Test
    public void removeAndBroadcast() {
        Object event = new Object();
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        victim.remove(mockListener);
        victim.broadcast(event);
        verify(mockListener, never()).onEvent(event);
    }

    @Test
    public void removeExplicitAndBroadcast() {
        Object event = new Object();
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        victim.remove(Object.class, mockListener);
        victim.broadcast(event);
        verify(mockListener, never()).onEvent(event);
    }

    @Test(expected = EventStudioException.class)
    public void failingAdd() {
        SecondTestListener<String> listener = new SecondTestListener<>();
        victim.add(listener, 0, ReferenceStrength.STRONG);
    }

    @Test(expected = EventStudioException.class)
    public void failingRemove() {
        SecondTestListener<String> listener = new SecondTestListener<>();
        victim.remove(listener);
    }

    @Test
    public void capacity() {
        System.setProperty(EventStudio.MAX_QUEUE_SIZE_PROP, "3");
        victim.broadcast(new Object());
        victim.broadcast(new Object());
        victim.broadcast(new Object());
        victim.broadcast(new Object());
        victim.broadcast(new Object());
        victim.broadcast(new Object());
        victim.add(Object.class, mockListener, 0, ReferenceStrength.STRONG);
        verify(mockListener, times(3)).onEvent(any(Object.class));
    }

    private class SecondTestListener<T extends Object> implements Listener<T> {
        @Override
        public void onEvent(T event) {
            // nothing
        }
    }

    private class TestInterruptingPrioritizedAnnotatedBean {

        @EventListener(priority = 1)
        public void first(Object event) {
            throw new BroadcastInterruptionException("");
        }

        @EventListener(priority = 2)
        public void second(Object event) {

        }

    }
}
