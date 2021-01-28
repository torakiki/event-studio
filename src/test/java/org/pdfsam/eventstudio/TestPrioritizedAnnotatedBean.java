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

import org.pdfsam.eventstudio.annotation.EventListener;

/**
 * @author Andrea Vacondio
 * 
 */
public class TestPrioritizedAnnotatedBean {

    @EventListener(priority = 1)
    public void first(Object event) {

    }

    @EventListener(priority = 2, strength = ReferenceStrength.SOFT)
    public void second(Object event) {

    }

}
