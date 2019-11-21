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

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(Parameterized.class)
public class MaximumValidatorTest extends BaseJsonSchemaValidatorTest {
    @Parameterized.Parameters
    public static Collection<?> parameters() {
      return Arrays.asList(new Object[][] {
         { "http://json-schema.org/draft-04/schema#", SpecVersion.VersionFlag.V4 },
         { "http://json-schema.org/draft-04/schema#", SpecVersion.VersionFlag.V6 },
         { "http://json-schema.org/draft-04/schema#", SpecVersion.VersionFlag.V7 },
         { "http://json-schema.org/draft/2019-09/schema#", SpecVersion.VersionFlag.V201909 }
      });
    }
    
    private static ObjectMapper MAPPER = new ObjectMapper();
    // due to a jackson bug, a float number which is larger than Double.POSITIVE_INFINITY cannot be convert to BigDecimal correctly
    // https://github.com/FasterXML/jackson-databind/issues/1770
    // https://github.com/FasterXML/jackson-databind/issues/2087
    private static ObjectMapper BIG_DECIMAL_MAPPER = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    private static ObjectMapper BIG_INTEGER_MAPPER = new ObjectMapper().enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
    
    private final String integerTemplate;
    private final String numberTemplate;
    private final String exclusiveIntegerTemplate;
    private final JsonSchemaFactory factory;
    
    public MaximumValidatorTest(final String draftUrl, final SpecVersion.VersionFlag specVersion) {
        super(specVersion);
        this.integerTemplate = String.format(
                "{ \"$schema\":\"%s\", \"type\": \"integer\", \"maximum\": %%s }",
                draftUrl);
        this.numberTemplate = String.format(
                "{ \"$schema\":\"%s\", \"type\": \"number\", \"maximum\": %%s }",
                draftUrl);
        this.exclusiveIntegerTemplate = String.format(
                "{ \"$schema\":\"%s\", \"type\": \"integer\", \"maximum\": %%s, \"exclusiveMaximum\": true}",
                draftUrl);
        this.factory = JsonSchemaFactory.getInstance(specVersion);
    }

    static String[][] augmentWithQuotes(String[][] values) {
        return Arrays.stream(values)
                .flatMap(pair -> Stream.of(pair, new String[]{pair[0], format("\"%s\"", pair[1])}))
                .toArray(String[][]::new);
    }

    @Test
    public void positiveNumber() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"1000.1",       "1000"},
                {"1000",         "1E3"},
        });

        expectNoMessages(values, numberTemplate);

    }

    @Test
    public void negativeNumber() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                           value
//            These values overflow 64bit IEEE 754
            {"1.7976931348623157e+308",         "1.7976931348623159e+308"},
            {"1.7976931348623156e+308",         "1.7976931348623157e+308"},

//            Here, threshold is parsed as integral number, yet payload is 'number'
            {"1000",                            "1000.1"},

//          See a {@link #doubleValueCoarsing() doubleValueCoarsing} test notes below
//            {"1.7976931348623157e+308",         "1.7976931348623158e+308"},
        });

        expectSomeMessages(values, numberTemplate);

        expectSomeMessages(values, numberTemplate, MAPPER, BIG_DECIMAL_MAPPER);

        expectSomeMessages(values, numberTemplate, BIG_DECIMAL_MAPPER, BIG_DECIMAL_MAPPER);
    }

    @Test
    public void positiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"9223372036854775807",         "9223372036854775807"},
                {"9223372036854775808",         "9223372036854775808"},

//                testIntegerTypeWithFloatMaxPositive
                {"37.7",         "37"},

//                testMaximumDoubleValue
                {"1E39",         "1000"},
        });

        expectNoMessages(values, integerTemplate);

        expectNoMessages(values, integerTemplate, BIG_INTEGER_MAPPER);
    }

    @Test
    public void negativeInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                value
                {"9223372036854775800",  "9223372036854775855"},
                {"9223372036854775807",  "9223372036854775808"},
                {"9223372036854775807",  new BigDecimal(String.valueOf(Double.MAX_VALUE)).add(BigDecimal.ONE).toString()},
                {"9223372036854775806",  new BigDecimal(String.valueOf(Double.MAX_VALUE)).add(BigDecimal.ONE).toString()},
                {"9223372036854776000",  "9223372036854776001"},
                {"1000",         "1E39"},
                {"37.7",         "38"},
        });

        expectSomeMessages(values, integerTemplate);

        expectSomeMessages(values, integerTemplate, MAPPER, BIG_INTEGER_MAPPER);
    }

    @Test
    public void positiveExclusiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"9223372036854775000",         "9223372036854774988"},
                {"20",         "10"},

//                threshold outside long range
                {"9223372036854775809",         "9223372036854775806"},

