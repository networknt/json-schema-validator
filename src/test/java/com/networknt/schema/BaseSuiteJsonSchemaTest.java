/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.FileResourceManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.undertow.Handlers.resource;
import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseSuiteJsonSchemaTest {
    protected ObjectMapper mapper = new ObjectMapper();
    protected JsonSchemaFactory validatorFactory;
    protected static Undertow server = null;

    protected BaseSuiteJsonSchemaTest(SpecVersion.VersionFlag version) {
        validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(version)).objectMapper(mapper).build();
    }

    @BeforeAll
    public static void setUp() {
        if (server == null) {
            server = Undertow.builder()
                    .addHttpListener(1234, "localhost")
                    .setHandler(resource(new FileResourceManager(
                            new File("./src/test/resources/remotes"), 100)))
                    .build();
            server.start();
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (server != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();

            }
            server.stop();
            server = null;
        }
    }

    protected void runTestFile(String testCaseFile) throws Exception {
        final URI testCaseFileUri = URI.create("classpath:" + testCaseFile);
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(testCaseFile);
        ArrayNode testCases = mapper.readValue(in, ArrayNode.class);
        final String VALIDATION_MESSAGES = "validationMessages";
        for (int j = 0; j < testCases.size(); j++) {
            try {
                JsonNode testCase = testCases.get(j);
                SchemaValidatorsConfig config = new SchemaValidatorsConfig();

                ArrayNode testNodes = (ArrayNode) testCase.get("tests");
                for (int i = 0; i < testNodes.size(); i++) {
                    JsonNode test = testNodes.get(i);
                    JsonNode node = test.get("data");
                    JsonNode typeLooseNode = test.get("isTypeLoose");
                    // Configure the schemaValidator to set typeLoose's value based on the test file,
                    // if test file do not contains typeLoose flag, use default value: true.
                    config.setTypeLoose((typeLooseNode == null) ? false : typeLooseNode.asBoolean());
                    JsonSchema schema = validatorFactory.getSchema(testCaseFileUri, testCase.get("schema"), config);
                    List<ValidationMessage> errors = new ArrayList<ValidationMessage>();

                    errors.addAll(schema.validate(node));
                    // Clear CollectorContext after every test.
                    if (test.get("valid").asBoolean()) {
                        if (!errors.isEmpty()) {
                            System.out.println("---- test case failed ----");
                            System.out.println("Description: " + test.get("description"));
                            System.out.println("schema: " + schema.toString());
                            System.out.println("data: " + test.get("data"));
                            System.out.println("errors: " + errors);
                        }
                        assertEquals(0, errors.size());
                    } else {
                        if (errors.isEmpty()) {
                            System.out.println("---- test case failed ----");
                            System.out.println("Description: " + test.get("description"));
                            System.out.println("schema: " + schema);
                            System.out.println("data: " + test.get("data"));
                        } else {
                            JsonNode errorCount = test.get("errorCount");
                            if (errorCount != null && errorCount.isInt() && errors.size() != errorCount.asInt()) {
                                System.out.println("---- test case failed ----");
                                System.out.println("Description: " + test.get("description"));
                                System.out.println("schema: " + schema);
                                System.out.println("data: " + test.get("data"));
                                System.out.println("errors: " + errors);
                                assertEquals(errorCount.asInt(), errors.size(), "expected error count");
                            }
                        }
                        assertEquals(false, errors.isEmpty());
                    }

                    // ExpectedValidation Messages need not be exactly same as actual errors.. the below code checks if expected validation message is subset of actual errors
                    ArrayNode expectedValidationMesgs = (ArrayNode) test.get(VALIDATION_MESSAGES);
                    if (errors.isEmpty() && expectedValidationMesgs != null && expectedValidationMesgs.size() > 0) {
                        System.out.println("---- test case failed ----");
                        System.out.println("Description: " + test.get("description"));
                        System.out.println("schema: " + schema);
                        System.out.println("data: " + test.get("data"));
                        System.out.println("Expected Validation Messages: " + expectedValidationMesgs);
                        fail("Expected errors but no errors encountered during validation.");

                    } else if (expectedValidationMesgs != null) {
                        Iterator<JsonNode> it = expectedValidationMesgs.iterator();
                        while (it.hasNext()) {
                            boolean found = false;
                            String expectedMsg = it.next().textValue();
                            for (ValidationMessage actualMsg : errors) {
                                if (StringUtils.equals(expectedMsg, actualMsg.getMessage())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                System.out.println("---- test case failed ----");
                                System.out.println("Description: " + test.get("description"));
                                System.out.println("schema: " + schema);
                                System.out.println("data: " + test.get("data"));
                                System.out.println("errors: " + errors);
                                System.out.println("validationMessages: " + expectedValidationMesgs);
                                fail("Expected validation message is not found in actual validation messages");
                                break;
                            }
                        }
                    }

                    CollectorContext.getInstance().reset();
                }
            } catch (JsonSchemaException e) {
                throw new IllegalStateException(String.format("Current schema should not be invalid: %s", testCaseFile), e);
            }
        }
    }
}