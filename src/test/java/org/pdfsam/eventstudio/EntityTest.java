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
import static org.junit.Assert.assertNull;

import java.lang.ref.SoftReference;

import org.junit.Test;
import org.pdfsam.eventstudio.Entity.ReferencedEntity;
import org.pdfsam.eventstudio.Entity.StrongEntity;

/**
 * @author Andrea Vacondio
 *
 */
public class EntityTest {

    @Test
    public void testNull() {
        Entity<Object> victim = new StrongEntity<>(null);
        assertNull(victim.get());
    }

    @Test
    public void testStrong() {
        Object referent = new Object();
        Entity<Object> victim = new StrongEntity<>(referent);
        assertEquals(referent, victim.get());
    }

    @Test
    public void testReference() {
        Object referent = new Object();
        Entity<Object> victim = new ReferencedEntity<>(new SoftReference<>(referent));
        assertEquals(referent, victim.get());
    }
}
