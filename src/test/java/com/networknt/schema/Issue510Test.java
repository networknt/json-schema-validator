package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class Issue510Test {
    @Test
    public void testIssue510() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).jsonMapper(objectMapper).build();
        System.out.println("schemaFactory = " + schemaFactory);
    }
}
