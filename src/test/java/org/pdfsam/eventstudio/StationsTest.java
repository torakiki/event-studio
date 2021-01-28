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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 * 
 */
public class StationsTest {
    private Stations victim;

    @Before
    public void setUp() {
        victim = new Stations();
    }

    @Test
    public void get() {
        assertNotNull(victim.getStation("ChuckNorris"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notBlank() {
        victim.getStation(" ");
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
