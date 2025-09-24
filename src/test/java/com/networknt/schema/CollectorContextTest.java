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
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.format.Format;
import com.networknt.schema.keyword.AbstractKeywordValidator;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.keyword.KeywordValidator;

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
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

class CollectorContextTest {
    enum Data {
        SAMPLE_COLLECTOR,
        SAMPLE_COLLECTOR_OTHER
    }

    private Schema jsonSchema;

    private Schema jsonSchemaForCombine;
    
    @BeforeEach
    void setup() throws Exception {
        setupSchema();
    }

    @Test
    void testCollectorContextWithKeyword() throws Exception {
        Result validationResult = validate("{\"test-property1\":\"sample1\",\"test-property2\":\"sample2\"}");
        Assertions.assertEquals(0, validationResult.getErrors().size());
        List<String> contextValues = validationResult.getCollectorContext().get(Data.SAMPLE_COLLECTOR);
        contextValues.sort(null);
        Assertions.assertEquals(0, validationResult.getErrors().size());
        Assertions.assertEquals(2, contextValues.size());
        Assertions.assertEquals(contextValues.get(0), "actual_value_added_to_context1");
        Assertions.assertEquals(contextValues.get(1), "actual_value_added_to_context2");
    }

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

        Result validationResult1 = validationRunnable1.getValidationResult();
        Result validationResult2 = validationRunnable2.getValidationResult();
        Result validationResult3 = validationRunnable3.getValidationResult();

        Assertions.assertEquals(0, validationResult1.getErrors().size());
        Assertions.assertEquals(0, validationResult2.getErrors().size());
        Assertions.assertEquals(0, validationResult3.getErrors().size());

        List<String> contextValue1 = validationResult1.getCollectorContext().get(Data.SAMPLE_COLLECTOR);
        List<String> contextValue2 = validationResult2.getCollectorContext().get(Data.SAMPLE_COLLECTOR);
        List<String> contextValue3 = validationResult3.getCollectorContext().get(Data.SAMPLE_COLLECTOR);

