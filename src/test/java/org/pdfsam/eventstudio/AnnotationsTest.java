/* 
 * This file is part of the EventStudio source code
 * Created on 16/nov/2013
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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;
import org.pdfsam.eventstudio.Annotations.ReflectiveListenerDescriptor;
import org.pdfsam.eventstudio.Annotations.ReflectiveMetadata;
import org.pdfsam.eventstudio.annotation.EventListener;
import org.pdfsam.eventstudio.annotation.EventStation;
import org.pdfsam.eventstudio.exception.EventStudioException;

/**
 * @author Andrea Vacondio
 * 
 */
public class AnnotationsTest {

    @Test
    public void stationField() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new StationField());
        assertEquals("StationField", metadata.getStation());
        assertEquals(1, metadata.getDescriptors().size());
    }

    @Test
    public void stationFieldEnum() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new StationFieldEnum());
        assertEquals("CHUCK", metadata.getStation());
        assertEquals(0, metadata.getDescriptors().size());
    }

    @Test
    public void stationMethod() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new StationMethod());
        assertEquals("myStation", metadata.getStation());
        assertEquals(2, metadata.getDescriptors().get("").size());
    }

    @Test
    public void stationMethodEnum() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new StationMethodEnum());
        assertEquals("NORRIS", metadata.getStation());
        assertEquals(0, metadata.getDescriptors().size());
    }

    @Test(expected = EventStudioException.class)
    public void wrongListener() throws IllegalAccessException, InvocationTargetException {
        Annotations.process(new WrongListener());
    }

    @Test(expected = EventStudioException.class)
    public void wrongStation() throws IllegalAccessException, InvocationTargetException {
        Annotations.process(new WrongStation());
    }

    @Test
    public void listenerWithStation() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new ListenerWithStation());
        assertEquals(1, metadata.getDescriptors().get("MyPersonalStation").size());
    }

    @Test
    public void inheritedListeners() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new ChildListener());
        List<ReflectiveListenerDescriptor> parentStation = metadata.getDescriptors().get("parentStation");
        assertEquals(1, parentStation.size());
        assertEquals("inheritedListen", parentStation.get(0).getMethod().getName());
    }

    @Test
    public void privateListeners() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new ChildListener());
        List<ReflectiveListenerDescriptor> childStation = metadata.getDescriptors().get("childStation");
        assertEquals(1, childStation.size());
        assertEquals("privateListen", childStation.get(0).getMethod().getName());
    }

    @Test
    public void overriddenNotAnnotatedListeners() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new ChildListener());
        List<ReflectiveListenerDescriptor> hiddenStation = metadata.getDescriptors().get("");
        assertEquals(null, hiddenStation);
    }

    @Test
    public void overriddenAnnotateListeners() throws IllegalAccessException, InvocationTargetException {
        ReflectiveMetadata metadata = Annotations.process(new AnnotatedChildListener());
        List<ReflectiveListenerDescriptor> hiddenStation = metadata.getDescriptors().get("");
        assertEquals(1, hiddenStation.size());
        assertEquals("listen", hiddenStation.get(0).getMethod().getName());
        assertEquals(AnnotatedChildListener.class, hiddenStation.get(0).getMethod().getDeclaringClass());
    }

    public static class ParentListener {
        @EventListener
        public void listen(String event) {
            // nothing
        }

        @EventListener(station = "parentStation")
        public void inheritedListen(String event) {
            // nothing
        }
    }

    public static class ChildListener extends ParentListener {
        @Override
        public void listen(String event) {
            // nothing
        }

        @EventListener(station = "childStation")
        private void privateListen(String another) {
            // nothing
        }
    }

    public static class AnnotatedChildListener extends ParentListener {
		@Override
		@EventListener
        public void listen(String event) {
            // nothing
        }

        @EventListener(station = "childStation")
        private void privateListen(String another) {
            // nothing
        }
    }
    public static class WrongStation {
        @EventStation
        public String withParams(Object first) {
            return first.toString();
        }
    }

    public static class WrongListener {
        @EventListener
        public void twoParams(Object first, Object second) {

        }
    }

    public static class StationField {
        @EventStation
        private String station = "StationField";

        @EventListener
        public void listenFor(Object event) {
            // nothing
        }
    }

    public static class StationMethod {
        @EventStation
        String stationName() {
            return "myStation";
        }

        @EventListener
        public void first(Object event) {
            // nothing
        }

        @EventListener
        public void second(Object event) {
            // nothing
        }
    }

    public static class ListenerWithStation {
        @EventListener(station = "MyPersonalStation")
        public void listenFor(Object event) {
            // nothing
        }
    }

    public enum WhateverEnum {
        CHUCK,
        NORRIS;
    }

    public static class StationFieldEnum {
        @EventStation
        private WhateverEnum station = WhateverEnum.CHUCK;
    }

    public static class StationMethodEnum {
        @EventStation
        WhateverEnum stationName() {
            return WhateverEnum.NORRIS;
        }
    }
}
