/*
 * Copyright (c) 2024 the original author or authors.
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
package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.keyword.MultipleOfValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;
import com.networknt.schema.serialization.NodeReader;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.BigIntegerNode;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.FloatNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Test MultipleOfValidator validator.
 */
class MultipleOfValidatorTest {
    String schemaData = "{" +
            "  \"type\": \"object\"," +
            "  \"properties\": {" +
            "    \"value1\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }," +
            "    \"value2\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }," +
            "    \"value3\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }" +
            "  }" +
            "}";

    @Test
    void test() {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\"value1\":123.892,\"value2\":123456.2934,\"value3\":123.123}";
        String validData = "{\"value1\":123.89,\"value2\":123456,\"value3\":123.010}";
        
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(3, messages.stream().filter(m -> "multipleOf".equals(m.getKeyword())).count());
        
        messages = schema.validate(validData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void testTypeLoose() {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        
        String inputData = "{\"value1\":\"123.892\",\"value2\":\"123456.2934\",\"value3\":123.123}";
        String validTypeLooseInputData = "{\"value1\":\"123.89\",\"value2\":\"123456.29\",\"value3\":123.12}";
        
        // Without type loose this has 2 type and 1 multipleOf errors
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(2, messages.stream().filter(m -> "type".equals(m.getKeyword())).count());
        assertEquals(1, messages.stream().filter(m -> "multipleOf".equals(m.getKeyword())).count());
        
        // 2 type errors
        messages = schema.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(2, messages.size());
        assertEquals(2, messages.stream().filter(m -> "type".equals(m.getKeyword())).count());
        
        // With type loose this has 3 multipleOf errors
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().typeLoose(true).build();
        factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder.schemaRegistryConfig(config));
        Schema typeLoose = factory.getSchema(schemaData);
        messages = typeLoose.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(3, messages.stream().filter(m -> "multipleOf".equals(m.getKeyword())).count());
        
        // No errors
        messages = typeLoose.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void messageFormatPrecision() {
        String schemaData = "{ \"type\": \"object\", \"properties\": { \"value1\": { \"type\": \"number\", \"multipleOf\": 0.00001 } } }";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\"value1\":123.000001}";

        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals("must be multiple of 0.00001", messages.get(0).getMessage());
    }

    @Test
    void nonFinite() {
        String schemaData = "{\r\n"
                + "  \"multipleOf\": 10\r\n"
                + "}";
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4,
                builder -> builder.nodeReader(NodeReader.builder()
                        .jsonMapper(JsonMapper.builder().enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build())
                        .build()))
                .getSchema(schemaData);
        List<Error> errors = schema.validate("NaN", InputFormat.JSON);
        assertEquals(0, errors.size());
        errors = schema.validate("Infinity", InputFormat.JSON);
        assertEquals(0, errors.size());
        errors = schema.validate("-Infinity", InputFormat.JSON);
        assertEquals(0, errors.size());
    }

    @Test
    void nonFiniteSchemaShouldBeIgnored() {
        String schemaData = "{\r\n"
                + "  \"multipleOf\": NaN\r\n"
                + "}";
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4,
                builder -> builder.nodeReader(NodeReader.builder()
                        .jsonMapper(JsonMapper.builder().enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build())
                        .build()))
                .getSchema(schemaData);
        List<Error> errors = schema.validate("NaN", InputFormat.JSON);
        assertEquals(0, errors.size());
    }

