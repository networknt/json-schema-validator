package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class Issue1087Test {
    protected JsonSchema getJsonSchemaFromStreamContent(InputStream schemaContent) throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        YAMLMapper mapper = new YAMLMapper();
        JsonNode node = mapper.readTree(schemaContent);
        return factory.getSchema(node, SchemaValidatorsConfig.builder().discriminatorKeywordEnabled(true).build());
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    void shouldHandleReferencesToYaml() throws Exception {
        String schemaPath = "/schema/issue1087.yml";
        String dataPath = "/data/issue1087.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContent(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        MatcherAssert.assertThat(schema.validate(node), Matchers.empty());
    }
}
