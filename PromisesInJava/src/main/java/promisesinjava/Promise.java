package main.java.promisesinjava;

import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class Promise<T> {

    private static int uuidSeed = 0;

    private int uuid = 0;

    private T value;

    private boolean isResolved = false;
    private Runnable invokable;

    private boolean isInvoked = false;

    public Promise() {
        uuid = uuidSeed++;
        debug("Created");
    }

//    public Promise(Consumer<Consumer<T>> fn) {
//        Executors.newCachedThreadPool().submit(() -> {
//            fn.accept((results) -> {
//                value = results;
//                _isResolved = true;
//                if (_invokable != null) {
//                    _invokable.apply(value);
//                }
//            });
//            return null;
//        });
//    }

    public void resolve(T results) {
        if (isResolved) {
            throw new RuntimeException("The promise was already resolved");
        }
        value = results;
        isResolved = true;
        invokeIfSubscribedAndResolved();
    }

    private void invokeIfSubscribedAndResolved() {
        if (isInvoked) {
            throw new RuntimeException("The promise was already invoked");
        }
        if (invokable == null) {
            return;
        }
        if (!isResolved) {
            return;
        }
        isInvoked = true;
        invokable.run();
//        Executors.newSingleThreadExecutor().submit(invokable);
    }

    public <U> Promise<U> then(Function<T, U> fn) {
        var nextPromise = new Promise<U>();
        invokable = () -> {
            var nextValue = fn.apply(value);
            nextPromise.resolve(nextValue);
        };
        invokeIfSubscribedAndResolved();
        return nextPromise;
    }

    public <U> Promise<U> thenPromise(Function<T, Promise<U>> fn) {
        var nextPromise = new Promise<U>();
        invokable = () -> {
            var nextValue = fn.apply(value);

            debug("Subscribing to nested Promise #" + (nextValue).uuid);
            (nextValue).then((nextNextValue) -> {
                debug("Value from nested promise: " + nextNextValue);
                nextPromise.resolve(nextNextValue);
            });
        };
        invokeIfSubscribedAndResolved();
        return nextPromise;
    }

    public void then(Consumer<T> fn) {
        invokable = () -> {
            fn.accept(value);
        };
    }

//    public <V extends Promise> Promise<V> then(Function<T, V> fn) {
//        var nextPromise = new Promise<V>();
//        invokable = ()->{
//            var nextValue  = fn.apply(value);
//            nextPromise.resolve(nextValue);
//        };
//        return nextPromise;
//    }

    private void debug(String message) {
        var r = " R:" + (isResolved ? "1" : "0");
        var s = " S:" + (invokable != null ? "1" : "0");
        var i = " I:" + (isInvoked ? "1" : "0");
        var t = "[" + Thread.currentThread().getName() + "] ";
        System.out.println(t + "Promise #" + uuid + ": " + r + s + i + "  " + message);
    }


}
