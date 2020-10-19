package com.networknt.schema;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.networknt.schema.walk.WalkFlow;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.JsonSchemaWalkListener;

public class JsonWalkTest {

    private JsonSchema jsonSchema;

    private static final String SAMPLE_COLLECTOR = "sampleCollectorType";

    private static final String CUSTOM_KEYWORD = "custom-keyword";

    @Before
    public void setup() {
        setupSchema();
    }

    private void setupSchema() {
        final JsonMetaSchema metaSchema = getJsonMetaSchema();
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.addKeywordWalkListener(new AllKeywordListener());
        schemaValidatorsConfig.addKeywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener());
        schemaValidatorsConfig.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(),
                new PropertiesKeywordListener());
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
                .build();
        this.jsonSchema = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig);
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
        JsonNode collectedNode = (JsonNode) result.getCollectorContext().get(SAMPLE_COLLECTOR);
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
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                          ValidationContext validationContext) throws JsonSchemaException {
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
            public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
                return new TreeSet<ValidationMessage>();
            }

            @Override
            public Set<ValidationMessage> validate(JsonNode rootNode) {
                return validate(rootNode, rootNode, BaseJsonValidator.AT_ROOT);
            }

            @Override
            public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at,
                                               boolean shouldValidateSchema) {
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
            CollectorContext collectorContext = CollectorContext.getInstance();
            if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
                collectorContext.add(SAMPLE_COLLECTOR, mapper.createObjectNode());
            }
            if (keyWordName.equals(CUSTOM_KEYWORD) && schemaNode.get(CUSTOM_KEYWORD).isArray()) {
                ObjectNode objectNode = (ObjectNode) collectorContext.get(SAMPLE_COLLECTOR);
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
            CollectorContext collectorContext = CollectorContext.getInstance();
            if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
                collectorContext.add(SAMPLE_COLLECTOR, mapper.createObjectNode());
            }
            ObjectNode objectNode = (ObjectNode) collectorContext.get(SAMPLE_COLLECTOR);
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
