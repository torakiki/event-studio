/* 
 * This file is part of the EventStudio source code
 * Created on 20/nov/2013
 *  Copyright 2020 by Sober Lemur S.r.l. (info@pdfsam.org).
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
package org.pdfsam.eventstudio.shakedown;

import java.util.Random;

import org.pdfsam.eventstudio.ReferenceStrength;
import org.pdfsam.eventstudio.annotation.EventListener;
import org.pdfsam.eventstudio.annotation.EventStation;

/**
 * @author Andrea Vacondio
 *
 */
public class AnotherAnnotatedListener {
    @EventStation
    private static final String STATION = "station";

    @EventListener
    public void listener(MyEvent event) {
        //noop
    }

    @EventListener(station = "anotherStation", priority = 2, strength = ReferenceStrength.SOFT)
    public void anotherListener(OtherMyEvent event) {
        //noop
    }
}
