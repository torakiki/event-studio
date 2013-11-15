/* 
 * This file is part of the EventStudio source code
 * Created on 12/nov/2013
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
package org.eventstudio;



/**
 * @author Andrea Vacondio
 * 
 */
public class TestEvents {

    public static class RootEvent {
    }

    public static class ChildEvent extends RootEvent {
    }
    public static class AnotherEvent {
    }

    public static class ImASupervisor implements Supervisor {

        public void inspect(Object event) {
            System.out.println("Inspected, all good!");
        }

    }

    public void test() {
        DefaultEventStudio studio = new DefaultEventStudio();
        studio.add(new Listener<RootEvent>() {
            public void onEvent(RootEvent event) {
                System.out.println("I listened you " + event);

            }
        }, "Radio1");
        studio.add(new Listener<ChildEvent>() {
            public void onEvent(ChildEvent event) {
                System.out.println("I listened you " + event);

            }
        }, "Radio1");
        studio.supervisor(new ImASupervisor(), "RadioSticazzi");
        studio.add(new Listener<AnotherEvent>() {

            public void onEvent(AnotherEvent event) {
                System.out.println("I'm weak");

            }
        }, "RadioSticazzi", 1, ReferenceStrength.WEAK);

        studio.add(new Listener<AnotherEvent>() {

            public void onEvent(AnotherEvent event) {
                System.out.println("And I'm soft");

            }
        }, "RadioSticazzi", -1, ReferenceStrength.SOFT);
        studio.add(new Listener<RootEvent>() {

            public void onEvent(RootEvent event) {
                System.out.println("I'm weak too");

            }
        }, "RadioBanana", 1, ReferenceStrength.WEAK);
        studio.broadcast(new RootEvent(), "RadioSticazzi");
        studio.broadcast(new ChildEvent(), "RadioSticazzi");
        studio.broadcast(new AnotherEvent(), "RadioSticazzi");
        studio.broadcast(new RootEvent(), "Radio1");
        studio.broadcast(new ChildEvent(), "Radio1");
        studio.broadcast(new AnotherEvent(), "Radio1");
        studio.broadcast(new RootEvent(), "RadioBanana");
        System.gc();
        studio.broadcast(new AnotherEvent(), "RadioSticazzi");
        studio.broadcast(new RootEvent(), "RadioBanana");
        studio.add(new Listener<RootEvent>() {

            public void onEvent(RootEvent event) {
                System.out.println("I'm weak too");

            }
        }, "RadioBanana", 1, ReferenceStrength.WEAK);
    }

}
