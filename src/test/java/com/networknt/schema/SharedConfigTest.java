package com.networknt.schema;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

/**
 * Issue 918.
 */
class SharedConfigTest {
    private static class AllKeywordListener implements JsonSchemaWalkListener {
        boolean wasCalled = false;

        @Override
        public WalkFlow onWalkStart(WalkEvent walkEvent) {
            wasCalled = true;
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
        }
    }

    @Test
    void shouldCallAllKeywordListenerOnWalkStart() throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

        AllKeywordListener allKeywordListener = new AllKeywordListener();
        SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder()
                .keywordWalkListener(allKeywordListener)
                .build();

        SchemaLocation draft07Schema = SchemaLocation.of("resource:/draft-07/schema#");

        // depending on this line the test either passes or fails:
        // - if this line is executed, then it passes
        // - if this line is not executed (just comment it) - it fails
        JsonSchema firstSchema = factory.getSchema(draft07Schema);
        firstSchema.walk(new ObjectMapper().readTree("{ \"id\": 123 }"), true);

        // note that only second schema takes overridden schemaValidatorsConfig
        JsonSchema secondSchema = factory.getSchema(draft07Schema, schemaValidatorsConfig);

        secondSchema.walk(new ObjectMapper().readTree("{ \"id\": 123 }"), true);
        Assertions.assertTrue(allKeywordListener.wasCalled);
    }
}