//                both threshold and value are outside long range
                {"9223372036854775809",         "9223372036854775808"},
        });

        expectNoMessages(values, exclusiveIntegerTemplate);

        expectNoMessages(values, exclusiveIntegerTemplate, BIG_INTEGER_MAPPER);
    }

    @Test
    public void negativeExclusiveInteger() throws IOException {
        String[][] values = augmentWithQuotes(new String[][]{
//            maximum,                       value
                {"10",         "20"},

//                value outside long range
                {"9223372036854775806",         "9223372036854775808"},

//                both threshold and value are outside long range
                {"9223372036854775808",         "9223372036854775809"},
        });

        expectSomeMessages(values, exclusiveIntegerTemplate);

        expectSomeMessages(values, exclusiveIntegerTemplate, MAPPER, BIG_INTEGER_MAPPER);
    }

    @Test
    public void negativeDoubleOverflowTest() throws IOException {
        String[][] values = new String[][]{
//            maximum,                           value
//                both of these get parsed into double (with a precision loss) as  1.7976931348623157E+308
                {"1.79769313486231571E+308",        "1.79769313486231572e+308"},
//                while underflow in not captures in previous case (unquoted number is parsed as double)
//                it is captured if value is passed as string, which is correctly parsed by BidDecimal
//                thus effective comparison is between
//                maximum 1.7976931348623157E+308  and
//                value   1.79769313486231572e+308
//                {"1.79769313486231571E+308",        "\"1.79769313486231572e+308\""},
                {"1.7976931348623157E+309",         "1.7976931348623157e+309"},
                {"1.7976931348623157E+309",         "\"1.7976931348623157e+309\""},
                {"1.000000000000000000000001E+400", "1.000000000000000000000001E+401"},
                {"1.000000000000000000000001E+400", "\"1.000000000000000000000001E+401\""},
                {"1.000000000000000000000001E+400", "1.000000000000000000000002E+400"},
                {"1.000000000000000000000001E+400", "\"1.000000000000000000000002E+400\""},
                {"1.000000000000000000000001E+400", "1.0000000000000000000000011E+400"},
                {"1.000000000000000000000001E+400", "\"1.0000000000000000000000011E+400\""},
        };

        for(String[] aTestCycle : values) {
            String maximum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(numberTemplate, maximum);
            SchemaValidatorsConfig config = new SchemaValidatorsConfig();
            config.setTypeLoose(true);
            // Schema and document parsed with just double
            JsonSchema v = factory.getSchema(MAPPER.readTree(schema), config);
            JsonNode doc = MAPPER.readTree(value);
            Set<ValidationMessage> messages = v.validate(doc);
            assertTrue(format("Maximum %s and value %s are interpreted as Infinity, thus no schema violation should be reported", maximum, value), messages.isEmpty());

            // document parsed with BigDecimal

            doc = BIG_DECIMAL_MAPPER.readTree(value);
            Set<ValidationMessage> messages2 = v.validate(doc);
            if(Double.valueOf(maximum).equals(Double.POSITIVE_INFINITY)) {
                assertTrue(format("Maximum %s and value %s are equal, thus no schema violation should be reported", maximum, value), messages2.isEmpty());
            } else {
                assertFalse(format("Maximum %s is smaller than value %s ,  should be validation error reported", maximum, value), messages2.isEmpty());
            }


            // schema and document parsed with BigDecimal
            v = factory.getSchema(BIG_DECIMAL_MAPPER.readTree(schema), config);
            Set<ValidationMessage> messages3 = v.validate(doc);
            //when the schema and value are both using BigDecimal, the value should be parsed in same mechanism.
            if(maximum.toLowerCase().equals(value.toLowerCase()) || Double.valueOf(maximum).equals(Double.POSITIVE_INFINITY)) {
                assertTrue(format("Maximum %s and value %s are equal, thus no schema violation should be reported", maximum, value), messages3.isEmpty());
            } else {
                assertFalse(format("Maximum %s is smaller than value %s ,  should be validation error reported", maximum, value), messages3.isEmpty());
            }
        }
    }

    /**
     *  value of 1.7976931348623158e+308 is not converted to POSITIVE_INFINITY for some reason
     *  the only way to spot this is to use BigDecimal for schema (and for document)
     */
    @Test
    public void doubleValueCoarsing() throws IOException {
        String schema = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"maximum\": 1.7976931348623157e+308 }";
        String content = "1.7976931348623158e+308";

        JsonNode doc = MAPPER.readTree(content);
        JsonSchema v = factory.getSchema(MAPPER.readTree(schema));

        Set<ValidationMessage> messages = v.validate(doc);
        assertTrue("Validation should succeed as by default double values are used by mapper", messages.isEmpty());

        doc = BIG_DECIMAL_MAPPER.readTree(content);
        messages = v.validate(doc);
        // "1.7976931348623158e+308" == "1.7976931348623157e+308" == Double.MAX_VALUE
        // new BigDecimal("1.7976931348623158e+308").compareTo(new BigDecimal("1.7976931348623157e+308")) > 0
        assertFalse("Validation should not succeed because content is using bigDecimalMapper, and bigger than the maximum", messages.isEmpty());

        /*
         * Note: technically this is where 1.7976931348623158e+308 rounding to 1.7976931348623157e+308 could be spotted,
         *       yet it requires a dedicated case of comparison BigDecimal to BigDecimal. Since values above
         *       1.7976931348623158e+308 are parsed as Infinity anyways (jackson uses double as primary type with later
         *       "upcasting" to BigDecimal, if property is set) adding a dedicated code block just for this one case
         *       seems infeasible.
         */
        v = factory.getSchema(BIG_DECIMAL_MAPPER.readTree(schema));
        messages = v.validate(doc);
        assertFalse("Validation should succeed as by default double values are used by mapper", messages.isEmpty());
    }

    /**
     * BigDecimalMapper issue, it doesn't work as expected, it will treat 1.7976931348623159e+308 as INFINITY instead of as it is.
     */
    @Test
    public void doubleValueCoarsingExceedRange() throws IOException {
        String schema = "{ \"$schema\":\"http://json-schema.org/draft-04/schema#\", \"type\": \"number\", \"maximum\": 1.7976931348623159e+308 }";
        String content = "1.7976931348623160e+308";

        JsonNode doc = MAPPER.readTree(content);
        JsonSchema v = factory.getSchema(MAPPER.readTree(schema));

        Set<ValidationMessage> messages = v.validate(doc);
        assertTrue("Validation should succeed as by default double values are used by mapper", messages.isEmpty());

        doc = BIG_DECIMAL_MAPPER.readTree(content);
        messages = v.validate(doc);
        // "1.7976931348623158e+308" == "1.7976931348623157e+308" == Double.MAX_VALUE
        // new BigDecimal("1.7976931348623158e+308").compareTo(new BigDecimal("1.7976931348623157e+308")) > 0
        assertTrue("Validation should success because the bug of bigDecimalMapper, it will treat 1.7976931348623159e+308 as INFINITY", messages.isEmpty());

        /*
         * Note: technically this is where 1.7976931348623158e+308 rounding to 1.7976931348623157e+308 could be spotted,
         *       yet it requires a dedicated case of comparison BigDecimal to BigDecimal. Since values above
         *       1.7976931348623158e+308 are parsed as Infinity anyways (jackson uses double as primary type with later
         *       "upcasting" to BigDecimal, if property is set) adding a dedicated code block just for this one case
         *       seems infeasible.
         */
        v = factory.getSchema(BIG_DECIMAL_MAPPER.readTree(schema));
        messages = v.validate(doc);
        assertTrue("Validation should success because the bug of bigDecimalMapper, it will treat 1.7976931348623159e+308 as INFINITY", messages.isEmpty());
    }

    private static final String POSITIVE_TEST_CASE_TEMPLATE = "Expecting no validation errors, maximum %s is greater than value %s";

    private void expectNoMessages(String[][] values, String schemaTemplate) throws IOException {
        expectNoMessages(values, schemaTemplate, MAPPER);
    }

    private void expectNoMessages(String[][] values, String schemaTemplate, ObjectMapper mapper) throws IOException {
        for (String[] aTestCycle : values) {
            String maximum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(schemaTemplate, maximum);
            SchemaValidatorsConfig config = new SchemaValidatorsConfig();
            config.setTypeLoose(true);

            JsonSchema v = factory.getSchema(mapper.readTree(schema), config);
            JsonNode doc = mapper.readTree(value);

            Set<ValidationMessage> messages = v.validate(doc);
            assertTrue(format(MaximumValidatorTest.POSITIVE_TEST_CASE_TEMPLATE, maximum, value), messages.isEmpty());
        }
    }

    private static final String NEGATIVE_TEST_CASE_TEMPLATE = "Expecting validation error, value %s is greater than maximum %s";

    private void expectSomeMessages(String[][] values, String schemaTemplate) throws IOException {
        expectSomeMessages(values, schemaTemplate, MAPPER, MAPPER);
    }

    private void expectSomeMessages(String[][] values, String schemaTemplate, ObjectMapper mapper, ObjectMapper mapper2) throws IOException {
        for (String[] aTestCycle : values) {
            String maximum = aTestCycle[0];
            String value = aTestCycle[1];
            String schema = format(schemaTemplate, maximum);

            JsonSchema v = factory.getSchema(mapper.readTree(schema));
            JsonNode doc = mapper2.readTree(value);

            Set<ValidationMessage> messages = v.validate(doc);
            assertFalse(format(MaximumValidatorTest.NEGATIVE_TEST_CASE_TEMPLATE, value, maximum), messages.isEmpty());
        }
    }
}


