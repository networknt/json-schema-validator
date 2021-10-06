package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Validating anyOf walker
 */
@SuppressWarnings("unchecked")
public class Issue451Test {

    private static final String COLLECTOR_ID = "collector-451";

    protected JsonSchema getJsonSchemaFromStreamContentV7(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        SchemaValidatorsConfig svc = new SchemaValidatorsConfig();
        svc.addPropertyWalkListener(new CountingWalker());
        return factory.getSchema(schemaContent, svc);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @AfterEach
    public void cleanup() {
        if (CollectorContext.getInstance() != null) {
            CollectorContext.getInstance().reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldWalkAnyOfProperties() {
        walk(null, false);
    }

    @Disabled
    @Test
    public void shouldWalkAnyOfPropertiesWithWithPayloadAndValidation() throws Exception {
        JsonNode data = getJsonNodeFromStreamContent(Issue451Test.class.getResourceAsStream(
                "/data/issue451.json"));
        walk(data,true);
    }

    @Test
    public void shouldWalkAnyOfPropertiesWithWithPayload() throws Exception {
        JsonNode data = getJsonNodeFromStreamContent(Issue451Test.class.getResourceAsStream(
                "/data/issue451.json"));
        walk(data, false);
    }

    private void walk(JsonNode data, boolean shouldValidate) {
        String schemaPath = "/schema/issue451-v7.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);

        schema.walk(data, shouldValidate);

        Map<String, Integer> collector = (Map<String, Integer>) CollectorContext.getInstance().get(COLLECTOR_ID);
        Assertions.assertEquals(2, collector.get("#/definitions/definition1/properties/a"));
        Assertions.assertEquals(2, collector.get("#/definitions/definition2/properties/x"));
    }


    private static class CountingWalker implements JsonSchemaWalkListener {
        @Override
        public WalkFlow onWalkStart(WalkEvent walkEvent) {
            String path = walkEvent.getSchemaPath();
            collector().compute(path, (k, v) -> v == null ? 1 : v + 1);
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {

        }

        private Map<String, Integer> collector() {
            Map<String, Integer> collector = (Map<String, Integer>) CollectorContext.getInstance().get(COLLECTOR_ID);
            if(collector == null) {
                collector = new HashMap<>();
                CollectorContext.getInstance().add(COLLECTOR_ID, collector);
            }

            return collector;
        }
    }
}

