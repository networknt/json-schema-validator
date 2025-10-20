package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.WalkListener;
import com.networknt.schema.walk.KeywordWalkHandler;
import com.networknt.schema.walk.WalkConfig;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

class Issue724Test {

    @Test
    void test() throws JsonProcessingException {
        StringCollector stringCollector = new StringCollector();
        KeywordWalkHandler keywordWalkHandler = KeywordWalkHandler.builder().keywordWalkListener(stringCollector).build();

        String schema =
            "{\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n"
                + "  \"type\" : \"object\",\n"
                + "  \"properties\" : {\n"
                + "    \"credit_card\": {\n"
                + "      \"type\" : \"string\"\n"
                + "    }\n"
                + "  },\n"
                + "  \"dependentSchemas\": {\n"
                + "    \"credit_card\": {\n"
                + "      \"properties\": {\n"
                + "        \"billing_address\": {\n"
                + "          \"type\" : \"string\"\n"
                + "        }\n"
                + "      },\n"
                + "      \"required\": [\"billing_address\"]\n"
                + "    }\n"
                + "  }\n"
                + "}\n";
        String data =
            "{\n"
                + "  \"credit_card\" : \"my_credit_card\",\n"
                + "  \"billing_address\" : \"my_billing_address\"\n"
                + "}\n";
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkHandler(keywordWalkHandler)
                .build();
        Schema jsonSchema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schema);
        jsonSchema.walk(new ObjectMapper().readTree(data), /* shouldValidateSchema= */ false, executionContext -> executionContext.setWalkConfig(walkConfig));

        System.out.println(stringCollector.strings);
        assertLinesMatch(Arrays.asList("my_credit_card", "my_billing_address"), stringCollector.strings);
    }

    static class StringCollector implements WalkListener {
        final List<String> strings = new ArrayList<>();

        @Override
        public WalkFlow onWalkStart(WalkEvent walkEvent) {
            boolean isString =
                Optional.of(walkEvent.getSchema().getSchemaNode())
                    .map(jsonNode -> jsonNode.get("type"))
                    .map(JsonNode::asText)
                    .map(type -> type.equals("string"))
                    .orElse(false);

            if (isString) {
                this.strings.add(walkEvent.getInstanceNode().asText());
            }

            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
            // nothing to do here
        }
    }

}
