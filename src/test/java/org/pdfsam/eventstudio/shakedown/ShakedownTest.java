/* 
 * This file is part of the EventStudio source code
 * Created on 20/nov/2013
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
package org.pdfsam.eventstudio.shakedown;

import static org.pdfsam.eventstudio.StaticStudio.eventStudio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pdfsam.eventstudio.DefaultEventStudio;
import org.pdfsam.eventstudio.ReferenceStrength;

/**
 * @author Andrea Vacondio
 * 
 */
public class ShakedownTest {

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<String> stations = new ArrayList<>();
    private Set<Callable<Void>> tasks = new HashSet<>();

    @Before
    public void setUp() {
        stations.add("station");
        stations.add("anotherStation");
        stations.add(DefaultEventStudio.HIDDEN_STATION);
        TestListener listener = new TestListener();
        for (int i = 0; i < 2000; i++) {
            tasks.add(new BroadcastTask(new MyEvent()));
            tasks.add(new BroadcastTask(new AnotherMyEvent()));
            tasks.add(new BroadcastTask(new OtherMyEvent()));
        }
        for (int i = 0; i < 200; i++) {
            tasks.add(new RemoveTask(listener));
            tasks.add(new AddTask(listener));
            tasks.add(new AddAnnotatedTask(new AnnotatedTestListener()));
            tasks.add(new AddAnnotatedTask(new AnotherAnnotatedListener()));
            tasks.add(new ClearTask());
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(5 * 60, TimeUnit.SECONDS);
    }

    @Test
    public void shakedown() throws InterruptedException {
        executor.invokeAll(tasks);
    }

    private class RemoveTask implements Callable<Void> {
        private TestListener listener;

        public RemoveTask(TestListener listener) {
            this.listener = listener;
        }

        public Void call() {
            eventStudio().remove(listener, stations.get(new Random().nextInt(3)));
            return null;
        }
    }

    private class AddTask implements Callable<Void> {
        private TestListener listener;

        public AddTask(TestListener listener) {
            this.listener = listener;
        }

        public Void call() {
            Random rand = new Random();
            eventStudio().add(listener, stations.get(rand.nextInt(3)), rand.nextInt(1000), ReferenceStrength.STRONG);
            return null;
        }
    }

    private class AddAnnotatedTask implements Callable<Void> {
        private Object bean;

        public AddAnnotatedTask(Object bean) {
            this.bean = bean;
        }

        public Void call() {
            eventStudio().addAnnotatedListeners(bean);
            return null;
        }
    }

    private class BroadcastTask implements Callable<Void> {
        private MyEvent event;

        public BroadcastTask(MyEvent event) {
            this.event = event;
        }

        public Void call() {
            eventStudio().broadcast(this.event, stations.get(new Random().nextInt(3)));
            return null;
        }
    }

    private class ClearTask implements Callable<Void> {
        public Void call() {
            eventStudio().clear(stations.get(new Random().nextInt(3)));
            return null;
        }
    }
}
