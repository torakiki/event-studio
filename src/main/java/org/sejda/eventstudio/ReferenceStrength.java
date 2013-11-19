/* 
 * This file is part of the EventStudio source code
 * Created on 09/nov/2013
 * Copyright 2013 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.eventstudio;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Possible reference strengths of the listeners
 * 
 * @author Andrea Vacondio
 * 
 */
public enum ReferenceStrength {
    STRONG {
        @Override
        <T> Entity<T> getReference(T referent) {
            return new Entity.StrongEntity<T>(referent);
        }
    },
    SOFT {
        @Override
        <T> Entity<T> getReference(T referent) {
            return new Entity.ReferencedEntity<T>(new SoftReference<T>(referent));
        }
    },
    WEAK {
        @Override
        <T> Entity<T> getReference(T referent) {
            return new Entity.ReferencedEntity<T>(new WeakReference<T>(referent));
        }
    };

    /**
     * 
     * @param referent
     * @return the referent wrapped with the appropriate {@link Entity} instance.
     */
    abstract <T> Entity<T> getReference(T referent);
}
