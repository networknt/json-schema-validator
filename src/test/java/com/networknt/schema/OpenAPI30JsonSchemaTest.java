package com.networknt.schema;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OpenAPI30JsonSchemaTest extends HTTPServiceSupport {
    protected ObjectMapper mapper = new ObjectMapper();
    protected JsonSchemaFactory validatorFactory = JsonSchemaFactory
            .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4)).build();

    OpenAPI30JsonSchemaTest() {
    }

    private void runTestFile(String testCaseFile) throws Exception {
        final SchemaLocation testCaseFileUri = SchemaLocation.of("classpath:" + testCaseFile);
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(testCaseFile);
        ArrayNode testCases = mapper.readValue(in, ArrayNode.class);

        for (int j = 0; j < testCases.size(); j++) {
            try {
                JsonNode testCase = testCases.get(j);

                ArrayNode testNodes = (ArrayNode) testCase.get("tests");
                for (int i = 0; i < testNodes.size(); i++) {
                    JsonNode test = testNodes.get(i);
                    System.out.println("=== " + test.get("description"));
                    JsonNode node = test.get("data");
                    JsonNode typeLooseNode = test.get("isTypeLoose");
                    // Configure the schemaValidator to set typeLoose's value based on the test file,
                    // if test file do not contains typeLoose flag, use default value: true.
                    SchemaValidatorsConfig.Builder configBuilder = SchemaValidatorsConfig.builder();
                    configBuilder.typeLoose(typeLooseNode != null && typeLooseNode.asBoolean());
                    configBuilder.discriminatorKeywordEnabled(true);
                    JsonSchema schema = validatorFactory.getSchema(testCaseFileUri, testCase.get("schema"), configBuilder.build());

                    List<ValidationMessage> errors = new ArrayList<ValidationMessage>(schema.validate(node));

                    if (test.get("valid").asBoolean()) {
                        if (!errors.isEmpty()) {
                            System.out.println("---- test case failed ----");
                            System.out.println("schema: " + schema);
                            System.out.println("data: " + test.get("data"));
                            System.out.println("errors:");
                            for (ValidationMessage error : errors) {
                                System.out.println(error);
                            }
                        }
                        assertEquals(0, errors.size());
                    } else {
                        if (errors.isEmpty()) {
                            System.out.println("---- test case failed ----");
                            System.out.println("schema: " + schema);
                            System.out.println("data: " + test.get("data"));
                        } else {
                            JsonNode errorCount = test.get("errorCount");
                            if (errorCount != null && errorCount.isInt() && errors.size() != errorCount.asInt()) {
                                System.out.println("---- test case failed ----");
                                System.out.println("schema: " + schema);
                                System.out.println("data: " + test.get("data"));
                                System.out.println("errors: " + errors);
                                for (ValidationMessage error : errors) {
                                    System.out.println(error);
                                }
                                assertEquals(errorCount.asInt(), errors.size(), "expected error count");
                            }
                        }
                        assertFalse(errors.isEmpty());
                    }
                }
            } catch (JsonSchemaException e) {
                throw new IllegalStateException(String.format("Current schema should not be invalid: %s", testCaseFile), e);
            }
        }
    }

    @Test
    void testDiscriminatorMapping() throws Exception {
        runTestFile("openapi3/discriminator.json");
    }
}
