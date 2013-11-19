/* 
 * This file is part of the EventStudio source code
 * Created on 15/nov/2013
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

import static org.sejda.eventstudio.util.RequireUtils.requireNotNull;
import static org.sejda.eventstudio.util.StringUtils.isBlank;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sejda.eventstudio.annotation.EventListener;
import org.sejda.eventstudio.annotation.EventStation;
import org.sejda.eventstudio.exception.EventStudioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods processing beans to find annotated method or fields and register reflective listeners.
 * 
 * @author Andrea Vacondio
 * 
 */
final class Annotations {

    private static final Logger LOG = LoggerFactory.getLogger(Annotations.class);

    private Annotations() {
        // utility
    }

    public static ReflectiveMetadata process(Object bean) throws IllegalAccessException, InvocationTargetException {
        requireNotNull(bean);
        LOG.trace("Processing {} for annotated listeners", bean);
        String station = getStationNameFromFieldIfAny(bean);
        ReflectiveMetadata metadata = new ReflectiveMetadata();
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (isBlank(station)) {
                station = getStationNameIfAnnotated(method, bean);
            }
            EventListener listenerAnnotation = method.getAnnotation(EventListener.class);
            if (listenerAnnotation != null) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new EventStudioException(
                            "@EventListener annotated method expected to be a single parameter method");
                }
                LOG.trace("Found @EventListener annotated method {}", method);
                metadata.put(listenerAnnotation.station(), new ReflectiveListenerDescriptor(
                        listenerAnnotation, method));
            }
        }
        metadata.station = station;
        return metadata;
    }

    private static String getStationNameIfAnnotated(Method method, Object bean) throws InvocationTargetException,
            IllegalAccessException {
        if (method.isAnnotationPresent(EventStation.class)) {
            if (method.getParameterTypes().length > 0) {
                throw new EventStudioException("@EventStation annotated method expected to be a no parameters method.");
            }
            method.setAccessible(true);
            LOG.trace("Found @EventStation annotated method {}", method);
            if (method.getReturnType().isEnum()) {
                return method.invoke(bean).toString();
            }
            return (String) method.invoke(bean);
        }
        return null;
    }

    /**
     * @param bean
     * @throws IllegalAccessException
     * @return a String value with the name of the station if an annotated field was found, null otherwise.
     */
    private static String getStationNameFromFieldIfAny(Object bean) throws IllegalAccessException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.getAnnotation(EventStation.class) != null) {
                field.setAccessible(true);
                Object value = field.get(bean);
                if (value.getClass().isEnum()) {
                    return value.toString();
                }
                return (String) value;
            }
        }
        return null;
    }

    /**
     * Holds metadata retrieved from the reflective inspection of a bean
     * 
     * @author Andrea Vacondio
     * 
     */
    static class ReflectiveMetadata {
        private String station;
        private Map<String, List<ReflectiveListenerDescriptor>> descriptors = new HashMap<String, List<ReflectiveListenerDescriptor>>();

        private void put(String station, ReflectiveListenerDescriptor descriptor) {
            List<ReflectiveListenerDescriptor> current = descriptors.get(station);
            if (current == null) {
                current = new ArrayList<ReflectiveListenerDescriptor>();
                descriptors.put(station, current);
            }
            current.add(descriptor);
        }

        public String getStation() {
            return station;
        }

        public Map<String, List<ReflectiveListenerDescriptor>> getDescriptors() {
            return descriptors;
        }

    }

    /**
     * Descriptor of a reflective listener holding information needed to create the listener
     * 
     * @author Andrea Vacondio
     * 
     */
    static class ReflectiveListenerDescriptor {

        private EventListener listenerAnnotation;
        private Method method;

        public ReflectiveListenerDescriptor(EventListener listenerAnnotation, Method method) {
            this.listenerAnnotation = listenerAnnotation;
            this.method = method;
        }

        public EventListener getListenerAnnotation() {
            return listenerAnnotation;
        }

        public Method getMethod() {
            return method;
        }
    }
}
