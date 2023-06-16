package main.java;

import main.java.promisesinjava.Promise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntryPoint1 {

//    private static Promise<Integer> incrementSlowly(int value) {
//        var promise = new Promise<Integer>();
//        Executors.newSingleThreadExecutor().submit(() -> {
//            Thread.sleep(2000);
//            promise.resolve(value + 1);
//            return null;
//        });
//        return promise;
//    }

    private static Promise<Integer> incrementSlowly(int value) {
        return new Promise<>(Executors.newSingleThreadExecutor(), (resolveFn) -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            resolveFn.accept(value + 1);
        });
    }

    public static void main(String[] args) {

//        log("Test Block 1 - Start");
//
//        incrementSlowly(0).thenPromise((value) -> {
//            log("A " + value);
//            return incrementSlowly(value);
//        }).thenPromise((value) -> {
//            log("B " + value);
//            return incrementSlowly(value);
//        }).then((value) -> {
//            log("C " + value);
//            log("End of output");
//        });
//
//        log("Test Block 1 - End");


//        log("Test Block 2 - Start");
//
//        incrementSlowly(0).thenPromise((value) -> {
//            log("A " + value);
//            return incrementSlowly(value);
//        }).then((value) -> {
//            log("B " + value);
//            return value + 1;
//        }).then((value) -> {
//            log("C " + value);
//            log("End of output");
//        });
//
//        log("Test Block 2 - End");


//        log("Test Block 3 - Start");
//
//        var pool = Executors.newFixedThreadPool(4);
//
//        incrementSlowly(0).thenPromise(pool, (value) -> {
//            log("A " + value);
//            return incrementSlowly(value);
//        }).then(pool, (value) -> {
//            log("B " + value);
//            return value + 1;
//        }).then(pool, (value) -> {
//            log("C " + value);
//            log("End of output");
//        });
//
//        log("Test Block 3 - End");


        log("Test Block 4 - Start");

        var pool = Executors.newFixedThreadPool(8);

        Function<Integer, Promise<Integer>> innerIncrementSlowly = (value) -> {
            return new Promise<>(pool, (resolveFn) -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                resolveFn.accept(value + 1);
            });
        };

        innerIncrementSlowly.apply(0).thenPromise(pool, (value) -> {
            log("A " + value);
            return innerIncrementSlowly.apply(value);
        }).thenPromise(pool, (value) -> {
            log("B " + value);
            return innerIncrementSlowly.apply(value);
        }).then(pool, (value) -> {
            log("C " + value);
            log("End of output");
            System.exit(0);
        });

        log("Test Block 4 - End");

    }

    static void log(String message) {
        var t = "[" + Thread.currentThread().getName() + "] ";
        System.out.println(t + message);
    }

}
