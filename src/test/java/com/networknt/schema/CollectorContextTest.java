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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

public class CollectorContextTest {

    private static final String SAMPLE_COLLECTOR = "sampleCollector";

    private static final String SAMPLE_COLLECTOR_OTHER = "sampleCollectorOther";

    private JsonSchema jsonSchema;

    private JsonSchema jsonSchemaForCombine;

    @BeforeEach
    public void setup() throws Exception {
        setupSchema();
    }

    @AfterEach
    public void cleanup() {
        if (CollectorContext.getInstance() != null) {
            CollectorContext.getInstance().reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCollectorContextWithKeyword() throws Exception {
        ValidationResult validationResult = validate("{\"test-property1\":\"sample1\",\"test-property2\":\"sample2\"}");
        Assertions.assertEquals(0, validationResult.getValidationMessages().size());
        List<String> contextValues = (List<String>) validationResult.getCollectorContext().get(SAMPLE_COLLECTOR);
        contextValues.sort(null);
        Assertions.assertEquals(0, validationResult.getValidationMessages().size());
        Assertions.assertEquals(2, contextValues.size());
        Assertions.assertEquals(contextValues.get(0), "actual_value_added_to_context1");
        Assertions.assertEquals(contextValues.get(1), "actual_value_added_to_context2");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCollectorContextWithMultipleThreads() throws Exception {

        ValidationThread validationRunnable1 = new ValidationThread("{\"test-property1\":\"sample1\" }", "thread1");
        ValidationThread validationRunnable2 = new ValidationThread("{\"test-property1\":\"sample2\" }", "thread2");
        ValidationThread validationRunnable3 = new ValidationThread("{\"test-property1\":\"sample3\" }", "thread3");

        // This simulates calling the validateAndCollect method from three different
        // threads.It should be noted that all these three threads use same
        // json schema instance.Create three threads with there own Runnables.
        Thread thread1 = new Thread(validationRunnable1);
        Thread thread2 = new Thread(validationRunnable2);
        Thread thread3 = new Thread(validationRunnable3);

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        ValidationResult validationResult1 = validationRunnable1.getValidationResult();
        ValidationResult validationResult2 = validationRunnable2.getValidationResult();
        ValidationResult validationResult3 = validationRunnable3.getValidationResult();

        Assertions.assertEquals(0, validationResult1.getValidationMessages().size());
        Assertions.assertEquals(0, validationResult2.getValidationMessages().size());
        Assertions.assertEquals(0, validationResult3.getValidationMessages().size());

        List<String> contextValue1 = (List<String>) validationResult1.getCollectorContext().get(SAMPLE_COLLECTOR);
        List<String> contextValue2 = (List<String>) validationResult2.getCollectorContext().get(SAMPLE_COLLECTOR);
        List<String> contextValue3 = (List<String>) validationResult3.getCollectorContext().get(SAMPLE_COLLECTOR);

        Assertions.assertEquals(contextValue1.get(0), "actual_value_added_to_context1");
        Assertions.assertEquals(contextValue2.get(0), "actual_value_added_to_context2");
        Assertions.assertEquals(contextValue3.get(0), "actual_value_added_to_context3");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCollectorGetAll() throws JsonMappingException, JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ValidationResult validationResult = jsonSchemaForCombine.validateAndCollect(objectMapper
                .readTree("{\"property1\":\"sample1\",\"property2\":\"sample2\",\"property3\":\"sample3\" }"));
        CollectorContext collectorContext = validationResult.getCollectorContext();
        Assertions.assertEquals(((List<String>) collectorContext.get(SAMPLE_COLLECTOR)).size(), 1);
        Assertions.assertEquals(((List<String>) collectorContext.get(SAMPLE_COLLECTOR_OTHER)).size(), 3);
    }

    private JsonMetaSchema getJsonMetaSchema(String uri) throws Exception {
        JsonMetaSchema jsonMetaSchema = JsonMetaSchema.builder(uri, JsonMetaSchema.getV201909())
                .addKeyword(new CustomKeyword()).addKeyword(new CustomKeyword1()).addFormat(new Format() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public boolean matches(String value) {
                        CollectorContext collectorContext = CollectorContext.getInstance();
                        if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
                            collectorContext.add(SAMPLE_COLLECTOR, new ArrayList<String>());
                        }
                        List<String> returnList = (List<String>) collectorContext.get(SAMPLE_COLLECTOR);
                        returnList.add(value);
                        return true;
                    }

                    @Override
                    public String getName() {
                        return "sample-format";
                    }

                    // Return null. As are just testing collection context.
                    @Override
                    public String getErrorMessageDescription() {
                        return null;
                    }
                }).build();
        return jsonMetaSchema;
    }

    private void setupSchema() throws Exception {
        final JsonMetaSchema metaSchema = getJsonMetaSchema(
                "https://github.com/networknt/json-schema-validator/tests/schemas/example01");
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
                .build();
        this.jsonSchema = schemaFactory.getSchema(getSchemaString());
        this.jsonSchemaForCombine = schemaFactory.getSchema(getSchemaStringMultipleProperties());
    }

    private String getSchemaString() {
        return "{"
                + "\"$schema\": \"https://github.com/networknt/json-schema-validator/tests/schemas/example01\","
                + "\"title\" : \"Sample test schema\",\n"
                + "\"description\" : \"Sample schema definition\","
                + "\"type\" : \"object\","
                + "\"properties\" :"
                + "{"
                + "\"test-property1\" : "
                + "{"
                + "\"title\": \"Test Property1\","
                + "\"type\": \"string\", "
                + "\"custom-keyword\":[\"x\",\"y\"]"
                + "},"
                + "\"test-property2\" : "
                + "{"
                + "\"title\": \"Test Property2\","
                + "\"type\": \"string\", "
                + "\"custom-keyword\":[\"x\",\"y\"]"
                + "}"
                + "},"
                + "\"additionalProperties\":\"false\","
                + "\"required\": [\"test-property1\"]\n"
                + "}";
    }

    private String getSchemaStringMultipleProperties() {
        return "{"
                + "\"$schema\": \"https://github.com/networknt/json-schema-validator/tests/schemas/example01\","
                + "\"title\" : \"Sample test schema\","
                + "\"description\" : \"Sample schema definition\","
                + "\"type\" : \"object\","
                + "\"properties\" :"
                + "{"
                + "\"property1\" : "
                + "{"
                + "\"title\": \"Property1\","
                + "\"type\": \"string\", "
                + "\"custom-keyword1\":[\"x\",\"y\"],"
                + "\"format\":\"sample-format\""
                + "},"
                + "\"property2\" : "
                + "{"
                + "\"title\": \"Property2\","
                + "\"type\": \"string\", "
                + "\"custom-keyword1\":[\"x\",\"y\"]"
                + "},"
                + "\"property3\" : "
                + "{"
                + "\"title\": \"Property3\","
                + "\"type\": \"string\", "
                + "\"custom-keyword1\":[\"x\",\"y\"]"
                + "}"
                + "}"
                + "}";
    }

    private class ValidationThread implements Runnable {

        private String data;

        private String name;

        private ValidationResult validationResult;

        ValidationThread(String data, String name) {
            this.name = name;
            this.data = data;
        }

        @Override
        public void run() {
            try {
                this.validationResult = validate(data);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ValidationResult getValidationResult() {
            return this.validationResult;
        }

        @Override
        public String toString() {
            return "ValidationThread [data=" + data + ", name=" + name + ", validationResult=" + validationResult + "]";
        }

    }

    /**
     * Our own custom keyword. In this case we don't use this keyword. It is just
     * for demonstration purpose.
     */
    private class CustomKeyword implements Keyword {
        @Override
        public String getValue() {
            return "custom-keyword";
        }

        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                ValidationContext validationContext) throws JsonSchemaException, Exception {
            if (schemaNode != null && schemaNode.isArray()) {
                return new CustomValidator();
            }
            return null;
        }
    }

    /**
     * We will be collecting information/data by adding the data in the form of
     * collectors into collector context object while we are validating this node.
     * This will be helpful in cases where we don't want to revisit the entire JSON
     * document again just for gathering this kind of information.
     */
    private class CustomValidator implements JsonValidator {

        @Override
        public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
            // Get an instance of collector context.
            CollectorContext collectorContext = CollectorContext.getInstance();
            if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
                collectorContext.add(SAMPLE_COLLECTOR, new CustomCollector());
            }
            collectorContext.combineWithCollector(SAMPLE_COLLECTOR, node.textValue());
            return new TreeSet<ValidationMessage>();
        }

        @Override
        public Set<ValidationMessage> validate(JsonNode rootNode) {
            return validate(rootNode, rootNode, BaseJsonValidator.AT_ROOT);
        }

        @Override
        public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
            // Ignore this method for testing.
            return null;
        }
    }

    private class CustomCollector extends AbstractCollector<List<String>> {

        List<String> returnList = new ArrayList<String>();

        private Map<String, String> referenceMap = null;

        public CustomCollector() {
            referenceMap = getDatasourceMap();
        }

        @Override
        public List<String> collect() {
            return returnList;
        }

        @Override
        public void combine(Object object) {
            returnList.add(referenceMap.get((String) object));
        }

    }

    /**
     * Our own custom keyword. In this case we don't use this keyword. It is just
     * for demonstration purpose.
     */
    private class CustomKeyword1 implements Keyword {
        @Override
        public String getValue() {
            return "custom-keyword1";
        }

        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                ValidationContext validationContext) throws JsonSchemaException, Exception {
            if (schemaNode != null && schemaNode.isArray()) {
                return new CustomValidator1();
            }
            return null;
        }
    }

    /**
     * We will be collecting information/data by adding the data in the form of
     * collectors into collector context object while we are validating this node.
     * This will be helpful in cases where we don't want to revisit the entire JSON
     * document again just for gathering this kind of information. In this test case
     * we expect this validator to be called multiple times as the associated
     * keyword has been used multiple times in JSON Schema.
     */
    private class CustomValidator1 implements JsonValidator {
        @SuppressWarnings("unchecked")
        @Override
        public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
            // Get an instance of collector context.
            CollectorContext collectorContext = CollectorContext.getInstance();
            // If collector type is not added to context add one.
            if (collectorContext.get(SAMPLE_COLLECTOR_OTHER) == null) {
                collectorContext.add(SAMPLE_COLLECTOR_OTHER, new ArrayList<String>());
            }
            List<String> returnList = (List<String>) collectorContext.get(SAMPLE_COLLECTOR_OTHER);
            returnList.add(node.textValue());
            return new TreeSet<ValidationMessage>();
        }

        @Override
        public Set<ValidationMessage> validate(JsonNode rootNode) {
            return validate(rootNode, rootNode, BaseJsonValidator.AT_ROOT);
        }

        @Override
        public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
            // Ignore this method for testing.
            return null;
        }
    }

    private ValidationResult validate(String jsonData) throws JsonMappingException, JsonProcessingException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return this.jsonSchema.validateAndCollect(objectMapper.readTree(jsonData));
    }

    private Map<String, String> getDatasourceMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("sample1", "actual_value_added_to_context1");
        map.put("sample2", "actual_value_added_to_context2");
        map.put("sample3", "actual_value_added_to_context3");
        return map;
    }
}
