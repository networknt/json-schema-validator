package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.AbstractKeywordValidator;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.keyword.ValidatorTypeCode;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonWalkTest {

    private Schema jsonSchema;

    private Schema jsonSchema1;

    private static final String SAMPLE_WALK_COLLECTOR_TYPE = "sampleWalkCollectorType";

    private static final String CUSTOM_KEYWORD = "custom-keyword";

    @BeforeEach
    void setup() {
        setupSchema();
    }

    private void setupSchema() {
        final Dialect dialect = getJsonMetaSchema();
        // Create Schema.
        SchemaValidatorsConfig.Builder schemaValidatorsConfigBuilder = SchemaValidatorsConfig.builder();
        schemaValidatorsConfigBuilder.keywordWalkListener(new AllKeywordListener());
        schemaValidatorsConfigBuilder.keywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener());
        schemaValidatorsConfigBuilder.keywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(),
                new PropertiesKeywordListener());
        final SchemaRegistry schemaFactory = SchemaRegistry
                .builder(SchemaRegistry.getInstance(Specification.Version.DRAFT_2019_09)).metaSchema(dialect)
                .build();
        this.jsonSchema = schemaFactory.getSchema(getSchema(), schemaValidatorsConfigBuilder.build());
        // Create another Schema.
        SchemaValidatorsConfig.Builder schemaValidatorsConfig1Builder = SchemaValidatorsConfig.builder();
        schemaValidatorsConfig1Builder.keywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener());
        schemaValidatorsConfig1Builder.keywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(),
                new PropertiesKeywordListener());
        this.jsonSchema1 = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig1Builder.build());
    }

    private Dialect getJsonMetaSchema() {
        return Dialect.builder(
                "https://github.com/networknt/json-schema-validator/tests/schemas/example01", Dialects.getDraft201909())
                .keyword(new CustomKeyword()).build();
    }

    @Test
    void testWalk() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ValidationResult result = jsonSchema.walk(
                objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data.json")), false);
        JsonNode collectedNode = (JsonNode) result.getCollectorContext().get(SAMPLE_WALK_COLLECTOR_TYPE);
        assertEquals(collectedNode, (objectMapper.readTree("{" +
                "    \"PROPERTY1\": \"sample1\","
                + "    \"PROPERTY2\": \"sample2\","
                + "    \"property3\": {"
                + "        \"street_address\":\"test-address\","
                + "        \"phone_number\": {"
                + "            \"country-code\": \"091\","
                + "            \"number\": \"123456789\""
                + "          }"
                + "     }"
                + "}")));
    }

    @Test
    void testWalkWithDifferentListeners() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // This instance of schema contains all listeners.
        ValidationResult result = jsonSchema.walk(
                objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data.json")), false);
        JsonNode collectedNode = (JsonNode) result.getCollectorContext().get(SAMPLE_WALK_COLLECTOR_TYPE);
        assertEquals(collectedNode, (objectMapper.readTree("{" +
                "    \"PROPERTY1\": \"sample1\","
                + "    \"PROPERTY2\": \"sample2\","
                + "    \"property3\": {"
                + "        \"street_address\":\"test-address\","
                + "        \"phone_number\": {"
                + "            \"country-code\": \"091\","
                + "            \"number\": \"123456789\""
                + "          }"
                + "     }"
                + "}")));
        // This instance of schema contains one listener removed.
        result = jsonSchema1.walk(objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data.json")), false);
        collectedNode = (JsonNode) result.getExecutionContext().getCollectorContext().get(SAMPLE_WALK_COLLECTOR_TYPE);
        assertEquals(collectedNode, (objectMapper.readTree("{"
                + "    \"property3\": {"
                + "        \"street_address\":\"test-address\","
                + "        \"phone_number\": {"
                + "            \"country-code\": \"091\","
                + "            \"number\": \"123456789\""
                + "          }"
                + "     }"
                + "}")));
    }

    @Test
    void testWalkMissingNodeWithPropertiesSchemaShouldNotThrow() {
        String schemaContents = "{\n"
                + "                \"type\": \"object\",\n"
                + "                \"properties\": {\n"
                + "                    \"field\": {\n"
                + "                    \"anyOf\": [\n"
                + "                        {\n"
                + "                        \"type\": \"string\"\n"
                + "                        }\n"
                + "                    ]\n"
                + "                    }\n"
                + "                }\n"
                + "            }";

        SchemaRegistry factory = SchemaRegistry.getInstance(Specification.Version.DRAFT_7);
        Schema schema = factory.getSchema(schemaContents);
        JsonNode missingNode = MissingNode.getInstance();
        assertDoesNotThrow(() -> schema.walk(missingNode, true));
    }

    private InputStream getSchema() {
        return getClass().getClassLoader().getResourceAsStream("schema/walk-schema.json");
    }

    /**
     * Our own custom keyword.
     */
    private static class CustomKeyword implements Keyword {
        @Override
        public String getValue() {
            return "custom-keyword";
        }

        @Override
        public KeywordValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                                          Schema parentSchema, ValidationContext validationContext) throws JsonSchemaException {
            if (schemaNode != null && schemaNode.isArray()) {
                return new CustomValidator(schemaLocation, evaluationPath, schemaNode);
            }
            return null;
        }

        /**
         * We will be collecting information/data by adding the data in the form of
         * collectors into collector context object while we are validating this node.
         * This will be helpful in cases where we don't want to revisit the entire JSON
         * document again just for gathering this kind of information.
         */
        private static class CustomValidator extends AbstractKeywordValidator {

            CustomValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode) {
                super(new CustomKeyword(), schemaNode, schemaLocation, evaluationPath);
            }

            @Override
            public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
                return;
            }

            @Override
            public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                    JsonNodePath instanceLocation, boolean shouldValidateSchema) {
                return;
            }
        }
    }

    private static class AllKeywordListener implements JsonSchemaWalkListener {
        @Override
        public WalkFlow onWalkStart(WalkEvent keywordWalkEvent) {
            ObjectMapper mapper = new ObjectMapper();
            String keyWordName = keywordWalkEvent.getKeyword();
            JsonNode schemaNode = keywordWalkEvent.getSchema().getSchemaNode();
            CollectorContext collectorContext = keywordWalkEvent.getExecutionContext().getCollectorContext();
            if (collectorContext.get(SAMPLE_WALK_COLLECTOR_TYPE) == null) {
                collectorContext.add(SAMPLE_WALK_COLLECTOR_TYPE, mapper.createObjectNode());
            }
            if (keyWordName.equals(CUSTOM_KEYWORD) && schemaNode.get(CUSTOM_KEYWORD).isArray()) {
                ObjectNode objectNode = (ObjectNode) collectorContext.get(SAMPLE_WALK_COLLECTOR_TYPE);
                objectNode.put(keywordWalkEvent.getSchema().getSchemaNode().get("title").textValue().toUpperCase(),
                        keywordWalkEvent.getInstanceNode().textValue());
            }
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, List<Error> errors) {

        }
    }

    private static class RefKeywordListener implements JsonSchemaWalkListener {

        @Override
        public WalkFlow onWalkStart(WalkEvent keywordWalkEvent) {
            ObjectMapper mapper = new ObjectMapper();
            CollectorContext collectorContext = keywordWalkEvent.getExecutionContext().getCollectorContext();
            if (collectorContext.get(SAMPLE_WALK_COLLECTOR_TYPE) == null) {
                collectorContext.add(SAMPLE_WALK_COLLECTOR_TYPE, mapper.createObjectNode());
            }
            ObjectNode objectNode = (ObjectNode) collectorContext.get(SAMPLE_WALK_COLLECTOR_TYPE);
            objectNode.set(keywordWalkEvent.getSchema().getSchemaNode().get("title").textValue().toLowerCase(),
                    keywordWalkEvent.getInstanceNode());
            return WalkFlow.SKIP;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, List<Error> errors) {

        }
    }

    private static class PropertiesKeywordListener implements JsonSchemaWalkListener {

        @Override
        public WalkFlow onWalkStart(WalkEvent keywordWalkEvent) {
            JsonNode schemaNode = keywordWalkEvent.getSchema().getSchemaNode();
            if (schemaNode.get("title").textValue().equals("Property3")) {
                return WalkFlow.SKIP;
            }
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, List<Error> errors) {

        }
    }

}
