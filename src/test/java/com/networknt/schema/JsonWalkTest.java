package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonWalkTest {

    private JsonSchema jsonSchema;

    private JsonSchema jsonSchema1;

    private static final String SAMPLE_WALK_COLLECTOR_TYPE = "sampleWalkCollectorType";

    private static final String CUSTOM_KEYWORD = "custom-keyword";

    @BeforeEach
    public void setup() {
        setupSchema();
    }

    private void setupSchema() {
        final JsonMetaSchema metaSchema = getJsonMetaSchema();
        // Create Schema.
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.addKeywordWalkListener(new AllKeywordListener());
        schemaValidatorsConfig.addKeywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener());
        schemaValidatorsConfig.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(),
                new PropertiesKeywordListener());
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
                .build();
        this.jsonSchema = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig);
        // Create another Schema.
        SchemaValidatorsConfig schemaValidatorsConfig1 = new SchemaValidatorsConfig();
        schemaValidatorsConfig1.addKeywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener());
        schemaValidatorsConfig1.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(),
                new PropertiesKeywordListener());
        this.jsonSchema1 = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig1);
    }

    private JsonMetaSchema getJsonMetaSchema() {
        return JsonMetaSchema.builder(
                "https://github.com/networknt/json-schema-validator/tests/schemas/example01", JsonMetaSchema.getV201909())
                .addKeyword(new CustomKeyword()).build();
    }

    @Test
    public void testWalk() throws IOException {
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
    public void testWalkWithDifferentListeners() throws IOException {
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
        public JsonValidator newValidator(JsonNodePath schemaPath, JsonNodePath validationPath, JsonNode schemaNode,
                                          JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException {
            if (schemaNode != null && schemaNode.isArray()) {
                return new CustomValidator();
            }
            return null;
        }

        /**
         * We will be collecting information/data by adding the data in the form of
         * collectors into collector context object while we are validating this node.
         * This will be helpful in cases where we don't want to revisit the entire JSON
         * document again just for gathering this kind of information.
         */
        private static class CustomValidator implements JsonValidator {

            @Override
            public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
                return new TreeSet<ValidationMessage>();
            }

            @Override
            public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                    JsonNodePath at, boolean shouldValidateSchema) {
                return new LinkedHashSet<ValidationMessage>();
            }
        }
    }

    private static class AllKeywordListener implements JsonSchemaWalkListener {
        @Override
        public WalkFlow onWalkStart(WalkEvent keywordWalkEvent) {
            ObjectMapper mapper = new ObjectMapper();
            String keyWordName = keywordWalkEvent.getKeyWordName();
            JsonNode schemaNode = keywordWalkEvent.getSchemaNode();
            CollectorContext collectorContext = keywordWalkEvent.getExecutionContext().getCollectorContext();
            if (collectorContext.get(SAMPLE_WALK_COLLECTOR_TYPE) == null) {
                collectorContext.add(SAMPLE_WALK_COLLECTOR_TYPE, mapper.createObjectNode());
            }
            if (keyWordName.equals(CUSTOM_KEYWORD) && schemaNode.get(CUSTOM_KEYWORD).isArray()) {
                ObjectNode objectNode = (ObjectNode) collectorContext.get(SAMPLE_WALK_COLLECTOR_TYPE);
                objectNode.put(keywordWalkEvent.getSchemaNode().get("title").textValue().toUpperCase(),
                        keywordWalkEvent.getNode().textValue());
            }
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {

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
            objectNode.set(keywordWalkEvent.getSchemaNode().get("title").textValue().toLowerCase(),
                    keywordWalkEvent.getNode());
            return WalkFlow.SKIP;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {

        }
    }

    private static class PropertiesKeywordListener implements JsonSchemaWalkListener {

        @Override
        public WalkFlow onWalkStart(WalkEvent keywordWalkEvent) {
            JsonNode schemaNode = keywordWalkEvent.getSchemaNode();
            if (schemaNode.get("title").textValue().equals("Property3")) {
                return WalkFlow.SKIP;
            }
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {

        }
    }

}
