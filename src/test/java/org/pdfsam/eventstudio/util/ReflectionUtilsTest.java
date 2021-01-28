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
package org.pdfsam.eventstudio.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pdfsam.eventstudio.Listener;

/**
 * @author Andrea Vacondio
 * 
 */
public class ReflectionUtilsTest {
    @Test
    public void testInfer() {
        TestListener victim = new TestListener();
        assertEquals(TestEvent.class, ReflectionUtils.inferParameterClass(victim.getClass(), "onEvent"));
    }

    @Test
    public void testFailingInfer() {
        SecondTestListener<TestEvent> victim = new SecondTestListener<>();
        assertEquals(null, ReflectionUtils.inferParameterClass(victim.getClass(), "onEvent"));
    }

    private class TestListener implements Listener<TestEvent> {
        public void onEvent(TestEvent event) {
            // nothing
        }
    }

    private static class TestEvent {

    }

    private class SecondTestListener<T extends TestEvent> implements Listener<T> {
        public void onEvent(T event) {
            // nothing
        }
    }
}