    @Test
    void stringSchemaShouldBeIgnored() {
        String schemaData = "{\r\n"
                + "  \"multipleOf\": \"test\"\r\n"
                + "}";
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4).getSchema(schemaData);
        List<Error> errors = schema.validate("10", InputFormat.JSON);
        assertEquals(0, errors.size());
    }

    @ParameterizedTest
    @CsvSource({
            "2, 9007199254740993, 1",
            "3, 9007199254740993, 0",
            "2, -9007199254740993, 1",
            "2, 9223372036854775809, 1",
            "2, 9007199254740992, 0",
            "2, 9007199254740994, 0",
            "2, 9223372036854775810, 0",
            "9007199254740993, 9007199254740992, 1",
            "9007199254740993, 9007199254740993, 0",
            "9007199254740993, 18014398509481986, 0",
            "9223372036854775809, 9223372036854775808, 1",
            "9223372036854775809, 9223372036854775809, 0"
    })
    void largeIntegers(String divisor, String dividend, int expectedErrors) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + divisor + "}");
        assertEquals(expectedErrors, schema.validate(dividend, InputFormat.JSON).size());
    }

    @ParameterizedTest
    @CsvSource({
            "9007199254740993, 9007199254740993.0, 0",
            "9007199254740993.0, 9007199254740993, 0",
            "9007199254740993, 18014398509481986.0, 0",
            "9007199254740993.0, 18014398509481986, 0",
            "9007199254740993, -9007199254740993.0, 0",
            "9007199254740993.0, -9007199254740993, 0",
            "-9007199254740993, 9007199254740993.0, 0",
            "-9007199254740993.0, 9007199254740993, 0",
            "9007199254740993, 9007199254740996.0, 1",
            "9007199254740993.0, 9007199254740996, 1",
            "9007199254740993, 0.0, 0",
            "9007199254740993.0, 0, 0",
            "3, 3.0, 0",
            "3.0, 3, 0",
            "3, 4.0, 1",
            "3.0, 4, 1",
            "3, 3.0000000000000004, 1",
            "3.0000000000000004, 3, 1"
    })
    void mixedFloatingPointOperandsKeepBaseBehavior(String divisor, String dividend, int expectedErrors) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + divisor + "}");
        assertEquals(expectedErrors, schema.validate(dividend, InputFormat.JSON).size());
    }

    @Test
    void mixedFloatingPointOperandsRetainExistingRounding() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        // Preserve the base branch's rounded results for mixed floating-point inputs.
        assertEquals(0, registry.getSchema("{\"multipleOf\":2.0}")
                .validate("9007199254740993", InputFormat.JSON).size());
        assertEquals(0, registry.getSchema("{\"multipleOf\":9007199254740993}")
                .validate("9007199254740992.0", InputFormat.JSON).size());
    }

    @Test
    void mixedFloatNodesKeepBaseBehavior() {
        long value = 9007199254740993L;
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        assertEquals(0, registry.getSchema("{\"multipleOf\":" + value + "}")
                .validate(FloatNode.valueOf((float) value)).size());
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", FloatNode.valueOf((float) value));
        assertEquals(0, registry.getSchema(schemaNode).validate(LongNode.valueOf(value)).size());
    }

    @ParameterizedTest
    @CsvSource({
            "0.1, 0.3, 0",
            "0.1, 0.31, 1",
            "1e-400, 1.5e-400, 1",
            "1e-400, 2e-400, 0",
            "1e400, 1, 1",
            "1e400, 2e400, 0",
            "2, 1e400, 0"
    })
    void exactDecimals(String divisor, String dividend, int expectedErrors) {
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(divisor)));
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema(schemaNode);
        assertEquals(expectedErrors, schema.validate(DecimalNode.valueOf(new BigDecimal(dividend))).size());
    }

    @Test
    void integersBeyondDoubleRange() {
        BigInteger divisor = BigInteger.TEN.pow(400).add(BigInteger.ONE);
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", BigIntegerNode.valueOf(divisor));
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = registry.getSchema(schemaNode);
        assertEquals(1, schema.validate(BigIntegerNode.valueOf(divisor.subtract(BigInteger.ONE))).size());
        assertEquals(0, schema.validate(BigIntegerNode.valueOf(divisor)).size());
        assertEquals(0, schema.validate(BigIntegerNode.valueOf(divisor.multiply(BigInteger.valueOf(2)))).size());
        assertEquals(0, schema.validate(BigIntegerNode.valueOf(divisor.negate())).size());

        Schema even = registry.getSchema("{\"multipleOf\":2}");
        assertEquals(1, even.validate(BigIntegerNode.valueOf(divisor)).size());
        assertEquals(1, even.validate(BigIntegerNode.valueOf(divisor.negate())).size());
        assertEquals(0, even.validate(BigIntegerNode.valueOf(divisor.add(BigInteger.ONE))).size());
    }

    @ParameterizedTest
    @CsvSource({
            "2, 9007199254740993.0, 1",
            "3, 9007199254740993.0, 0",
            "9007199254740993.0, 9007199254740992.0, 1",
            "9007199254740993.0, 18014398509481986.0, 0",
            "1e-400, 1.5e-400, 1",
            "1e-400, 2e-400, 0",
            "1e400, 1, 1",
            "1e400, 2e400, 0"
    })
    void exactDecimalsFromReader(String divisor, String dividend, int expectedErrors) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(NodeReader.builder()
                        .jsonMapper(JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build())
                        .build()))
                .getSchema("{\"multipleOf\":" + divisor + "}");
        assertEquals(expectedErrors, schema.validate(dividend, InputFormat.JSON).size());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 3, must be multiple of 1E+2",
            "100.0, 3, must be multiple of 1E+2",
            "100.00, 3, must be multiple of 1E+2",
            "2.0, 3, must be multiple of 2",
            "9007199254740993, 2, must be multiple of 9007199254740993"
    })
    void exactDivisorMessage(String divisor, String dividend, String expectedMessage) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + divisor + "}");
        assertEquals(expectedMessage, schema.validate(dividend, InputFormat.JSON).get(0).getMessage());
    }

    @ParameterizedTest
    @CsvSource({"2.0, 2", "100.00, 1E+2", "0.0000100, 0.00001",
            "9007199254740993.0, 9007199254740993", "1e400, 1E+400"})
    void exactDecimalDivisorMessage(String divisor, String expectedDivisor) {
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(divisor)));
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema(schemaNode);
        assertEquals("must be multiple of " + expectedDivisor,
                schema.validate(DecimalNode.valueOf(new BigDecimal("0.0000001"))).get(0).getMessage());
    }

    @Test
    void divisorMessageAtMinimumScale() {
        BigDecimal divisor = new BigDecimal(BigInteger.TEN, Integer.MIN_VALUE);
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(divisor));
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema(schemaNode);
        assertEquals("must be multiple of " + divisor, schema.validate(IntNode.valueOf(1)).get(0).getMessage());
    }

    @Test
    void mixedFloatingPointOperandsBeyondDoubleRange() {
        BigInteger value = BigInteger.TEN.pow(400).add(BigInteger.ONE);
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", BigIntegerNode.valueOf(value));
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = registry.getSchema(schemaNode);
        assertEquals(1, schema.validate(DoubleNode.valueOf(1.0)).size());
        assertEquals(0, schema.validate(DoubleNode.valueOf(0.0)).size());
        Schema even = registry.getSchema("{\"multipleOf\":2.0}");
        assertEquals(1, even.validate(BigIntegerNode.valueOf(value)).size());
        assertEquals(0, even.validate(BigIntegerNode.valueOf(value.add(BigInteger.ONE))).size());
    }

    @ParameterizedTest
    @CsvSource({"2.0, 2, 0", "2.0, 3, 1", "100.00, 200, 0", "100.00, 201, 1",
            "2.0, -9223372036854775808, 0", "3.0, -9223372036854775808, 1"})
    void inheritedConversionsWithIntegralDecimalDivisor(String divisor, long dividend, int expectedErrors) {
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":2}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                DecimalNode.valueOf(new BigDecimal(divisor)), parent, parent.getSchemaContext()) { };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode node = LongNode.valueOf(dividend);
        validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
        assertEquals(expectedErrors, context.getErrors().size());
    }

    @Test
    void largeScaleDifferences() throws IOException, InterruptedException {
        runInFork("largeScaleDifferences", 30);
    }

    @Test
    void largeExponentsFromReaderAndLooseType() throws IOException, InterruptedException {
        runInFork("largeExponentsFromReaderAndLooseType", 30);
    }

    private static void runInFork(String testName, long timeoutSeconds) throws IOException, InterruptedException {
        // BigInteger arithmetic does not respond to interrupts. Isolate these extreme
        // inputs so a regression can be stopped without leaving work in the test JVM.
        Path output = Files.createTempFile("multiple-of-", ".log");
        try {
            String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
            String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();
            String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
            List<String> command = new ArrayList<>();
            command.add(java);
            command.addAll(forkJvmArguments(ManagementFactory.getRuntimeMXBean().getInputArguments()));
            command.addAll(List.of("-Xmx128m", "-cp", classPath, MultipleOfValidatorTest.class.getName(), testName));
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true).redirectOutput(output.toFile()).start();
            boolean finished;
            try {
                finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                    process.waitFor(5, TimeUnit.SECONDS);
                }
            }
            assertTrue(finished, "Timed out: " + testName);
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                assertEquals(0, exitCode, Files.readString(output));
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private static List<String> forkJvmArguments(List<String> arguments) {
        List<String> inherited = new ArrayList<>();
        for (String argument : arguments) {
            // Keep this project's coverage agent and locale, not process-specific
            // settings such as the debugger port or the parent JVM's heap sizes.
            if (argument.startsWith("-javaagent:") && argument.contains("org.jacoco.agent")
                    || argument.startsWith("-Duser.language=") || argument.startsWith("-Duser.region=")
                    || argument.startsWith("-Duser.country=") || argument.startsWith("-Duser.variant=")) {
                inherited.add(argument);
            }
        }
        return inherited;
    }

    @Test
    void forkInheritsCoverageAndLocaleWithoutProcessSpecificOptions() {
        String coverage = "-javaagent:/test/org.jacoco.agent.jar=destfile=/test/jacoco.exec";
        assertEquals(List.of(coverage, "-Duser.language=en", "-Duser.region=GB"),
                forkJvmArguments(List.of("-Xms256m", "-Xmx512m",
                        "-agentlib:jdwp=transport=dt_socket,server=y,address=127.0.0.1:5005",
                        coverage, "-Duser.language=en", "-Duser.region=GB")));
    }

    public static void main(String[] args) {
        switch (args[0]) {
            case "largeScaleDifferences":
                checkLargeScaleDifferences();
                break;
            case "largeExponentsFromReaderAndLooseType":
                checkLargeExponentsFromReaderAndLooseType();
                break;
            default:
                throw new IllegalArgumentException("Unknown test: " + args[0]);
        }
    }

    private static void checkLargeScaleDifferences() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(BigInteger.valueOf(3), Integer.MAX_VALUE)));
        Schema tinyDivisor = registry.getSchema(schemaNode);
        assertEquals(0, tinyDivisor.validate(DecimalNode.valueOf(new BigDecimal(BigInteger.valueOf(3), Integer.MIN_VALUE))).size());
        assertEquals(1, tinyDivisor.validate(DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE))).size());
        assertEquals(0, tinyDivisor.validate(DecimalNode.valueOf(BigDecimal.ZERO)).size());

        schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE)));
        Schema hugeDivisor = registry.getSchema(schemaNode);
        assertEquals(1, hugeDivisor.validate(DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE))).size());
    }

    private static void checkLargeExponentsFromReaderAndLooseType() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(NodeReader.builder()
                        .jsonMapper(JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build())
                        .build()).schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
        Schema tinyDivisor = registry.getSchema("{\"multipleOf\":1e-20000000}");
        assertEquals(0, tinyDivisor.validate("1", InputFormat.JSON).size());
        Schema thirds = registry.getSchema("{\"multipleOf\":3}");
        assertEquals(1, thirds.validate("1e5000000", InputFormat.JSON).size());
        assertEquals(1, thirds.validate("\"1e5000000\"", InputFormat.JSON).size());
        assertEquals(0, thirds.validate("\"3e5000000\"", InputFormat.JSON).size());
    }

    @Test
    void matchesDecimalRemainder() {
        Random random = new Random(1286);
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (int i = 0; i < 100; i++) {
            BigDecimal divisor = BigDecimal.valueOf(1 + random.nextInt(1000), random.nextInt(25) - 12);
            ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
            schemaNode.set("multipleOf", DecimalNode.valueOf(divisor));
            Schema schema = registry.getSchema(schemaNode);
            for (int j = 0; j < 20; j++) {
                BigDecimal dividend = BigDecimal.valueOf(random.nextInt(2001) - 1000, random.nextInt(25) - 12);
                int expectedErrors = dividend.remainder(divisor).signum() == 0 ? 0 : 1;
                assertEquals(expectedErrors, schema.validate(DecimalNode.valueOf(dividend)).size(),
                        dividend + " multipleOf " + divisor);
            }
            BigDecimal multiple = divisor.multiply(BigDecimal.valueOf(i - 50));
            assertEquals(0, schema.validate(DecimalNode.valueOf(multiple)).size());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "2, 0, 0",
            "2, -9223372036854775808, 0",
            "3, -9223372036854775808, 1",
            "9223372036854775807, 9223372036854775807, 0",
            "9223372036854775807, -9223372036854775807, 0",
            "9223372036854775807, -9223372036854775808, 1",
            "1, -9223372036854775808, 0",
            "2, 2147483647, 1"
    })
    void integralFastPath(String divisor, String dividend, int expectedErrors) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + divisor + "}");
        assertEquals(expectedErrors, schema.validate(dividend, InputFormat.JSON).size());
    }

    @ParameterizedTest
    @CsvSource({"18, 2, 0", "18, 3, 1", "19, 2, 0", "19, 3, 1"})
    void smallAndLargePositiveScaleDifferences(int scale, int coefficient, int expectedErrors) {
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(BigInteger.valueOf(coefficient), scale)));
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema(schemaNode);
        assertEquals(expectedErrors, schema.validate(IntNode.valueOf(1)).size());
        assertEquals(expectedErrors, schema.validate(IntNode.valueOf(-1)).size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "9007199254740993", "1e400", "9223372036854775808",
            "18446744073709551616", "-9223372036854775809"})
    void convertedDividendIsUsedBeforeIntegralFastPath(String convertedValue) {
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":3}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(3), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDividend(JsonNode node) {
                return new BigDecimal(convertedValue);
            }
        };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode node = IntNode.valueOf(3);
        validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
        int expectedErrors = new BigDecimal(convertedValue).remainder(BigDecimal.valueOf(3)).signum() == 0 ? 0 : 1;
        assertEquals(expectedErrors, context.getErrors().size());
    }

    @Test
    void conversionOverridesAreNotBypassed() {
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema("{\"multipleOf\":2}");
        MultipleOfValidator divisorOverride = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(2), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDivisor(JsonNode node) {
                return BigDecimal.valueOf(3);
            }
        };
        ExecutionContext context = parent.createExecutionContext();
        NodePath instanceLocation = new NodePath(PathType.JSON_POINTER);
        divisorOverride.validate(context, IntNode.valueOf(3), IntNode.valueOf(3), instanceLocation);
        assertTrue(context.getErrors().isEmpty());
        divisorOverride.validate(context, IntNode.valueOf(2), IntNode.valueOf(2), instanceLocation);
        assertEquals("must be multiple of 3", context.getErrors().get(0).getMessage());

        final boolean[] called = {false};
        MultipleOfValidator dividendOverride = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(2), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDividend(JsonNode node) {
                called[0] = true;
                return BigDecimal.valueOf(6);
            }
        };
        context = parent.createExecutionContext();
        dividendOverride.validate(context, IntNode.valueOf(3), IntNode.valueOf(3), instanceLocation);
        assertTrue(called[0]);
        assertTrue(context.getErrors().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"0, 1, 0", "0.0, 1, 0", "-0.0, 1, 0", "-2, 4, 0", "-2, 3, 1", "-0.1, 0.3, 0", "-0.1, 0.31, 1"})
    void preservesNonPositiveDivisorBehavior(String divisor, String dividend, int expectedErrors) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + divisor + "}");
        assertEquals(expectedErrors, schema.validate(dividend, InputFormat.JSON).size());
    }

    @Test
    void preservesLooseExponentOverflow() {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()))
                .getSchema("{\"multipleOf\":2}");
        assertThrows(NumberFormatException.class,
                () -> schema.validate("\"1e99999999999\"", InputFormat.JSON));
    }

    @Test
    void preservesFloatConversion() {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":0.1}");
        // FloatNode is still widened to double, as on the base branch.
        assertEquals(1, schema.validate(FloatNode.valueOf(0.3f)).size());
    }

    @Test
    void mixedFloatingPointConversionOverridesAreNotBypassed() {
        long value = 9007199254740993L;
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + value + "}");
        MultipleOfValidator divisorOverride = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(2), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDivisor(JsonNode node) {
                return BigDecimal.valueOf(value);
            }
        };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode roundedInput = DoubleNode.valueOf((double) value);
        divisorOverride.validate(context, roundedInput, roundedInput, new NodePath(PathType.JSON_POINTER));
        assertEquals(1, context.getErrors().size());

        MultipleOfValidator dividendOverride = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                LongNode.valueOf(value), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDividend(JsonNode node) {
                return BigDecimal.valueOf(value);
            }
        };
        context = parent.createExecutionContext();
        dividendOverride.validate(context, roundedInput, roundedInput, new NodePath(PathType.JSON_POINTER));
        // The floating-point input selects the rounded schema divisor.
        assertEquals(1, context.getErrors().size());

        MultipleOfValidator floatingSchemaOverride = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                DoubleNode.valueOf(2.0), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDivisor(JsonNode node) {
                return BigDecimal.valueOf(value);
            }
        };
        context = parent.createExecutionContext();
        JsonNode exactInput = LongNode.valueOf(value);
        floatingSchemaOverride.validate(context, exactInput, exactInput, new NodePath(PathType.JSON_POINTER));
        // The custom divisor is used with the exact integer returned by the dividend hook.
        assertTrue(context.getErrors().isEmpty());

        MultipleOfValidator inherited = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                LongNode.valueOf(value), parent, parent.getSchemaContext()) { };
        context = parent.createExecutionContext();
        inherited.validate(context, roundedInput, roundedInput, new NodePath(PathType.JSON_POINTER));
        assertTrue(context.getErrors().isEmpty());
    }

    @Test
    void exactDecimalDivisorsNeedNoScaleNormalization() {
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(BigInteger.TEN, Integer.MIN_VALUE)));
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaNode);
        assertEquals(0, schema.validate(DecimalNode.valueOf(new BigDecimal(BigInteger.valueOf(20), Integer.MIN_VALUE))).size());
        assertEquals(1, schema.validate(DecimalNode.valueOf(BigDecimal.ONE)).size());
    }

    @ParameterizedTest
    @CsvSource({
            "abs, -9007199254740992, 0", "abs, -9007199254740996, 1",
            "negate, 9007199254740992, 0", "negate, -9007199254740992, 0",
            "double, 9007199254740992, 0", "double, -9007199254740992, 0",
            "shift, 9007199254740992, 0", "shift, -9007199254740992, 0"
    })
    void transformedDividendKeepsBaseBehavior(String operation, double value, int expectedErrors) {
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":9007199254740993}");
        int[] calls = { 0 };
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                LongNode.valueOf(9007199254740993L), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDividend(JsonNode node) {
                calls[0]++;
                BigDecimal dividend = super.getDividend(node);
                switch (operation) {
                    case "abs": return dividend.abs();
                    case "negate": return dividend.negate();
                    case "double": return dividend.multiply(BigDecimal.valueOf(2));
                    case "shift": return dividend.scaleByPowerOfTen(1);
                    default: throw new AssertionError(operation);
                }
            }
        };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode node = DoubleNode.valueOf(value);
        validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
        assertEquals(expectedErrors, context.getErrors().size());
        assertEquals(1, calls[0]);
    }

    @Test
    void usesConversionInheritedFromIntermediateSubclass() {
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":9007199254740993}");
        class AbsoluteDividendValidator extends MultipleOfValidator {
            AbsoluteDividendValidator() {
                super(SchemaLocation.of("#/multipleOf"), LongNode.valueOf(9007199254740993L),
                        parent, parent.getSchemaContext());
            }

            @Override
            protected BigDecimal getDividend(JsonNode node) {
                return super.getDividend(node).abs();
            }
        }
        MultipleOfValidator validator = new AbsoluteDividendValidator() { };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode node = DoubleNode.valueOf(-9007199254740992d);
        validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
        assertTrue(context.getErrors().isEmpty());
    }

    @Test
    void subclassWithoutConversionOverridesRetainsIntegerPrecision() {
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":9007199254740993}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                LongNode.valueOf(9007199254740993L), parent, parent.getSchemaContext()) { };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode node = LongNode.valueOf(9007199254740992L);
        validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
        assertEquals(1, context.getErrors().size());
    }

    @Test
    void delegatingConversionRetainsIntegerPrecision() {
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":9007199254740993}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                LongNode.valueOf(9007199254740993L), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDivisor(JsonNode node) {
                return super.getDivisor(node);
            }

            @Override
            protected BigDecimal getDividend(JsonNode node) {
                return super.getDividend(node);
            }
        };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode node = LongNode.valueOf(9007199254740992L);
        validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
        assertEquals(1, context.getErrors().size());
    }

    @Test
    void mixedInputMessageUsesTheDivisorUsedForValidation() {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":9007199254740993}");
        assertEquals("must be multiple of 9007199254740992",
                schema.validate("27021597764222979.0", InputFormat.JSON).get(0).getMessage());
        assertEquals("must be multiple of 9007199254740993",
                schema.validate("2", InputFormat.JSON).get(0).getMessage());
    }

    @Test
    void divisorScaleDoesNotChangeMixedInputRounding() {
        long value = 9007199254740993L;
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + value + "}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                LongNode.valueOf(value), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDivisor(JsonNode node) {
                return super.getDivisor(node).setScale(1);
            }
        };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode node = DoubleNode.valueOf((double) value);
        validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
        assertTrue(context.getErrors().isEmpty());
    }

    @Test
    void convertedNonFiniteInputDoesNotRoundTheDivisor() {
        long value = 9007199254740993L;
        Schema parent = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + value + "}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                LongNode.valueOf(value), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDividend(JsonNode node) {
                return BigDecimal.valueOf(value);
            }
        };
        for (JsonNode node : List.of(DoubleNode.valueOf(Double.NaN), DoubleNode.valueOf(Double.POSITIVE_INFINITY),
                FloatNode.valueOf(Float.NaN), FloatNode.valueOf(Float.NEGATIVE_INFINITY))) {
            ExecutionContext context = parent.createExecutionContext();
            validator.validate(context, node, node, new NodePath(PathType.JSON_POINTER));
            assertTrue(context.getErrors().isEmpty());
        }
    }


}
