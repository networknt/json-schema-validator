package com.networknt.schema.benchmark;

import java.util.concurrent.Callable;

public class NetworkntPerf {
    public static void main(String[] args) throws Exception {
        Callable<Object> runner = new NetworkntBasicRunner();
        runner.call();
    }
}
