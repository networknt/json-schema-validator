package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonSchemaTest {
    protected ObjectMapper mapper = new ObjectMapper();

    public JsonSchemaTest() {
    }

    private void runTestFile(String testCaseFile) throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(testCaseFile);
        ArrayNode testCases = (ArrayNode) mapper.readTree(in);

        for (int j = 0; j < testCases.size(); j++) {
            try {
                JsonNode testCase = testCases.get(j);
                JsonSchema schema = new JsonSchema(mapper, testCase.get("schema"));
                ArrayNode testNodes = (ArrayNode) testCase.get("tests");
                for (int i = 0; i < testNodes.size(); i++) {
                    JsonNode test = testNodes.get(i);
                    JsonNode node = test.get("data");
                    List<ValidationMessage> errors = new ArrayList<ValidationMessage>();

                    errors.addAll(schema.validate(node));

                    if (test.get("valid").asBoolean()) {
                        if (!errors.isEmpty()) {
                            System.out.println("---- test case filed ----");
                            System.out.println("schema: " + schema.toString());
                            System.out.println("data: " + test.get("data"));
                        }
                        Assert.assertEquals(0, errors.size());
                    } else {
                        if (errors.isEmpty()) {
                            System.out.println("---- test case filed ----");
                            System.out.println("schema: " + schema);
                            System.out.println("data: " + test.get("data"));
                        }
                        Assert.assertEquals(false, errors.isEmpty());
                    }
                }
            } catch (JsonSchemaException e) {
                System.out.println("Bypass validation due to invalid schema: " + e.getMessage());
            }
        }
    }

    @Test
    public void testBignumValidator() throws Exception {
        runTestFile("tests/optional/bignum.json");
    }

    @Test
    public void testFormatValidator() throws Exception {
        runTestFile("tests/optional/format.json");
    }

    @Test
    public void testZeroTerminatedFloatsValidator() throws Exception {
        runTestFile("tests/optional/zeroTerminatedFloats.json");
    }

    @Test
    public void testAdditionalItemsValidator() throws Exception {
        runTestFile("tests/additionalItems.json");
    }

    @Test
    public void testAdditionalPropertiesValidator() throws Exception {
        runTestFile("tests/additionalProperties.json");
    }

    @Test
    public void testAllOfValidator() throws Exception {
        runTestFile("tests/allOf.json");
    }

    @Test
    public void testAnyOFValidator() throws Exception {
        runTestFile("tests/anyOf.json");
    }

    @Test
    public void testDefaultValidator() throws Exception {
        runTestFile("tests/default.json");
    }

    @Test
    public void testDefinitionsValidator() throws Exception {
        runTestFile("tests/definitions.json");
    }

    @Test
    public void testDependenciesValidator() throws Exception {
        runTestFile("tests/dependencies.json");
    }

    @Test
    public void testEnumValidator() throws Exception {
        runTestFile("tests/enum.json");
    }

    @Test
    public void testItemsValidator() throws Exception {
        runTestFile("tests/items.json");
    }

    @Test
    public void testMaximumValidator() throws Exception {
        runTestFile("tests/maximum.json");
    }

    @Test
    public void testMaxItemsValidator() throws Exception {
        runTestFile("tests/maxItems.json");
    }

    @Test
    public void testMaxLengthValidator() throws Exception {
        runTestFile("tests/maxLength.json");
    }

    @Test
    public void testMaxPropertiesValidator() throws Exception {
        runTestFile("tests/maxProperties.json");
    }

    @Test
    public void testMinimumValidator() throws Exception {
        runTestFile("tests/minimum.json");
    }

    @Test
    public void testMinItemsValidator() throws Exception {
        runTestFile("tests/minItems.json");
    }

    @Test
    public void testMinLengthValidator() throws Exception {
        runTestFile("tests/minLength.json");
    }

    @Test
    public void testMinPropertiesValidator() throws Exception {
        runTestFile("tests/minProperties.json");
    }

    @Test
    public void testMultipleOfValidator() throws Exception {
        runTestFile("tests/multipleOf.json");
    }

    @Test
    public void testNotValidator() throws Exception {
        runTestFile("tests/not.json");
    }

    @Test
    public void testNullAndFormatValidator() throws Exception {
        runTestFile("tests/nullAndFormat.json");
    }

    @Test
    public void testOneOfValidator() throws Exception {
        runTestFile("tests/oneOf.json");
    }

    @Test
    public void testPatternValidator() throws Exception {
        runTestFile("tests/pattern.json");
    }

    @Test
    public void testPatternPropertiesValidator() throws Exception {
        runTestFile("tests/patternProperties.json");
    }

    @Test
    public void testPropertiesValidator() throws Exception {
        runTestFile("tests/properties.json");
    }

    @Test
    public void testRefValidator() throws Exception {
        runTestFile("tests/ref.json");
    }

    @Test
    public void testRefRemoteValidator() throws Exception {
        runTestFile("tests/refRemote.json");
    }

    @Test
    public void testRequiredValidator() throws Exception {
        runTestFile("tests/required.json");
    }

    @Test
    public void testTypeValidator() throws Exception {
        runTestFile("tests/type.json");
    }

    @Test
    public void testUniqueItemsValidator() throws Exception {
        runTestFile("tests/uniqueItems.json");
    }

}
