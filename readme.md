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

**Caution**
In this scenario You should use `checkThread()` or `checkThread(Callable callback)` method.

### Scenario #2 - Using with `Callable` object passed as `workToDo` argument
- encapsulate your code in `Callable` object's `call` method and pass it to `checkThread` method

**Caution**
In this scenario You should use `checkThread(Callable workToDo, Callable callback)` method. If You prefer to get an Exception when method call count excdeeds defined maximum allowed call count - pass `null` as the `callback` argument. Otherwise, the `call` method of passed `Callable` object would be executed.   

### Annotating your methods
Put the annotation `LimitRate` before your method:
**Example**
```Java
@LimitRate(value = 5)
public doWork() {}
```

**Caution**
You can annotate many methods using different values, this will cause to create separate counters for separate methods. This is also the case for overloading methods, eg.:
```Java
@LimitRate(value = 5)
public doWork() {}
```

and 

```Java
@LimitRate(value = 5)
public doWork(int arg) {}
```

will (internally) create two separate counters for each of the methods. 

### Creating `Dropper` object 
Just call the default constructor.

**Example**
```Java
Dropper dropper = new Dropper();
```

There are three possibilities to call `checkThread` method.

### `checkThread` without arguments call
The `checkThread` method when called without arguments, when the count of method calls exceeds maximum method call count defined using annotation, the `DropCountExceededException` is thrown.  

**Example**
```Java
dropper.checkThread();
```

If internal count for method call count exceeds maximum count defined for this method - an `DropCountExceededException` will be thrown.

### `checkThread` with `Callable callback`  argument
The `checkThread` method when called with an `Callback` argument, when the count of method calls exceeds maximum method call count defined using annotation, executes `call` method of provided `Callback` object. 

**Example**
```Java
dropper.checkThread(new Callable() {
    @Override
    public void call() {
        // deal with this situation (eg. inform calling object)               
    }
});
```

### `checkThread` with `Callable workToDo` and/or `Callable callback` argument
The `checkThread` method when called with an `Callback` argument, when the count of method calls exceeds maximum method call count defined using annotation, executes `call` method of provided `Callback` object. 

**Example with empty callback, eg. You prefer to get an DropCountExceededException**
```Java
dropper.checkThread(/*workToDo*/ new Callable() {
    @Override
    public void call() {
        // do Your stuff here
    }
}, /*callback*/ null);
```
`Dropper` object will deal with thread count increase and decrease in this situation. If internal count for method call count exceeds maximum count defined for this method - an `DropCountExceededException` will be thrown.

**Example with provided callback object to call instead of an DropCountExceededException**
```Java
dropper.checkThread(/*workToDo*/ new Callable() {
    @Override
    public void call() {
        // do Your stuff here
    }
}, /*callback*/ new Callable() {
    @Override
    public void call() {
        // deal with this situation (eg. inform calling object)  
    }
});
```

`Dropper` object will deal with thread count increase and decrease in this situation. If internal count for method call count exceeds maximum count defined for this method - an `call` method of provided `Callback` object will be executed.

## History

2014-09-29

* Initial version.

## Problems?

[Submit an issue](https://github.com/michalkolodziejski/java-dropper/issues).
