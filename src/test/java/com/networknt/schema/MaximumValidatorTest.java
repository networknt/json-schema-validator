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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaximumValidatorTest extends BaseJsonSchemaValidatorTest {
    private static final String INTEGER = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"integer\", \"maximum\": %s }";
    private static final String NUMBER = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"maximum\": %s }";
    private static final String EXCLUSIVE_INTEGER = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"integer\", \"maximum\": %s, \"exclusiveMaximum\": true}";

    private static final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

    private static final ObjectMapper mapper = new ObjectMapper();
    // due to a jackson bug, a float number which is larger than Double.POSITIVE_INFINITY cannot be convert to BigDecimal correctly
    // https://github.com/FasterXML/jackson-databind/issues/1770
    // https://github.com/FasterXML/jackson-databind/issues/2087
    private static final ObjectMapper bigDecimalMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    private static final ObjectMapper bigIntegerMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

    static String[][] augmentWithQuotes(String[][] values) {
        for (int i = 0; i < values.length; i++) {
            String[] pair = values[i];
            values[i] = new String[]{pair[0], format("\"%s\"", pair[1])};
        }
        return values;
    }

    @Test
    void positiveNumber() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"1000.1", "1000"},
                {"1000", "1E3"},
        });

        expectNoMessages(values, NUMBER);

    }

    @Test
    void negativeNumber() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                           value
//            These values overflow 64bit IEEE 754
                {"1.7976931348623157e+308", "1.7976931348623159e+308"},
                {"1.7976931348623156e+308", "1.7976931348623157e+308"},

//            Here, threshold is parsed as integral number, yet payload is 'number'
                {"1000", "1000.1"},

//          See a {@link #doubleValueCoarsing() doubleValueCoarsing} test notes below
//            {"1.7976931348623157e+308",         "1.7976931348623158e+308"},
        });

        expectSomeMessages(values, NUMBER);

        expectSomeMessages(values, NUMBER, mapper, bigDecimalMapper);

        expectSomeMessages(values, NUMBER, bigDecimalMapper, bigDecimalMapper);
    }

    @Test
    void positiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"9223372036854775807", "9223372036854775807"},
                {"9223372036854775808", "9223372036854775808"},

//                testIntegerTypeWithFloatMaxPositive
                {"37.7", "37"},

//                testMaximumDoubleValue
                {"1E39", "1000"},
        });

        expectNoMessages(values, INTEGER);

        expectNoMessages(values, INTEGER, bigIntegerMapper);
    }

    @Test
    void negativeInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                value
                {"9223372036854775800", "9223372036854775855"},
                {"9223372036854775807", "9223372036854775808"},
                {"9223372036854775807", new BigDecimal(String.valueOf(Double.MAX_VALUE)).add(BigDecimal.ONE).toString()},
                {"9223372036854775806", new BigDecimal(String.valueOf(Double.MAX_VALUE)).add(BigDecimal.ONE).toString()},
                {"9223372036854776000", "9223372036854776001"},
                {"1000", "1E39"},
                {"37.7", "38"},
        });

        expectSomeMessages(values, INTEGER);

        expectSomeMessages(values, INTEGER, mapper, bigIntegerMapper);
    }

    @Test
    void positiveExclusiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"9223372036854775000", "9223372036854774988"},
                {"20", "10"},

//                threshold outside long range
                {"9223372036854775809", "9223372036854775806"},

//                both threshold and value are outside long range
                {"9223372036854775809", "9223372036854775808"},
        });

        expectNoMessages(values, EXCLUSIVE_INTEGER);

        expectNoMessages(values, EXCLUSIVE_INTEGER, bigIntegerMapper);
    }

    @Test
    void negativeExclusiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"10", "20"},

//                value outside long range
                {"9223372036854775806", "9223372036854775808"},

