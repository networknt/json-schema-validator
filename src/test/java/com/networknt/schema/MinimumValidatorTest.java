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

package com.networknt.schema;

import static com.networknt.schema.MaximumValidatorTest.augmentWithQuotes;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class MinimumValidatorTest {
    private static final String NUMBER = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"minimum\": %s }";
    private static final String EXCLUSIVE_INTEGER = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"integer\", \"minimum\": %s, \"exclusiveMinimum\": true}";
    private static final String INTEGER = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"integer\", \"minimum\": %s }";
    private static final String NEGATIVE_MESSAGE_TEMPLATE = "Expecting validation errors, value %s is smaller than minimum %s";
    private static final String POSITIVT_MESSAGE_TEMPLATE = "Expecting no validation errors, value %s is greater than minimum %s";
    private static final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

    private static ObjectMapper mapper;
    private static ObjectMapper bigDecimalMapper;
    private static ObjectMapper bigIntegerMapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        // due to a jackson bug, a float number which is larger than Double.POSITIVE_INFINITY cannot be convert to BigDecimal correctly
        // https://github.com/FasterXML/jackson-databind/issues/1770
        // https://github.com/FasterXML/jackson-databind/issues/2087
        bigDecimalMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        bigIntegerMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

    }

    @Test
    void positiveNumber() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            minimum,                       value
                {"1000", "1000.1"},
        });

        expectNoMessages(values, NUMBER, mapper);
    }

    @Test
    void negativeNumber() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            minimum,                           value
                {"-1.7976931348623157e+308", "-1.7976931348623159e+308"},
                {"-1.7976931348623156e+308", "-1.7976931348623157e+308"},
                {"-1000", "-1E309"},
                {"1000.1", "1000"},
//          See a {@link #doubleValueCoarsing() doubleValueCoarsing} test notes below
//            {"-1.7976931348623157e+308",         "-1.7976931348623158e+308"},
        });

        expectSomeMessages(values, NUMBER, mapper, mapper);

        expectSomeMessages(values, NUMBER, mapper, bigDecimalMapper);

        expectSomeMessages(values, NUMBER, bigDecimalMapper, bigDecimalMapper);
    }

    @Test
    void positiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            minimum,                       value
                {"-1E309", "-1000"},
                {"-9223372036854775808", "-9223372036854775808"},
        });

        expectNoMessages(values, INTEGER, mapper);
        expectNoMessages(values, INTEGER, mapper, bigIntegerMapper);
    }

    @Test
    void negativeInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            minimum,                value
                {"-9223372036854775800", "-9223372036854775855"},
                {"-9223372036854775808", "-9223372036854775809"},
                {"-9223372036854775808", new BigDecimal(String.valueOf(-Double.MAX_VALUE)).subtract(BigDecimal.ONE).toString()},
                {"-9223372036854775807", new BigDecimal(String.valueOf(-Double.MAX_VALUE)).subtract(BigDecimal.ONE).toString()},
                {"-9223372036854776000", "-9223372036854776001"},
        });
        expectSomeMessages(values, INTEGER, mapper, mapper);
    }

    @Test
    void positiveExclusiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//                minimum,                       value
                {"-9223372036854775000", "-9223372036854774988"},
                {"10", "20"},
//                threshold in outside long range
                {"-9223372036854775809", "-9223372036854775807"},
//                both threshold and value are outside long range
                {"-9223372036854775810", "-9223372036854775809"},
        });

        expectNoMessages(values, EXCLUSIVE_INTEGER, mapper);

        expectNoMessages(values, EXCLUSIVE_INTEGER, bigIntegerMapper);
    }

    @Test
    void negativeExclusiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            minimum,                       value
                {"20", "10"},

//                value is outside long range
                {"-9223372036854775807", "-9223372036854775809"},

