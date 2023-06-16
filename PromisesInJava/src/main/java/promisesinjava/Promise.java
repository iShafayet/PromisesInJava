package main.java.promisesinjava;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class Promise<T> {

    private static int uuidSeed = 0;

    private int uuid = 0;

    private T value;

    private boolean isResolved = false;
    private Runnable invokable;

    private ExecutorService invokeOnExecutor;

    private boolean isInvoked = false;

    public Promise() {
        uuid = uuidSeed++;
        debug("Created");
    }

    public Promise(ExecutorService executorService, Consumer<Consumer<T>> fn) {
        executorService.submit(() -> {
            fn.accept((results) -> {
                resolve(results);
            });
        });
    }

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

        debug("Invoking receiver with value: " + value);
        if (invokeOnExecutor != null) {
            invokeOnExecutor.submit(invokable);
        } else {
            invokable.run();
        }
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
        invokeIfSubscribedAndResolved();
    }

    public <U> Promise<U> then(ExecutorService executorService, Function<T, U> fn) {
        this.invokeOnExecutor = executorService;
        return then(fn);
    }

    public <U> Promise<U> thenPromise(ExecutorService executorService, Function<T, Promise<U>> fn) {
        this.invokeOnExecutor = executorService;
        return thenPromise(fn);
    }

    public void then(ExecutorService executorService, Consumer<T> fn) {
        this.invokeOnExecutor = executorService;
        then(fn);
    }


    @SafeVarargs
    public static <V> Promise<V[]> allAsArray(Class<V> vClass, Promise<V>... promises) {
        var aggregatePromise = new Promise<V[]>();

        var resultArray = (V[]) Array.newInstance(vClass, promises.length);
        AtomicInteger completionCount = new AtomicInteger();

        for (var i = 0; i < promises.length; i++) {
            int finalI = i;
            promises[i].then((value) -> {
                resultArray[finalI] = value;
                completionCount.addAndGet(1);
                if (completionCount.intValue() == promises.length) {
                    aggregatePromise.resolve(resultArray);
                }
            });
        }

        return aggregatePromise;
    }

    public static Promise<Void> all(Promise<?>... promises) {
        var aggregatePromise = new Promise<Void>();

        AtomicInteger completionCount = new AtomicInteger();
        Arrays.stream(promises).forEach(promise -> {
            promise.then((ignored) -> {
                completionCount.addAndGet(1);
                if (completionCount.intValue() == promises.length) {
                    aggregatePromise.resolve(null);
                }
            });
        });

        return aggregatePromise;
    }

    private void debug(String message) {
        var r = "R:" + (isResolved ? "1" : "0");
        var s = " S:" + (invokable != null ? "1" : "0");
        var i = " I:" + (isInvoked ? "1" : "0");
        var t = "[" + Thread.currentThread().getName() + "] ";
        System.out.println(t + "Promise #" + uuid + " [" + r + s + i + "]  " + message);
    }


}
