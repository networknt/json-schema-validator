package com.networknt.schema.benchmark;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;

/** Isolates numeric comparison from parsing, including exact-decimal readers' values. */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class NumericUniqueItemsBenchmark {
    @Param({"decimal", "bigInteger", "double", "nestedDecimal"})
    public String kind;

    @Param({"false", "true"})
    public boolean duplicate;

    private Schema schema;
    private ArrayNode items;

    @Setup
    public void setup() {
        schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"uniqueItems\":true}");
        JsonNodeFactory nodes = JsonNodeFactory.instance;
        items = nodes.arrayNode();
        for (int i = 0; i < 1000; i++) {
            int value = duplicate && i == 999 ? 500 : i;
            BigDecimal decimal = BigDecimal.valueOf(value).add(new BigDecimal("0.2500"));
            if ("decimal".equals(kind)) {
                items.add(decimal);
            } else if ("bigInteger".equals(kind)) {
                items.add(BigInteger.ONE.shiftLeft(70).add(BigInteger.valueOf(value)));
            } else if ("double".equals(kind)) {
                items.add(value + 0.25);
            } else {
                items.add(nodes.objectNode().set("value", nodes.arrayNode().add(decimal)));
            }
        }
    }

    @Benchmark
    public Object validate() {
        return schema.validate(items);
    }
}
