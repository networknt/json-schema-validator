package com.networknt.schema;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.WalkListener;
import com.networknt.schema.walk.KeywordWalkListenerRunner;
import com.networknt.schema.walk.WalkConfig;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

/**
 * Issue 918.
 */
class SharedConfigTest {
    private static class AllKeywordListener implements WalkListener {
        boolean wasCalled = false;

        @Override
        public WalkFlow onWalkStart(WalkEvent walkEvent) {
            wasCalled = true;
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
        }
    }

    @Test
    void shouldCallAllKeywordListenerOnWalkStart() throws Exception {

        AllKeywordListener allKeywordListener = new AllKeywordListener();
        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(allKeywordListener).build();
        WalkConfig walkConfig = WalkConfig.builder().keywordWalkListenerRunner(keywordWalkListenerRunner).build();

        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7);

        SchemaLocation draft07Schema = SchemaLocation.of("resource:/draft-07/schema#");

        // depending on this line the test either passes or fails:
        // - if this line is executed, then it passes
        // - if this line is not executed (just comment it) - it fails
        Schema firstSchema = factory.getSchema(draft07Schema);
        firstSchema.walk(new ObjectMapper().readTree("{ \"id\": 123 }"), true);

        // note that only second schema takes overridden schemaValidatorsConfig
        Schema secondSchema = factory.getSchema(draft07Schema);

        secondSchema.walk(new ObjectMapper().readTree("{ \"id\": 123 }"), true,
                executionContext -> executionContext.setWalkConfig(walkConfig));
        Assertions.assertTrue(allKeywordListener.wasCalled);
    }
}