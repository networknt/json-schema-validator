package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Validating anyOf walker
 */
public class Issue451Test {
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

    @SuppressWarnings("unchecked")
    @Test
    public void shouldWalkAnyOfProperties() throws Exception {
        String schemaPath = "/schema/issue451-v7.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);

        schema.walk(null, false);

        Map<String, Integer> collector = (Map<String, Integer>) CollectorContext.getInstance().get("collector-451");
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
            Map<String, Integer> collector = (Map<String, Integer>) CollectorContext.getInstance().get("collector-451");
            if(collector == null) {
                collector = new HashMap<>();
                CollectorContext.getInstance().add("collector-451", collector);
            }

            return collector;
        }
    }
}

