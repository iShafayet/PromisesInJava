package main.java;

import main.java.promisesinjava.Promise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class EntryPoint1 {

    private static Promise<Integer> incrementSlowly(int value) {
        var promise = new Promise<Integer>();
        Executors.newSingleThreadExecutor().submit(() -> {
            Thread.sleep(2000);
            promise.resolve(value + 1);
            return null;
        });
        return promise;
    }

    public static void main(String[] args) {

        log("Test Block 1 - Start");

        incrementSlowly(0).thenPromise((value) -> {
            log("A " + value);
            return incrementSlowly(value);
        }).thenPromise((value) -> {
            log("B " + value);
            return incrementSlowly(value);
        }).then((value) -> {
            log("C " + value);
            log("End of output");
        });

        log("Test Block 1 - End");

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


    }


    static void log(String message) {
        var t = "[" + Thread.currentThread().getName() + "] ";
        System.out.println(t + message);
    }

}
