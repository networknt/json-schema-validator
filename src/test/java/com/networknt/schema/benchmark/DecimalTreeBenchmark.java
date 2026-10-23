package com.networknt.schema.benchmark;

import java.util.Random;
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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.serialization.NodeReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/** Compare native and precision-preserving reads, plus the complete validation path. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 8)
@Fork(2)
public class DecimalTreeBenchmark {
    @Param({"JSON", "YAML"})
    public String format;
    @Param({"integer", "plain", "machine", "decimal19", "decimal40"})
    public String payload;
    // 20,000 keeps every YAML payload below the default code-point limit.
    @Param({"20000"})
    public int count;
    private ObjectMapper nativeMapper;
    private NodeReader reader;
    private Schema schema;
    private InputFormat inputFormat;
    private String input;

    @Setup
    public void setup() {
        inputFormat = InputFormat.valueOf(format);
        nativeMapper = inputFormat == InputFormat.JSON ? JsonMapper.builder().build() : YAMLMapper.builder().build();
        reader = NodeReader.builder().build();
        schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema("true");
        Random random = new Random(1362);
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) text.append(',');
            switch (payload) {
            case "integer": text.append("123"); break;
            case "plain": text.append("123.25"); break;
            case "machine": text.append(Double.toString(Math.scalb(random.nextDouble() - 0.5, random.nextInt(40) - 20))); break;
            case "decimal19": text.append("0.1234567890123456789"); break;
            case "decimal40": text.append("0.1234567890123456789012345678901234567890"); break;
            default: throw new IllegalArgumentException(payload);
            }
        }
        input = text.append(']').toString();
    }

    @Benchmark
    public JsonNode nativeTree() throws Exception {
        return nativeMapper.readTree(input);
    }

    @Benchmark
    public JsonNode selectiveTree() throws Exception {
        return reader.readTree(input, inputFormat);
    }

    @Benchmark
    public Object validate() {
        return schema.validate(input, inputFormat);
    }
}
