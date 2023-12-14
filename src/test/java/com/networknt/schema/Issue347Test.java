package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class Issue347Test {

    @Test
    public void failure() {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        try {
            JsonSchema schema = factory.getSchema(Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/issue347-v7.json"));
        } catch (Throwable e) {
            assertThat(e, instanceOf(JsonSchemaException.class));
            assertEquals("id: null is an invalid segment for URI test", e.getMessage());
        }
    }
}
