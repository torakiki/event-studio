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
package org.pdfsam.eventstudio.util;

/**
 * Simple utility methods {@link String} related.
 * 
 * @author Andrea Vacondio
 * 
 */
public final class StringUtils {

    private StringUtils() {
        // utility
    }

    public static boolean isBlank(String input) {
        return input == null || input.trim().length() <= 0;
    }

    public static boolean isNotBlank(String input) {
        return !isBlank(input);
    }

    /**
     * @param input
     * @param defaultValue
     * @return the unchanged input if it's not blank, the default value otherwise
     */
    public static String defaultString(String input, String defaultValue) {
        return StringUtils.isBlank(input) ? defaultValue : input;
    }
}
