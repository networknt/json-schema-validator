package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class Issue461Test {
    protected ObjectMapper mapper = new ObjectMapper();

    protected JsonSchema getJsonSchemaFromStreamContentV7(SchemaLocation schemaUri) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        SchemaValidatorsConfig svc = new SchemaValidatorsConfig();
        svc.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new Walker());
        return factory.getSchema(schemaUri, svc);
    }

    @Test
    public void shouldWalkWithValidation() throws URISyntaxException, IOException {
        JsonSchema schema = getJsonSchemaFromStreamContentV7(SchemaLocation.of("resource:/draft-07/schema#"));
        JsonNode data = mapper.readTree(Issue461Test.class.getResource("/data/issue461-v7.json"));
        ValidationResult result = schema.walk(data, true);
        Assertions.assertTrue(result.getValidationMessages().isEmpty());
    }

    /**
     * Example NOP walker
     */
    private static class Walker implements JsonSchemaWalkListener {
        @Override
        public WalkFlow onWalkStart(final WalkEvent walkEvent) {
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(final WalkEvent walkEvent,
                              final Set<ValidationMessage> validationMessages) {
        }
    }
}
