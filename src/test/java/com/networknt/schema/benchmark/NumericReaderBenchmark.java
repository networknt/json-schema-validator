/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.networknt.schema.InputFormat;
import com.networknt.schema.serialization.BasicNodeReader;
import com.networknt.schema.serialization.NodeReader;
import com.networknt.schema.serialization.JsonMapperFactory;

/** Measures parsing 1,000 short decimals, serialized doubles, or high-precision decimals. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class NumericReaderBenchmark {
    @Param({"short", "machine", "precise"})
    public String shape;

    private NodeReader reader;
    private String input;

    @Setup
    public void setup() {
        reader = BasicNodeReader.getInstance();
        StringBuilder values = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) {
                values.append(',');
            }
            if ("short".equals(shape)) {
                values.append(i).append(".25");
            } else if ("machine".equals(shape)) {
                values.append(Double.toString(Math.PI * (i + 1)));
            } else {
                values.append("9007199254740993.").append(i);
            }
        }
        input = values.append(']').toString();
    }

    @Benchmark
    public Object defaultReader() throws Exception {
        return reader.readTree(input, InputFormat.JSON);
    }

    /** Compatibility baseline; this reader may lose precision. */
    @Benchmark
    public Object publicDoubleReader() throws Exception {
        return JsonMapperFactory.getInstance().readTree(input);
    }
}
