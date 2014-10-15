Method call limiter/dropper in Java 
==================================

## Build status
[![Build Status](https://buildhive.cloudbees.com/job/michalkolodziejski/job/java-dropper/badge/icon)](https://buildhive.cloudbees.com/job/michalkolodziejski/job/java-dropper/)
             
## Description
Utility for limiting method calls to given count (using annotations), count is based on method name(s).

## Usage
You have to:
- Annotate your method(s),
- create `Dropper` object 

### Scenario #1 - Using with calling methods
- call `checkThread` method at the beginning (this call increases the count)
- call `releaseThread` after the method finishes its work (this call decreases the count).

### Scenario #2 - Using with `Callable` object passed as `workToDo` argument
- wraps your code in `Callable` object's `call` method and pass it to `checkThread` method

**Caution**
In both scenario You should use `checkThread(Callable workToDo, Callable callback, Integer limitRate)` method. If You prefer to get an Exception when method call count excdeeds defined maximum allowed call count - pass `null` as the `callback` argument. Otherwise, the `call` method of passed `Callable` object would be executed, when You prefer to declare limit using annotations - provide `null` for `limitRate` argument.

### Annotating your methods

#### `LimitRate` annotation
You can put `LimitRate` annotation before your method, the argument is responsible for setting an maximum call limit count.
**Example**
```Java
@LimitRate(value = 5)
public doWork() {}
```

#### `LimitRateProperty` annotation
You can put `LimitRateProperty` annotation before your method, the is responsible for providing name of the `System property` (eg. set by `-dXXX` run argument or by `System.setProperty(key, value)` call).
**Example**
```Java
@LimitRateProperty(name = "doWorkMethodLimitRate")
public doWork() {}
```

**Caution**
You can annotate many methods using different values, this will cause to create separate counters for separate methods. This is also the case for overloading methods, eg.:
```Java
@LimitRate(value = 5) or @LimitRateProperty(name = "doWorkMethodLimitRate")
public doWork() {}
```

and

```Java
@LimitRate(value = 5) or @LimitRateProperty(name = "doWorkMethodLimitRate")
public doWork(int arg) {}
```

will (internally) create two separate counters for each of the methods.

This assumes that `System properties` context have defined an property of name `doWorkMethodLimitRate` prior to the method call (value defined in this property is evaluated at runtime when the method is called).

### Creating `Dropper` object 
Just call the default constructor.

**Example**
```Java
Dropper dropper = new Dropper();
```

### Calling `checkThread`

- when `workToDo` argument evaluates to `null`, the usage engages calling two methods - `checkThread` at the beginning of method, and `releaseThread` at the end.
- when `workToDo` argument evaluates to an valid `Callable` object, there is an automatic lock and release, and does not forces to call `releaseThread` method.
- when `callback` argument evaluates to `null`, the `DropCountExceededException` is used as a method to notify caller when call limit has been reached.

#### `Callable workToDo` argument
When the `Callable workToDo` argument evaluates to a valid object, `Dropper` wraps execution in `call` method, and provides automatic lock and release, and does not need to use `releaseThread` method.

**Example**
```Java
dropper.checkThread(/*workToDo*/ new Callable() {
    @Override
    public void call() {
        // do Your stuff here
    }
}, ..., ...);
```

#### `Callable callback` argument
When the `Callable callback` argument evaluates to a valid object, if count of method calls exceeds maximum method call count defined using annotation, executes `call` method of provided `Callable` object.

**Example**
```Java
dropper.checkThread(...
}, /*callback*/ new Callable() {
    @Override
    public void call() {
        // deal with this situation (eg. inform calling object)
    }
}, ...);
```

`Dropper` object will deal with thread count increase and decrease in this situation. If internal count for method call count exceeds maximum count defined for this method - an `call` method of provided `Callback` object will be executed.

#### `Integer limitRate` argument
When `limitRate` argument is `null`, the actual value of rate limit is calculated using a value of annotation `@LimitRate`, when this evaluates to `null`, the value is checked in `System property` defined as `name` attribute of `@LimitRateProperty` annotation. When this evaluates to `null`, the `DropCountExceededException` is thrown.

### How to add JAR to Maven M2 repo?
Execute following command:

```
mvn install:install-file -Dfile=dropper-1.0.jar -DgroupId=org.mkdev.ut -DartifactId=dropper -Dversion=1.0 -Dpackaging=jar
```

## History

2014-10-15
* refactored to use logger (logback/slf4j)
* added annotation for providing an system property to configure rate limit
* moved to version 1.0 from 1.0-SNAPSHOT

2014-10-01
* added possibility to define an rate limit when calling `checkThread`

2014-09-29
* Initial version.

## Problems?

[Submit an issue](https://github.com/michalkolodziejski/java-dropper/issues).
