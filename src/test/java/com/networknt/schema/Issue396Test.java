package com.networknt.schema;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class Issue396Test {
    protected JsonSchema getJsonSchemaFromStreamContentV7(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    @Test
    void testComplexPropertyNamesV7() throws Exception {
        String schemaPath = "/schema/issue396-v7.json";
        String dataPath = "/data/issue396.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);

        final Set<String> expected = new HashSet<>();
        node.fields().forEachRemaining(entry -> {
            if (!entry.getValue().asBoolean())
                expected.add(entry.getKey());
        });

        Set<ValidationMessage> errors = schema.validate(node);
        final Set<String> actual = errors.stream().map(ValidationMessage::getProperty).map(Object::toString).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
