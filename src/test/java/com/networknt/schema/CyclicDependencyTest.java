package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CyclicDependencyTest {

    @Test
    void whenDependencyBetweenSchemaThenValidationSuccessful() throws Exception {

        JsonSchemaFactory schemaFactory = JsonSchemaFactory
            .builder(JsonSchemaFactory.getInstance(Specification.Version.DRAFT_4))
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

        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        Schema schema = schemaFactory.getSchema(SchemaLocation.of("resource:/draft4/issue258/Master.json"), config);
        assertEquals(0, schema.validate(new ObjectMapper().readTree(jsonObject)).size());
    }


}
