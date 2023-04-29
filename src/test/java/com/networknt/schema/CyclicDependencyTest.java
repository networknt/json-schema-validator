package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CyclicDependencyTest {

    @Test
    public void whenDependencyBetweenSchemaThenValidationSuccessful() throws Exception {

        JsonSchemaFactory schemaFactory = JsonSchemaFactory
            .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4))
            .build();
        String jsonObject = "{\n" +
                "  \"element\": {\n" +
                "    \"id\": \"top\",\n" +
                "    \"extension\": [\n" +
                "      {\n" +
                "        \"url\": \"http://inner.test\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"extension\": [\n" +
                "    {\n" +
                "      \"url\": \"http://top.test\",\n" +
                "      \"valueElement\": {\n" +
                "        \"id\": \"inner\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        URI jsonSchemaLocation = URI.create("resource:/draft4/issue258/Master.json");
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        JsonSchema schema = schemaFactory.getSchema(jsonSchemaLocation, config);
        assertEquals(0, schema.validate(new ObjectMapper().readTree(jsonObject)).size());
    }


}
