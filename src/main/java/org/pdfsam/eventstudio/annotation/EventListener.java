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
package org.pdfsam.eventstudio.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pdfsam.eventstudio.ReferenceStrength;

/**
 * Annotated methods will be registered as Listener for the event in the method signature. Method signature must have a single parameter from which the event class will be
 * inferred. Multiple methods on the same pojo can be annotated.
 * 
 * @author Andrea Vacondio
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {

    /**
     * @return the priority for this listener, low numbers mean high priority.
     */
    int priority() default 0;

    /**
     * @return the station for this listener. If nothing is specified the {@link EventStation} annotated field or method will be used.
     */
    String station() default "";

    /**
     * @return the reference strength for this listener.
     */
    ReferenceStrength strength() default ReferenceStrength.STRONG;
}
