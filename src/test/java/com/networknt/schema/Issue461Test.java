package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Issue461Test {
    protected ObjectMapper mapper = new ObjectMapper();

    protected JsonSchema getJsonSchemaFromStreamContentV7(URI schemaUri) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        SchemaValidatorsConfig svc = new SchemaValidatorsConfig();
        svc.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new Issue461Test.Walker());
        return factory.getSchema(schemaUri, svc);
    }

    @Test
    public void shouldWalkWithValidation() throws URISyntaxException, IOException {
        JsonSchema schema = getJsonSchemaFromStreamContentV7(new URI("http://json-schema" +
                ".org/draft-07/schema#"));
        JsonNode data = mapper.readTree(Issue461Test.class.getResource("/data/issue461-v7.json"));
        ValidationResult result = schema.walk(data, true);
        Assertions.assertTrue(result.getValidationMessages().isEmpty());
    }


    private static class Property {
        private final String name;
        private final String path;

        private Property(String name, String at) {
            this.name = name;
            this.path = at;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }

    private class Walker implements JsonSchemaWalkListener {
        private final List<Property> path = new ArrayList<>();
        @Override
        public WalkFlow onWalkStart(final WalkEvent walkEvent) {
            // This method is more confusing than it should be on first blush. The reason for this is
            // that technically properties can have the literal name "properties" and can also contain
            // periods which means we need complex logic as we can use simple string splitting or
            // jsonpath. We have to track the state of our recursive descent to strip out known prefixes
            // from our current path.

            // The first event is always the outer object properties.
            if (walkEvent.getAt().equalsIgnoreCase("$")) {
                path.add(new Property("properties", "$.properties"));
                return WalkFlow.CONTINUE;
            }

            final Property previous = path.get(path.size() - 1);
            final String path = walkEvent.getAt();

            String name = StringUtils.removeStart(path, previous.getPath() + ".");
            final Property nextProperty = new Property(name, walkEvent.getAt() + ".properties");
            this.path.add(nextProperty);

            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(final WalkEvent walkEvent,
                              final Set<ValidationMessage> validationMessages) {
            this.path.remove(path.size() - 1);
        }
    }
}
