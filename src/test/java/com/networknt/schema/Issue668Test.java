package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class Issue668Test {
    protected Schema getJsonSchemaFromStreamContent(InputStream schemaContent) throws Exception {
        SchemaRegistry factory = SchemaRegistry.getInstance(Specification.Version.DRAFT_7);
        YAMLMapper mapper = new YAMLMapper();
        JsonNode node = mapper.readTree(schemaContent);
        return factory.getSchema(node);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    void shouldHandleReferencesToYaml() throws Exception {
        String schemaPath = "/schema/issue668.yml";
        String dataPath = "/data/issue668.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        Schema schema = getJsonSchemaFromStreamContent(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        MatcherAssert.assertThat(schema.validate(node), Matchers.empty());
    }
}
