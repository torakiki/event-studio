/* 
 * This file is part of the EventStudio source code
 * Created on 12/nov/2013
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

import static org.pdfsam.eventstudio.util.RequireUtils.requireNotNull;

import java.lang.ref.Reference;

/**
 * Holder for an instance.
 * 
 * @author Andrea Vacondio
 * @param <T>
 *            the type of the referent
 */
interface Entity<T> {
    /**
     * @return the instance or null if nothing is available
     */
    T get();

    /**
     * Holds an entity referenced using the input {@link Reference}
     * 
     * @author Andrea Vacondio
     * 
     * @param <T>
     */
    static class ReferencedEntity<T> implements Entity<T> {
        private Reference<T> reference;

        ReferencedEntity(Reference<T> reference) {
            requireNotNull(reference);
            this.reference = reference;
        }

        public T get() {
            return reference.get();
        }
    }

    /**
     * A strongly referenced entity
     * 
     * @author Andrea Vacondio
     * 
     * @param <T>
     */
    static class StrongEntity<T> implements Entity<T> {
        private T referent;

        StrongEntity(T referent) {
            this.referent = referent;
        }

        public T get() {
            return referent;
        }

    }
}
