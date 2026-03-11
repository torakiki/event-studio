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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Andrea Vacondio
 *
 */
public class StationsTest {
    private Stations victim;

    @BeforeEach
    public void setUp() {
        victim = new Stations();
    }

    @Test
    public void get() {
        assertNotNull(victim.getStation("ChuckNorris"));
    }

    @Test
    public void notBlank() {
        assertThrows(IllegalArgumentException.class, () -> victim.getStation(" "));
    }

    @Test
    public void same() {
        Station one = victim.getStation("ChuckNorris");
        Station two = victim.getStation("ChuckNorris");
        assertEquals(one, two);
    }

    @Test
    public void clear() {
        victim.getStation("ChuckNorris");
        assertEquals(1, victim.getStations().size());
        victim.clear("ChuckNorris");
        assertEquals(0, victim.getStations().size());
    }
}