//                both threshold and value are outside long range
                {"-9223372036854775809", "-9223372036854775810"},
        });

        expectSomeMessages(values, EXCLUSIVE_INTEGER, mapper, bigIntegerMapper);
    }

    @Test
    void negativeDoubleOverflowTest() throws IOException {
        String[][] values = {
//            minimum,                            value
                {"-1.79769313486231571E+308", "-1.79769313486231572e+308"},
//            while underflow in not captures in previous case (unquoted number is parsed as double)
//            it is captured if value is passed as string, which is correctly parsed by BidDecimal
//            thus effective comparison is between
//            minimum -1.7976931348623157E+308  and
//            value   -1.79769313486231572e+308
//            {"-1.79769313486231571E+308",        "\"-1.79769313486231572e+308\""},
                {"-1.7976931348623157E+309", "-1.7976931348623157e+309"},
                {"-1.7976931348623157E+309", "\"-1.7976931348623157e+309\""},
                {"-1.000000000000000000000001E+308", "-1.000000000000000000000001E+308"},
//            Similar to statements above
//            minimum -1.0E+308  and
//            value   -1.000000000000000000000001E+308
//            {"-1.000000000000000000000001E+308", "\"-1.000000000000000000000001E+308\""},
                {"-1.000000000000000000000001E+400", "-1.000000000000000000000001E+401"},
                {"-1.000000000000000000000001E+400", "\"-1.000000000000000000000001E+401\""},
                {"-1.000000000000000000000001E+400", "-1.000000000000000000000002E+400"},
                {"-1.000000000000000000000001E+400", "\"-1.000000000000000000000002E+400\""},
                {"-1.000000000000000000000001E+400", "-1.0000000000000000000000011E+400"},
                {"-1.000000000000000000000001E+400", "\"-1.0000000000000000000000011E+400\""},
        };

        for (String[] aTestCycle : values) {
            String minimum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(NUMBER, minimum);
            SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().typeLoose(true).build();

            // Schema and document parsed with just double
            JsonSchema v = factory.getSchema(mapper.readTree(schema), config);
            JsonNode doc = mapper.readTree(value);
            Set<ValidationMessage> messages = v.validate(doc);
            assertTrue(messages.isEmpty(), format("Minimum %s and value %s are interpreted as Infinity, thus no schema violation should be reported", minimum, value));

            // document parsed with BigDecimal
            doc = bigDecimalMapper.readTree(value);
            Set<ValidationMessage> messages2 = v.validate(doc);

            //when the schema and value are both using BigDecimal, the value should be parsed in same mechanism.
            if (Double.valueOf(minimum).equals(Double.NEGATIVE_INFINITY)) {
                /*
                 * {"-1.000000000000000000000001E+308", "-1.000000000000000000000001E+308"} will be false
                 * because the different between two mappers, without using big decimal, it loses some precises.
                 */
                assertTrue(messages2.isEmpty(), format("Minimum %s and value %s are equal, thus no schema violation should be reported", minimum, value));
            } else {
                assertFalse(messages2.isEmpty(), format("Minimum %s is larger than value %s ,  should be validation error reported", minimum, value));
            }

            // schema and document parsed with BigDecimal
            v = factory.getSchema(bigDecimalMapper.readTree(schema), config);
            Set<ValidationMessage> messages3 = v.validate(doc);
            //when the schema and value are both using BigDecimal, the value should be parsed in same mechanism.
            String theValue = value.toLowerCase().replace("\"", "");
            if (minimum.toLowerCase().equals(theValue)) {
                assertTrue(messages3.isEmpty(), format("Minimum %s and value %s are equal, thus no schema violation should be reported", minimum, value));
            } else {
                assertFalse(messages3.isEmpty(), format("Minimum %s is larger than value %s ,  should be validation error reported", minimum, value));
            }
        }
    }

    /**
     * value of -1.7976931348623158e+308 is not converted to NEGATIVE_INFINITY for some reason
     * the only way to spot this is to use BigDecimal for schema (and for document)
     */
    @Test
    void doubleValueCoarsing() throws IOException {
        String schema = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"minimum\": -1.7976931348623157e+308 }";
        String content = "-1.7976931348623158e+308";

        JsonNode doc = mapper.readTree(content);
        JsonSchema v = factory.getSchema(mapper.readTree(schema));

        Set<ValidationMessage> messages = v.validate(doc);
        assertTrue(messages.isEmpty(), "Validation should succeed as by default double values are used by mapper");

        doc = bigDecimalMapper.readTree(content);
        messages = v.validate(doc);
        assertFalse(messages.isEmpty(), "Validation should not succeed because content is using bigDecimalMapper, and smaller than the minimum");

        /*
         * Note: technically this is where -1.7976931348623158e+308 rounding to -1.7976931348623157e+308 could be
         *       spotted, yet it requires a dedicated case of comparison BigDecimal to BigDecimal. Since values below
         *       -1.7976931348623158e+308 are parsed as Infinity anyways (jackson uses double as primary type with later
         *       "upcasting" to BigDecimal, if property is set) adding a dedicated code block just for this one case
         *       seems infeasible.
         */
        v = factory.getSchema(bigDecimalMapper.readTree(schema));
        messages = v.validate(doc);
        assertFalse(messages.isEmpty(), "Validation should not succeed because content is using bigDecimalMapper, and smaller than the minimum");
    }

    /**
     * BigDecimalMapper issue, it doesn't work as expected, it will treat -1.7976931348623157e+309 as INFINITY instead of as it is.
     */
    @Test
    void doubleValueCoarsingExceedRange() throws IOException {
        String schema = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"minimum\": -1.7976931348623159e+308 }";
        String content = "-1.7976931348623160e+308";

        JsonNode doc = mapper.readTree(content);
        JsonSchema v = factory.getSchema(mapper.readTree(schema));

        Set<ValidationMessage> messages = v.validate(doc);
        assertTrue(messages.isEmpty(), "Validation should succeed as by default double values are used by mapper");

        doc = bigDecimalMapper.readTree(content);
        messages = v.validate(doc);
        assertTrue(messages.isEmpty(), "Validation should succeed due to the bug of BigDecimal option of mapper");

        v = factory.getSchema(bigDecimalMapper.readTree(schema));
        messages = v.validate(doc);
        // Before 2.16.0 messages will be empty due to bug https://github.com/FasterXML/jackson-databind/issues/1770
        //assertTrue(messages.isEmpty(), "Validation should succeed due to the bug of BigDecimal option of mapper");
        assertFalse(messages.isEmpty(), "Validation should fail as Incorrect deserialization for BigDecimal numbers is fixed in 2.16.0");
    }

    private void expectSomeMessages(String[][] values, String number, ObjectMapper mapper, ObjectMapper mapper2) throws IOException {
        for (String[] aTestCycle : values) {
            String minimum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(number, minimum);

            JsonSchema v = factory.getSchema(mapper.readTree(schema));
            JsonNode doc = mapper2.readTree(value);

            Set<ValidationMessage> messages = v.validate(doc);
            assertFalse(messages.isEmpty(), format(MinimumValidatorTest.NEGATIVE_MESSAGE_TEMPLATE, value, minimum));
        }
    }

    private void expectNoMessages(String[][] values, String number, ObjectMapper mapper) throws IOException {
        expectNoMessages(values, number, mapper, mapper);
    }

    private void expectNoMessages(String[][] values, String integer, ObjectMapper mapper, ObjectMapper bigIntegerMapper) throws IOException {
        for (String[] aTestCycle : values) {
            String minimum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(integer, minimum);
            SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().typeLoose(true).build();

            JsonSchema v = factory.getSchema(mapper.readTree(schema), config);
            JsonNode doc = bigIntegerMapper.readTree(value);

            Set<ValidationMessage> messages = v.validate(doc);
            assertTrue(messages.isEmpty(), format(MinimumValidatorTest.POSITIVT_MESSAGE_TEMPLATE, value, minimum));
        }
    }
}