//                both threshold and value are outside long range
                {"9223372036854775808", "9223372036854775809"},
        });

        expectSomeMessages(values, EXCLUSIVE_INTEGER);

        expectSomeMessages(values, EXCLUSIVE_INTEGER, mapper, bigIntegerMapper);
    }

    @Test
    void negativeDoubleOverflowTest() throws IOException {
        String[][] values = new String[][]{
//            maximum,                           value
//                both of these get parsed into double (with a precision loss) as  1.7976931348623157E+308
                {"1.79769313486231571E+308", "1.79769313486231572e+308"},
//                while underflow in not captures in previous case (unquoted number is parsed as double)
//                it is captured if value is passed as string, which is correctly parsed by BidDecimal
//                thus effective comparison is between
//                maximum 1.7976931348623157E+308  and
//                value   1.79769313486231572e+308
//                {"1.79769313486231571E+308",        "\"1.79769313486231572e+308\""},
                {"1.7976931348623157E+309", "1.7976931348623157e+309"},
                {"1.7976931348623157E+309", "\"1.7976931348623157e+309\""},
                {"1.000000000000000000000001E+400", "1.000000000000000000000001E+401"},
                {"1.000000000000000000000001E+400", "\"1.000000000000000000000001E+401\""},
                {"1.000000000000000000000001E+400", "1.000000000000000000000002E+400"},
                {"1.000000000000000000000001E+400", "\"1.000000000000000000000002E+400\""},
                {"1.000000000000000000000001E+400", "1.0000000000000000000000011E+400"},
                {"1.000000000000000000000001E+400", "\"1.0000000000000000000000011E+400\""},
        };

        for (String[] aTestCycle : values) {
            String maximum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(NUMBER, maximum);
            SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().typeLoose(true).build();
            // Schema and document parsed with just double
            JsonSchema v = factory.getSchema(mapper.readTree(schema), config);
            JsonNode doc = mapper.readTree(value);
            Set<ValidationMessage> messages = v.validate(doc);
            assertTrue(messages.isEmpty(), format("Maximum %s and value %s are interpreted as Infinity, thus no schema violation should be reported", maximum, value));

            // document parsed with BigDecimal

            doc = bigDecimalMapper.readTree(value);
            Set<ValidationMessage> messages2 = v.validate(doc);
            if (Double.valueOf(maximum).equals(Double.POSITIVE_INFINITY)) {
                assertTrue(messages2.isEmpty(), format("Maximum %s and value %s are equal, thus no schema violation should be reported", maximum, value));
            } else {
                assertFalse(messages2.isEmpty(), format("Maximum %s is smaller than value %s ,  should be validation error reported", maximum, value));
            }


            // schema and document parsed with BigDecimal
            v = factory.getSchema(bigDecimalMapper.readTree(schema), config);
            Set<ValidationMessage> messages3 = v.validate(doc);
            //when the schema and value are both using BigDecimal, the value should be parsed in same mechanism.
            String theValue = value.toLowerCase().replace("\"", "");
            if (maximum.toLowerCase().equals(theValue)) {
                assertTrue(messages3.isEmpty(), format("Maximum %s and value %s are equal, thus no schema violation should be reported", maximum, value));
            } else {
                assertFalse(messages3.isEmpty(), format("Maximum %s is smaller than value %s ,  should be validation error reported", maximum, value));
            }
        }
    }

    /**
     * value of 1.7976931348623158e+308 is not converted to POSITIVE_INFINITY for some reason
     * the only way to spot this is to use BigDecimal for schema (and for document)
     */
    @Test
    void doubleValueCoarsing() throws IOException {
        String schema = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"maximum\": 1.7976931348623157e+308 }";
        String content = "1.7976931348623158e+308";

        JsonNode doc = mapper.readTree(content);
        JsonSchema v = factory.getSchema(mapper.readTree(schema));

        Set<ValidationMessage> messages = v.validate(doc);
        assertTrue(messages.isEmpty(), "Validation should succeed as by default double values are used by mapper");

        doc = bigDecimalMapper.readTree(content);
        messages = v.validate(doc);
        // "1.7976931348623158e+308" == "1.7976931348623157e+308" == Double.MAX_VALUE
        // new BigDecimal("1.7976931348623158e+308").compareTo(new BigDecimal("1.7976931348623157e+308")) > 0
        assertFalse(messages.isEmpty(), "Validation should not succeed because content is using bigDecimalMapper, and bigger than the maximum");

        /*
         * Note: technically this is where 1.7976931348623158e+308 rounding to 1.7976931348623157e+308 could be spotted,
         *       yet it requires a dedicated case of comparison BigDecimal to BigDecimal. Since values above
         *       1.7976931348623158e+308 are parsed as Infinity anyways (jackson uses double as primary type with later
         *       "upcasting" to BigDecimal, if property is set) adding a dedicated code block just for this one case
         *       seems infeasible.
         */
        v = factory.getSchema(bigDecimalMapper.readTree(schema));
        messages = v.validate(doc);
        assertFalse(messages.isEmpty(), "Validation should succeed as by default double values are used by mapper");
    }

    /**
     * BigDecimalMapper issue, it doesn't work as expected, it will treat 1.7976931348623159e+308 as INFINITY instead of as it is.
     */
    @Test
    void doubleValueCoarsingExceedRange() throws IOException {
        String schema = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"maximum\": 1.7976931348623159e+308 }";
        String content = "1.7976931348623160e+308";

        JsonNode doc = mapper.readTree(content);
        JsonSchema v = factory.getSchema(mapper.readTree(schema));

        Set<ValidationMessage> messages = v.validate(doc);
        assertTrue(messages.isEmpty(), "Validation should succeed as by default double values are used by mapper");

        doc = bigDecimalMapper.readTree(content);
        messages = v.validate(doc);
        // "1.7976931348623158e+308" == "1.7976931348623157e+308" == Double.MAX_VALUE
        // new BigDecimal("1.7976931348623158e+308").compareTo(new BigDecimal("1.7976931348623157e+308")) > 0
        assertTrue(messages.isEmpty(), "Validation should success because the bug of bigDecimalMapper, it will treat 1.7976931348623159e+308 as INFINITY");

        /*
         * Note: technically this is where 1.7976931348623158e+308 rounding to 1.7976931348623157e+308 could be spotted,
         *       yet it requires a dedicated case of comparison BigDecimal to BigDecimal. Since values above
         *       1.7976931348623158e+308 are parsed as Infinity anyways (jackson uses double as primary type with later
         *       "upcasting" to BigDecimal, if property is set) adding a dedicated code block just for this one case
         *       seems infeasible.
         */
        v = factory.getSchema(bigDecimalMapper.readTree(schema));
        messages = v.validate(doc);
        // Before 2.16.0 messages will be empty due to bug https://github.com/FasterXML/jackson-databind/issues/1770
        // assertTrue(messages.isEmpty(), "Validation should success because the bug of bigDecimalMapper, it will treat 1.7976931348623159e+308 as INFINITY");
        assertFalse(messages.isEmpty(), "Validation should fail as Incorrect deserialization for BigDecimal numbers is fixed in 2.16.0");
    }

    private static final String POSITIVE_TEST_CASE_TEMPLATE = "Expecting no validation errors, maximum %s is greater than value %s";

    private static void expectNoMessages(String[][] values, String schemaTemplate) throws IOException {
        expectNoMessages(values, schemaTemplate, mapper);
    }

    private static void expectNoMessages(String[][] values, String schemaTemplate, ObjectMapper mapper) throws IOException {
        for (String[] aTestCycle : values) {
            String maximum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(schemaTemplate, maximum);
            SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().typeLoose(true).build();

            JsonSchema v = factory.getSchema(mapper.readTree(schema), config);
            JsonNode doc = mapper.readTree(value);

            Set<ValidationMessage> messages = v.validate(doc);
            assertTrue(messages.isEmpty(), format(MaximumValidatorTest.POSITIVE_TEST_CASE_TEMPLATE, maximum, value));
        }
    }

    private static final String NEGATIVE_TEST_CASE_TEMPLATE = "Expecting validation error, value %s is greater than maximum %s";

    private static void expectSomeMessages(String[][] values, String schemaTemplate) throws IOException {
        expectSomeMessages(values, schemaTemplate, mapper, mapper);
    }

    private static void expectSomeMessages(String[][] values, String schemaTemplate, ObjectMapper mapper, ObjectMapper mapper2) throws IOException {
        for (String[] aTestCycle : values) {
            String maximum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(schemaTemplate, maximum);

            JsonSchema v = factory.getSchema(mapper.readTree(schema));
            JsonNode doc = mapper2.readTree(value);

            Set<ValidationMessage> messages = v.validate(doc);
            assertFalse(messages.isEmpty(), format(MaximumValidatorTest.NEGATIVE_TEST_CASE_TEMPLATE, value, maximum));
        }
    }
}


