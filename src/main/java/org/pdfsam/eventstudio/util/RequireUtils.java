/* 
 * This file is part of the EventStudio source code
 * Created on 10/nov/2013
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
package org.pdfsam.eventstudio.util;
import static org.pdfsam.eventstudio.util.StringUtils.isBlank;
/**
 * Utility class with some helper method to check validity of input arguments
 * 
 * @author Andrea Vacondio
 * 
 */
public final class RequireUtils {

    private RequireUtils() {
        // hide
    }

    /**
     * Requires that the input string is not blank
     * 
     * @param victim
     * @throws IllegalArgumentException
     *             if the input is blank
     */
    public static void requireNotBlank(String victim) {
        if (isBlank(victim)) {
            throw new IllegalArgumentException("The input string cannot be blank");
        }
    }

    /**
     * Requires that the input argument is not null
     * 
     * @param victim
     * @throws IllegalArgumentException
     *             if the input is null
     */
    public static void requireNotNull(Object victim) {
        if (victim == null) {
            throw new IllegalArgumentException("The input object cannot be null");
        }
    }
}
