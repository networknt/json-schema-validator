package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.keyword.KeywordType;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.walk.WalkListener;
import com.networknt.schema.walk.KeywordWalkHandler;
import com.networknt.schema.walk.WalkConfig;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class Issue461Test {
    protected ObjectMapper mapper = JsonMapperFactory.getInstance();

    protected Schema getJsonSchemaFromStreamContentV7(SchemaLocation schemaUri) {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        return factory.getSchema(schemaUri);
    }

    @Test
    void shouldWalkWithValidation() throws IOException {
    	KeywordWalkHandler keywordWalkHandler = KeywordWalkHandler.builder()
                .keywordWalkListener(KeywordType.PROPERTIES.getValue(), new Walker())
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkHandler(keywordWalkHandler)
                .build();

        Schema schema = getJsonSchemaFromStreamContentV7(SchemaLocation.of("resource:/draft-07/schema#"));
        JsonNode data = mapper.readTree(Issue461Test.class.getResource("/data/issue461-v7.json"));
        Result result = schema.walk(data, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        Assertions.assertTrue(result.getErrors().isEmpty());
    }

    /**
     * Example NOP walker
     */
    private static class Walker implements WalkListener {
        @Override
        public WalkFlow onWalkStart(final WalkEvent walkEvent) {
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(final WalkEvent walkEvent,
                              final List<Error> errors) {
        }
    }
}
