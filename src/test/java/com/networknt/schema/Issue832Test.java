package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.format.AbstractFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Issue832Test {
    private class NoMatchFormat extends AbstractFormat {
        public NoMatchFormat() {
            super("no_match", "always fail match");
        }

        @Override
        public boolean matches(String value) {
            return false;
        }
    }

    private JsonSchemaFactory buildV7PlusNoFormatSchemaFactory() {
        List<Format> formats;
        formats = new ArrayList<>();
        formats.add(new NoMatchFormat());

        JsonMetaSchema jsonMetaSchema = JsonMetaSchema.builder(
                JsonMetaSchema.getV7().getUri(),
                JsonMetaSchema.getV7())
                .addFormats(formats)
                .build();
        return new JsonSchemaFactory.Builder().defaultMetaSchemaURI(jsonMetaSchema.getUri()).addMetaSchema(jsonMetaSchema).build();
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    public void testV7WithNonMatchingCustomFormat() throws IOException {
        String schemaPath = "/schema/issue832-v7.json";
        String dataPath = "/data/issue832.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchemaFactory factory = buildV7PlusNoFormatSchemaFactory();
        JsonSchema schema = factory.getSchema(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        Set<ValidationMessage> errors = schema.validate(node);
        // Both the custom no_match format and the standard email format should fail.
        // This ensures that both the standard and custom formatters have been invoked.
        Assertions.assertEquals(2, errors.size());
    }
}
