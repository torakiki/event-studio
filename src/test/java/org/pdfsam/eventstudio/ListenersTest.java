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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pdfsam.eventstudio.Annotations.ReflectiveListenerDescriptor;
import org.pdfsam.eventstudio.Annotations.ReflectiveMetadata;
import org.pdfsam.eventstudio.Listeners.ListenerReferenceHolder;
import org.pdfsam.eventstudio.annotation.EventListener;

/**
 * @author Andrea Vacondio
 * 
 */
public class ListenersTest {

    private Listeners victim;

    @Before
    public void setUp() {
        victim = new Listeners();
    }

    @Test
    public void add() {
        assertTrue(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
        victim.add(TestEvent.class, new TestListener(), 0, ReferenceStrength.STRONG);
        assertFalse(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
        assertEquals(1, victim.nullSafeGetListeners(TestEvent.class).size());
    }

    @Test
    public void addSameListener() {
        TestListener listener = new TestListener();
        victim.add(TestEvent.class, listener, 0, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, listener, -1, ReferenceStrength.STRONG);
        assertEquals(2, victim.nullSafeGetListeners(TestEvent.class).size());
    }

    @Test
    public void addManyDiffenent() {
        victim.add(AnotherTestEvent.class, new AnotherTestListener(), 0, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, new TestListener(), -1, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, new SecondTestListener(), -1, ReferenceStrength.STRONG);
        assertEquals(2, victim.nullSafeGetListeners(TestEvent.class).size());
        assertEquals(1, victim.nullSafeGetListeners(AnotherTestEvent.class).size());
    }

    @Test
    public void remove() {
        TestListener listener = new TestListener();
        assertTrue(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
        victim.add(TestEvent.class, listener, 0, ReferenceStrength.STRONG);
        assertFalse(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
        victim.remove(TestEvent.class, listener);
        assertTrue(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
    }

    @Test
    public void removeMany() {
        TestListener listener = new TestListener();
        SecondTestListener listener2 = new SecondTestListener();
        AnotherTestListener anotherListener = new AnotherTestListener();
        victim.add(TestEvent.class, listener, 0, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, listener2, 0, ReferenceStrength.WEAK);
        victim.add(AnotherTestEvent.class, anotherListener, 0, ReferenceStrength.SOFT);
        assertFalse(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
        assertFalse(victim.nullSafeGetListeners(AnotherTestEvent.class).isEmpty());
        assertTrue(victim.remove(TestEvent.class, listener2));
        assertTrue(victim.remove(AnotherTestEvent.class, anotherListener));
        assertTrue(victim.nullSafeGetListeners(AnotherTestEvent.class).isEmpty());
        assertEquals(1, victim.nullSafeGetListeners(TestEvent.class).size());
    }

    @Test
    public void removeHolder() {
        TestListener listener = new TestListener();
        assertTrue(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
        victim.add(TestEvent.class, listener, 0, ReferenceStrength.STRONG);
        for (ListenerReferenceHolder holder : victim.nullSafeGetListeners(TestEvent.class)) {
            assertTrue(victim.remove(TestEvent.class, holder));
        }
        assertTrue(victim.nullSafeGetListeners(TestEvent.class).isEmpty());
    }

    @Test
    public void falseRemove() {
        TestListener listener = new TestListener();
        AnotherTestListener anotherListener = new AnotherTestListener();
        victim.add(TestEvent.class, listener, 0, ReferenceStrength.STRONG);
        assertFalse(victim.remove(AnotherTestEvent.class, anotherListener));
    }

    @Test
    public void priorityOrder() throws IllegalAccessException, InvocationTargetException {

        TestListener prio0 = new TestListener();
        TestListener prio1 = new TestListener();
        TestListener prio2 = new TestListener();
        TestListener prio3 = new TestListener();
        TestListener prio4 = new TestListener();
        TestListener prio5 = new TestListener();
        TestListener prio6 = new TestListener();
        TestListener prio7 = new TestListener();
        TestListener prio8 = new TestListener();
        TestListener prio9 = new TestListener();
        victim.add(TestEvent.class, prio7, 7, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio9, 9, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio0, 0, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio1, 1, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio2, 2, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio5, 5, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio6, 6, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio8, 8, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio3, 3, ReferenceStrength.STRONG);
        victim.add(TestEvent.class, prio4, 4, ReferenceStrength.STRONG);
        ReflectiveTestListener bean = new ReflectiveTestListener();
        ReflectiveMetadata metadata = Annotations.process(bean);
        for (List<ReflectiveListenerDescriptor> descriptors : metadata.getDescriptors().values()) {
            victim.addAll(bean, descriptors);
        }
        List<ListenerReferenceHolder> listeners = victim.nullSafeGetListeners(TestEvent.class);
        assertEquals(Integer.MIN_VALUE, listeners.get(0).priority);
        assertEquals(0, listeners.get(1).priority);
        assertEquals(1, listeners.get(2).priority);
        assertEquals(2, listeners.get(3).priority);
        assertEquals(3, listeners.get(4).priority);
        assertEquals(4, listeners.get(5).priority);
        assertEquals(4, listeners.get(6).priority);
        assertEquals(5, listeners.get(7).priority);
        assertEquals(6, listeners.get(8).priority);
        assertEquals(7, listeners.get(9).priority);
        assertEquals(8, listeners.get(10).priority);
        assertEquals(9, listeners.get(11).priority);
        assertEquals(Integer.MAX_VALUE, listeners.get(12).priority);
    }

    private static class AnotherTestListener implements Listener<AnotherTestEvent> {
        @Override
        public void onEvent(AnotherTestEvent event) {
            // nothing
        }
    }

    private static class SecondTestListener implements Listener<TestEvent> {
        @Override
        public void onEvent(TestEvent event) {
            // nothing
        }
    }

    private static class TestListener implements Listener<TestEvent> {
        @Override
        public void onEvent(TestEvent event) {
            // nothing
        }
    }

    private static class ReflectiveTestListener implements Listener<TestEvent> {
        @Override
        @EventListener(priority = 4)
        public void onEvent(TestEvent event) {
            // nothing
        }

        @EventListener(priority = Integer.MIN_VALUE)
        public void onSuperPrioEvent(TestEvent event) {
            // nothing
        }

        @EventListener(priority = Integer.MAX_VALUE)
        public void onLowestPrioEvent(TestEvent event) {
            // nothing
        }
    }

    private static class TestEvent {
        // nothing
    }

    private static class AnotherTestEvent {
        // nothing
    }

}
