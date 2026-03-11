/*
 * This file is part of the EventStudio source code
 * Created on 14/nov/2013
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
package org.pdfsam.eventstudio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andrea Vacondio
 *
 */
public class EnvelopeTest {

    @Test
    public void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new Envelope(null));
    }

    @Test
    public void testGet() {
        Object message = new Object();
        Envelope victim = new Envelope(message);
        assertEquals(message, victim.getEvent());
    }

    @Test
    public void isNotified() {
        Object message = new Object();
        Envelope victim = new Envelope(message);
        assertFalse(victim.isNotified());
        victim.notified();
        assertTrue(victim.isNotified());
    }
}
