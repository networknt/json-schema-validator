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
import com.networknt.schema.SpecVersion.VersionFlag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class CollectorContextTest {

    private static final String SAMPLE_COLLECTOR = "sampleCollector";

    private static final String SAMPLE_COLLECTOR_OTHER = "sampleCollectorOther";

    private JsonSchema jsonSchema;

    private JsonSchema jsonSchemaForCombine;
    
    @BeforeEach
    void setup() throws Exception {
        setupSchema();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCollectorContextWithKeyword() throws Exception {
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
    void testCollectorContextWithMultipleThreads() throws Exception {

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
    void testCollectorGetAll() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExecutionContext executionContext = jsonSchemaForCombine.createExecutionContext();
        executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        Set<ValidationMessage> messages = jsonSchemaForCombine.validate(executionContext, objectMapper
                .readTree("{\"property1\":\"sample1\",\"property2\":\"sample2\",\"property3\":\"sample3\" }"));
        ValidationResult validationResult = new ValidationResult(messages, executionContext);
        CollectorContext collectorContext = validationResult.getCollectorContext();
        collectorContext.loadCollectors();
        Assertions.assertEquals(((List<String>) collectorContext.get(SAMPLE_COLLECTOR)).size(), 1);
        Assertions.assertEquals(((List<String>) collectorContext.get(SAMPLE_COLLECTOR_OTHER)).size(), 3);
    }
    
    private JsonMetaSchema getJsonMetaSchema(String uri) throws Exception {
        JsonMetaSchema jsonMetaSchema = JsonMetaSchema.builder(uri, JsonMetaSchema.getV201909())
                .keyword(new CustomKeyword()).keyword(new CustomKeyword1()).format(new Format() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public boolean matches(ExecutionContext executionContext, String value) {
                        CollectorContext collectorContext = executionContext.getCollectorContext();
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
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).metaSchema(metaSchema)
                .build();
        SchemaValidatorsConfig schemaValidatorsConfig  = SchemaValidatorsConfig.builder().build();
        this.jsonSchema = schemaFactory.getSchema(getSchemaString(), schemaValidatorsConfig);
        this.jsonSchemaForCombine = schemaFactory.getSchema(getSchemaStringMultipleProperties(), schemaValidatorsConfig);
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

        private final String data;

        private final String name;

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
        public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException, Exception {
            if (schemaNode != null && schemaNode.isArray()) {
                return new CustomValidator(schemaLocation, evaluationPath, schemaNode);
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
    private class CustomValidator extends AbstractJsonValidator {
        public CustomValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode) {
            super(schemaLocation, evaluationPath, new CustomKeyword(), schemaNode);
        }

        @Override
        public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                JsonNodePath instanceLocation) {
            CollectorContext collectorContext = executionContext.getCollectorContext();
            CustomCollector customCollector = (CustomCollector) collectorContext.getCollectorMap().computeIfAbsent(SAMPLE_COLLECTOR,
                    key -> new CustomCollector());
            customCollector.combine(node.textValue());
            return Collections.emptySet();
        }

        @Override
        public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
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
            synchronized (returnList) {
                returnList.add(referenceMap.get((String) object));
            }
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
        public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException, Exception {
            if (schemaNode != null && schemaNode.isArray()) {
                return new CustomValidator1(schemaLocation, evaluationPath, schemaNode);
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
    private class CustomValidator1 extends AbstractJsonValidator {
        public CustomValidator1(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode) {
            super(schemaLocation, evaluationPath,new CustomKeyword(), schemaNode);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
            // Get an instance of collector context.
            CollectorContext collectorContext = executionContext.getCollectorContext();
            // If collector type is not added to context add one.
            List<String> returnList = (List<String>) collectorContext.getCollectorMap()
                    .computeIfAbsent(SAMPLE_COLLECTOR_OTHER, key -> new ArrayList<String>());
            synchronized(returnList) {
                returnList.add(node.textValue());
            }
            return Collections.emptySet();
        }

        @Override
        public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
            // Ignore this method for testing.
            return null;
        }
    }

    private ValidationResult validate(String jsonData) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExecutionContext executionContext = this.jsonSchema.createExecutionContext();
        Set<ValidationMessage> messages = this.jsonSchema.validate(executionContext, objectMapper.readTree(jsonData));
        executionContext.getCollectorContext().loadCollectors();
        return new ValidationResult(messages, executionContext);
    }

    private Map<String, String> getDatasourceMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("sample1", "actual_value_added_to_context1");
        map.put("sample2", "actual_value_added_to_context2");
        map.put("sample3", "actual_value_added_to_context3");
        return map;
    }

    @Test
    void constructor() {
        CollectorContext context = new CollectorContext();
        assertTrue(context.getCollectorMap().isEmpty());
        assertTrue(context.getAll().isEmpty());
    }

    @Test
    void constructorWithMap() {
        ConcurrentHashMap<String, Object> collectorMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Object> collectorLoadMap = new ConcurrentHashMap<>();
        CollectorContext context = new CollectorContext(collectorMap, collectorLoadMap);
        assertSame(collectorMap, context.getCollectorMap());
    }

    private class CollectKeyword implements Keyword {
        @Override
        public String getValue() {
            return "collect";
        }

        @Override
        public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException, Exception {
            if (schemaNode != null && schemaNode.isBoolean()) {
                return new CollectValidator(schemaLocation, evaluationPath, schemaNode);
            }
            return null;
        }
    }

    private class CollectValidator extends AbstractJsonValidator {
        CollectValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode) {
            super(schemaLocation, evaluationPath, new CollectKeyword(), schemaNode);
        }

        @Override
        public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
            // Get an instance of collector context.
            CollectorContext collectorContext = executionContext.getCollectorContext();
            AtomicInteger count = (AtomicInteger) collectorContext.getCollectorMap().computeIfAbsent("collect",
                    (key) -> new AtomicInteger(0));
            count.incrementAndGet();
            return Collections.emptySet();
        }

        @Override
        public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                JsonNodePath instanceLocation, boolean shouldValidateSchema) {
            if (!shouldValidateSchema) {
                CollectorContext collectorContext = executionContext.getCollectorContext();
                AtomicInteger count = (AtomicInteger) collectorContext.getCollectorMap().computeIfAbsent("collect",
                        (key) -> new AtomicInteger(0));
                count.incrementAndGet();
            }
            return super.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
        }
    }

    @Test
    void concurrency() throws Exception {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012()).keyword(new CollectKeyword()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
        JsonSchema schema = factory.getSchema("{\n"
                + "  \"collect\": true\n"
                + "}");
        Exception[] instance = new Exception[1];
        CountDownLatch latch = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 50; ++i) {
            Runnable runner = new Runnable() {
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        schema.validate("1", InputFormat.JSON, executionContext -> {
                            executionContext.setCollectorContext(collectorContext);
                        });
                    } catch (RuntimeException e) {
                        instance[0] = e;
                    }
                }
            };
            Thread thread = new Thread(runner, "Thread" + i);
            thread.start();
            threads.add(thread);
        }
        latch.countDown(); // Release the latch for threads to run concurrently
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        if (instance[0] != null) {
            throw instance[0];
        }
        collectorContext.loadCollectors();
        AtomicInteger result = (AtomicInteger) collectorContext.get("collect");
        assertEquals(50, result.get());
    }

    @Test
    void iterate() {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012()).keyword(new CollectKeyword()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
        JsonSchema schema = factory.getSchema("{\n"
                + "  \"collect\": true\n"
                + "}");
        for (int i = 0; i < 50; ++i) {
            schema.validate("1", InputFormat.JSON, executionContext -> {
                executionContext.setCollectorContext(collectorContext);
            });
        }
        collectorContext.loadCollectors();
        AtomicInteger result = (AtomicInteger) collectorContext.get("collect");
        assertEquals(50, result.get());
    }

    @Test
    void iterateWalk() {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012()).keyword(new CollectKeyword()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
        JsonSchema schema = factory.getSchema("{\n"
                + "  \"collect\": true\n"
                + "}");
        for (int i = 0; i < 50; ++i) {
            schema.walk("1", InputFormat.JSON, false, executionContext -> {
                executionContext.setCollectorContext(collectorContext);
            });
        }
        collectorContext.loadCollectors();
        AtomicInteger result = (AtomicInteger) collectorContext.get("collect");
        assertEquals(50, result.get());
    }

    @Test
    void iterateWalkValidate() {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012()).keyword(new CollectKeyword()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
        JsonSchema schema = factory.getSchema("{\n"
                + "  \"collect\": true\n"
                + "}");
        for (int i = 0; i < 50; ++i) {
            schema.walk("1", InputFormat.JSON, true, executionContext -> {
                executionContext.setCollectorContext(collectorContext);
            });
        }
        collectorContext.loadCollectors();
        AtomicInteger result = (AtomicInteger) collectorContext.get("collect");
        assertEquals(50, result.get());
    }

}