        Assertions.assertEquals(contextValue1.get(0), "actual_value_added_to_context1");
        Assertions.assertEquals(contextValue2.get(0), "actual_value_added_to_context2");
        Assertions.assertEquals(contextValue3.get(0), "actual_value_added_to_context3");
    }

    @Test
    void testCollectorGetAll() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExecutionContext executionContext = jsonSchemaForCombine.createExecutionContext();
        executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        jsonSchemaForCombine.validate(executionContext, objectMapper
                .readTree("{\"property1\":\"sample1\",\"property2\":\"sample2\",\"property3\":\"sample3\" }"));
        Result validationResult = new Result(executionContext);
        CollectorContext collectorContext = validationResult.getCollectorContext();
        List<String> sampleCollector = collectorContext.get(Data.SAMPLE_COLLECTOR);
        List<String> sampleCollectorOther = collectorContext.get(Data.SAMPLE_COLLECTOR_OTHER);
        Assertions.assertEquals(sampleCollector.size(), 1);
        Assertions.assertEquals(sampleCollectorOther.size(), 3);
    }

    private Dialect getDialect(String uri) throws Exception {
        Dialect dialect = Dialect.builder(uri, Dialects.getDraft201909())
                .keyword(new CustomKeyword()).keyword(new CustomKeyword1()).format(new Format() {
					@Override
					public boolean matches(ExecutionContext executionContext, String value) {
						CollectorContext collectorContext = executionContext.getCollectorContext();
                        List<String> returnList = collectorContext.computeIfAbsent(Data.SAMPLE_COLLECTOR,
                                key -> new ArrayList<String>());
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
        return dialect;
    }

    private void setupSchema() throws Exception {
        final Dialect dialect = getDialect(
                "https://github.com/networknt/json-schema-validator/tests/schemas/example01");
        final SchemaRegistry schemaFactory = SchemaRegistry.withDialect(dialect);
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

        private final String data;

        private final String name;

        private Result validationResult;
        
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

        Result getValidationResult() {
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
        public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
                Schema parentSchema, SchemaContext schemaContext) throws SchemaException, Exception {
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
    private class CustomValidator extends AbstractKeywordValidator {
    	private final CustomCollector customCollector = new CustomCollector();
        public CustomValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode) {
            super(new CustomKeyword(), schemaNode, schemaLocation, evaluationPath);
        }

		@Override
		public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
				NodePath instanceLocation) {
			CollectorContext collectorContext = executionContext.getCollectorContext();
			List<String> result = collectorContext.computeIfAbsent(Data.SAMPLE_COLLECTOR,
					key -> customCollector.supplier().get());
			customCollector.accumulator().accept(result, node);
		}

        @Override
        public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
            // Ignore this method for testing.
        }
    }
    private class CustomCollector implements Collector<JsonNode, List<String>, List<String>> {
        private Map<String, String> referenceMap = null;

        public CustomCollector() {
            this(getDatasourceMap());
        }
        
        public CustomCollector(Map<String, String> referenceMap) {
            this.referenceMap = referenceMap;
        }

		@Override
		public Supplier<List<String>> supplier() {
			return ArrayList::new;
		}

		@Override
		public BiConsumer<List<String>, JsonNode> accumulator() {
			return (returnList, instanceNode) -> {
	            synchronized (returnList) {
	                returnList.add(referenceMap.get(instanceNode.textValue()));
	            }
			};
		}

		@Override
		public BinaryOperator<List<String>> combiner() {
	        return (left, right) -> {
	            left.addAll(right);
	            return left;
	        };
		}

		@Override
		public Function<List<String>, List<String>> finisher() {
			return Function.identity();
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
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
        public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
                Schema parentSchema, SchemaContext schemaContext) throws SchemaException, Exception {
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
    private class CustomValidator1 extends AbstractKeywordValidator {
        public CustomValidator1(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode) {
            super(new CustomKeyword(), schemaNode,schemaLocation, evaluationPath);
        }

        @Override
        public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                NodePath instanceLocation) {
            // Get an instance of collector context.
            CollectorContext collectorContext = executionContext.getCollectorContext();
            // If collector type is not added to context add one.
            List<String> returnList = collectorContext.computeIfAbsent(Data.SAMPLE_COLLECTOR_OTHER,
                    key -> new ArrayList<String>());
            synchronized (returnList) {
                returnList.add(node.textValue());
            }
        }

        @Override
        public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
            // Ignore this method for testing.
        }
    }

    private Result validate(String jsonData) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ExecutionContext executionContext = this.jsonSchema.createExecutionContext();
        this.jsonSchema.validate(executionContext, objectMapper.readTree(jsonData));
        return new Result(executionContext);
    }

    protected static Map<String, String> getDatasourceMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("sample1", "actual_value_added_to_context1");
        map.put("sample2", "actual_value_added_to_context2");
        map.put("sample3", "actual_value_added_to_context3");
        return map;
    }

    @Test
    void constructor() {
        CollectorContext context = new CollectorContext();
        assertTrue(context.getData().isEmpty());
    }

    @Test
    void constructorWithMap() {
        ConcurrentHashMap<Object, Object> data = new ConcurrentHashMap<>();
        CollectorContext context = new CollectorContext(data);
        assertSame(data, context.getData());
    }

    private class CollectKeyword implements Keyword {
        @Override
        public String getValue() {
            return "collect";
        }

        @Override
        public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
                Schema parentSchema, SchemaContext schemaContext) throws SchemaException, Exception {
            if (schemaNode != null && schemaNode.isBoolean()) {
                return new CollectValidator(schemaLocation, evaluationPath, schemaNode);
            }
            return null;
        }
    }

    private class CollectValidator extends AbstractKeywordValidator {
        CollectValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode) {
            super(new CollectKeyword(), schemaNode, schemaLocation, evaluationPath);
        }

        @Override
        public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
            // Get an instance of collector context.
            CollectorContext collectorContext = executionContext.getCollectorContext();
            AtomicInteger count = collectorContext.computeIfAbsent("collect",
                    (key) -> new AtomicInteger(0));
            count.incrementAndGet();
        }

        @Override
        public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                NodePath instanceLocation, boolean shouldValidateSchema) {
            if (!shouldValidateSchema) {
                CollectorContext collectorContext = executionContext.getCollectorContext();
                AtomicInteger count = (AtomicInteger) collectorContext.getData().computeIfAbsent("collect",
                        (key) -> new AtomicInteger(0));
                count.incrementAndGet();
            }
            super.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
        }
    }

    @Test
    void concurrency() throws Exception {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>());
        Dialect dialect = Dialect.builder(Dialects.getDraft202012()).keyword(new CollectKeyword()).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(dialect);
        Schema schema = factory.getSchema("{\n"
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
        AtomicInteger result = collectorContext.get("collect");
        assertEquals(50, result.get());
    }

    @Test
    void iterate() {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>());
        Dialect dialect = Dialect.builder(Dialects.getDraft202012()).keyword(new CollectKeyword()).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(dialect);
        Schema schema = factory.getSchema("{\n"
                + "  \"collect\": true\n"
                + "}");
        for (int i = 0; i < 50; ++i) {
            schema.validate("1", InputFormat.JSON, executionContext -> {
                executionContext.setCollectorContext(collectorContext);
            });
        }
        AtomicInteger result = collectorContext.get("collect");
        assertEquals(50, result.get());
    }

    @Test
    void iterateWalk() {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>());
        Dialect dialect = Dialect.builder(Dialects.getDraft202012()).keyword(new CollectKeyword()).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(dialect);
        Schema schema = factory.getSchema("{\n"
                + "  \"collect\": true\n"
                + "}");
        for (int i = 0; i < 50; ++i) {
            schema.walk("1", InputFormat.JSON, false, executionContext -> {
                executionContext.setCollectorContext(collectorContext);
            });
        }
        AtomicInteger result = collectorContext.get("collect");
        assertEquals(50, result.get());
    }

    @Test
    void iterateWalkValidate() {
        CollectorContext collectorContext = new CollectorContext(new ConcurrentHashMap<>());
        Dialect dialect = Dialect.builder(Dialects.getDraft202012()).keyword(new CollectKeyword()).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(dialect);
        Schema schema = factory.getSchema("{\n"
                + "  \"collect\": true\n"
                + "}");
        for (int i = 0; i < 50; ++i) {
            schema.walk("1", InputFormat.JSON, true, executionContext -> {
                executionContext.setCollectorContext(collectorContext);
            });
        }
        AtomicInteger result = collectorContext.get("collect");
        assertEquals(50, result.get());
    }

}
