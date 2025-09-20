package com.networknt.schema.benchmark;

import java.util.concurrent.Callable;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class NetworkntBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        private Callable<Object> basic = new NetworkntBasicRunner();
    }

    @BenchmarkMode(Mode.Throughput)
    @Fork(2)
    @Warmup(iterations = 5, time = 5)
    @Measurement(iterations = 5, time = 5)
    @Benchmark
    public void basic(BenchmarkState state, Blackhole blackhole) throws Exception {
        blackhole.consume(state.basic.call());
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(NetworkntBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class).build();

        new Runner(opt).run();
    }

}
