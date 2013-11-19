/* 
 * This file is part of the EventStudio source code
 * Created on 16/nov/2013
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
package org.sejda.eventstudio;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.sejda.eventstudio.Annotations;
import org.sejda.eventstudio.Annotations.ReflectiveMetadata;
import org.sejda.eventstudio.annotation.EventListener;
import org.sejda.eventstudio.annotation.EventStation;
import org.sejda.eventstudio.exception.EventStudioException;

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
