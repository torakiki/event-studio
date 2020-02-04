
EventStudio
=====================
[![Build Status](https://travis-ci.org/torakiki/event-studio.png?branch=master)](https://travis-ci.org/torakiki/event-studio)
[![License](http://img.shields.io/badge/license-APLv2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

EventStudio is yet another pure Java event bus implementation providing pub/sub pattern with events queue capabilities for intra-jvm event communication.

Why?
---------
There are many different libraries with slightly different approaches implementing the pub/sub pattern but none of them was a perfect match for the problem I was trying to solve.
#### The problem
Working on a rich client software that can have multiple modules/plugins and:

+ I wanted to be able to send events to the whole application (every listener subscribed on a given event)
+ I wanted to be able to send events to a single module (every listener belonging to a given module and subscribed on a given event)
+ I wanted the event to be queued in case no listener was listening and delivered as soon as a listener registers.
+ I wanted listeners to be able to define at runtime what module they belong to. The idea was to create components (e.g. a SpecializedComboBox) listening for events (e.g. DisableEvent) and be able to reuse those components in different modules. I couldn't use the classical annotation approach with some topic/filter static value since every new instance of the component needed to be registered as listener for the module creating it.

#### Enter the Station
The solution I found was to mimic a network of radio stations. For those familiar with [RabbitMQ](http://www.rabbitmq.com/), the architecture is loosely resembling a [DirectExchange](http://www.rabbitmq.com/tutorials/tutorial-four-java.html) where the event class is the binding key.

![alt text](https://raw.github.com/torakiki/event-studio/master/src/graphics/event-studio.png "EventStudio diagram")

#### In a nutshell
`Listeners`s registers themselves on a `Station` for a specific event class and publishers can broadcast their events to a `Station`.
##### Additionally
* A `Supervisor` can be registered on a `Station` and it will be notified of every message going through the `Station` before handing it to the `Listeners`s.
* Publishers can broadcast to all the `Station`s
* Undelivered events are stored in a queue and delivered as soon as `Listener`s for those events register.


Features/Characteristics
---------
+ Minimal dependencies ([slf4j](http://www.slf4j.org/))
+ Thread safe
+ Fully unit tested
+ Fully documented
+ Super simple API
+ Any pojo can be an event
+ Programatically add/remove support to add/remove listeners to/from a given station
+ Annotation support to add listeners to a station with runtime name (any pojo can be a listener)
+ Broadcast to a given station or every station
+ `Supervisor` of a `Station` to be notified of every message going through the `Station`
+ Enqueue undelivered events and deliver them as soon as a listener registers
+ Strong/Soft/Weak listeners reference support
+ Prioritize listeners to enforce an execution order
+ Quick and dirty, super simple veto system
+ Helper methods to completely hide the `Station` abstraction and behave like a traditional pub/sub event bus
+ Strict event class matching (i.e. child events are not notified to listeners registered on parent events)
+ Singleton pattern provided but **not** enforced

#### What is not there
+ Async listeners execution

Maven coordinates
----------
``` 
<dependency>
  <groupId>org.pdfsam</groupId>
  <artifactId>eventstudio</artifactId>
  <version>2.0.0</version>
</dependency>
``` 

Examples
--------
##### Assuming the following:

``` 
    public class ParentEvent {
    }

    public class ChildEvent extends ParentEvent {
    }

    public class ParentListener implements Listener<ParentEvent> {
        public void onEvent(ParentEvent event) {
            System.out.println("Got it!");
        }
    }

    public class GenParentListener<T extends ParentEvent> implements Listener<T> {
        public void onEvent(T event) {
            System.out.println("Got it!");
        }
    }
```
### Add listeners
Add them without specifying a `Station` name for a traditional Pub/Sub pattern, hiding the `Station` abstraction or add them to a specific station.
``` 
    import static org.eventstudio.StaticStudio.eventStudio;
    .....
    
    public void initListenersTraditional() {
        eventStudio().add(new ParentListener());
        eventStudio().add(new ParentListener(), 1, ReferenceStrength.SOFT);
        eventStudio().add(ChildEvent.class, new GenParentListener<ChildEvent>());
        eventStudio().add(ChildEvent.class, new GenParentListener<ChildEvent>(), 
        0, ReferenceStrength.STRONG);
    }

    public void initListenersWithStation() {
        eventStudio().add(new ParentListener(), "MyStation");
        eventStudio().add(new ParentListener(), "MyStation", 1, 
        ReferenceStrength.WEAK);
        eventStudio().add(ChildEvent.class, new GenParentListener<ChildEvent>(), 
        "MyStation");
        eventStudio().add(ChildEvent.class, new GenParentListener<ChildEvent>(), 
        "MyStation", 0, ReferenceStrength.STRONG);
    }
``` 

### Add a supervisor
Define a supervisor:
``` 
    public class MyStationSupervisor implements Supervisor{
        public void inspect(Object event) {
            System.out.println("All good!");
        }
    }
```
And register it:
``` 
    public void initSupervisor() {
        eventStudio().supervisor(new MyStationSupervisor());
        eventStudio().supervisor(new MyStationSupervisor(), "MyStation");
    }
``` 
### Remove listeners
```
	ParentListener parentListener = new ParentListener();
	GenParentListener<ChildEvent> genericListener = new GenParentListener<ChildEvent>();
	....
    public void removeListeners() {
        eventStudio().remove(parentListener);
        eventStudio().remove(parentListener, "MyStation");
        eventStudio().remove(ChildEvent.class, genericListener, "MyStation");
        eventStudio().remove(ChildEvent.class, genericListener);
    }
``` 
### Broadcast
Broadcast events without specifying a `Station` name for a traditional Pub/Sub pattern, hiding the `Station` abstraction or broadcast them to a specific station or to all the `Station`s. 
``` 
     public void broadcast() {
        eventStudio().broadcast(new ParentEvent());
        eventStudio().broadcast(new ParentEvent(), "MyStation");
        eventStudio().broadcastToEveryStation(new ChildEvent());
    }
``` 
### Clear
Clear a `Station`, events won't be notified anymore.
``` 
    public void clear() {
        eventStudio().clear();
        eventStudio().clear( "MyStation");
    }
``` 
### Veto
Every `Listener` is allowed to veto the event it is listening for by throwing a `BroadcastInterruptionException`, broadcast of the event will be interrupted and lower priority `Listener`s won't receive it.
``` 
    public class VetoListener implements Listener<ParentEvent> {
        public void onEvent(ParentEvent event) {
        	if(some condition){
        		throw new BroadcastInterruptionException("You won't get any!");
        	}
            System.out.println("Got it!");
        }
    }
``` 
Annotation
--------
Any annotated pojo can be registered as a listener. Use the `@EventListener` annotation on a single parameter method.
``` 
 public class Foo{
        @EventListener
        private void onParent(ParentEvent event){
            System.out.println("Got it!");
        }
        
        @EventListener(priority=1, strength=ReferenceStrength.SOFT)
        private void onChild(ChildEvent event){
            System.out.println("Got it!");
        }
    }
``` 
and add the pojo to EventStudio. No `Station` is specified, traditional Pub/Sub.
``` 
 public void addAnnotated() {
    eventStudio().addAnnotatedListeners(new Foo());
 }
```
### Annotation+Station with runtime name
You can specify the `Station` name of an annotated method or the `Station` name for all the annotated methods of a pojo. `Station` names defined on annotated methods have precedence over the pojo `Station` definition. `Enum` and `String` values can be used as station name.
```
public class Foo {
        @EventStation
        private String station = "MyStation";

        @EventListener
        private void onParent(ParentEvent event) {
            System.out.println("Got it!");
        }

        @EventListener(station = "AnotherStation")
        private void onChild(ChildEvent event) {
            System.out.println("Got it!");
        }
    }
```
And finally, also methods returning `Enum` and `String` can be annotated to retrieve a runtime value for the `Station` name.
```
public class SpecializedComboBox {
        private String moduleName;

        public SpecializedComboBox(String moduleName) {
            this.moduleName = moduleName;
        }

        @EventStation
        private String stationName() {
            return this.moduleName;
        }

        @EventListener
        private void onDisable(DisableEvent event) {
            this.setEnabled(false);
        }
    }
```
and add the pojo to EventStudio:
``` 
 public void addAnnotated() {
    eventStudio().addAnnotatedListeners(new SpecializedComboBox("ModuleA"));
    eventStudio().addAnnotatedListeners(new SpecializedComboBox("ModuleB"));
 }
```
and broadcast event to the module:
``` 
 public void disableModuleA() {
    eventStudio().broadcast(new DisableEvent(), "ModuleA");
 }
```
### Annotation and inheritance
#### Station: annotated field
Any annotated field (public, protected, default (package) access, and private fields, but excludes inherited fields) with the `@EventStation` is discovered and used as `Station`.
#### Station: annotated method
Any annotated method (including protected, default (package) access, private, public declared by the class or interface and those inherited from superclasses and superinterfaces) with the `@EventStation` is discovered and used as `Station`.
#### Listeners: annotated methods
Any annotated method (including protected, default (package) access, private, public declared by the class or interface and those inherited from superclasses and superinterfaces) with the `@EventListener` is discovered and used as `Listener`.
#### Inheritance
In case of overridden methods no funky logic is applied trying to guess what the user intentions are, if you override an annotated method (either with `@EventStation` or `@EventListener`) and you want it to be discovered, it has to be annotated as well. In short if a method is overridden, annotations are not inherited. Consider making your annotated methods as final to avoid subclasses to override them causing unexpected behaviors.
```
 public class ParentListener {
    @EventListener
    public void listen(String event) {
        // do something
    }
 }

 public class ChildListener extends ParentListener {
    @Override
    public void listen(String event) {
        // do something else
    }
 }
 ....
 eventStudio().addAnnotatedListeners(new ChildListener());
```   
In the previous case no listener is discovered because the annotated method is overridden. 
#### Method invocation
Annotated methods are reflectively invoked using dynamic method lookup as documented in The Java Language Specification, Second Edition, section 15.12.4.4. See the [javadoc] (http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/reflect/Method.html#invoke%28java.lang.Object,%20java.lang.Object...%29).
