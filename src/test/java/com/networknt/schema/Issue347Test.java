package com.networknt.schema;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Issue347Test {

    @Test
    public void failure() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        try {
            JsonSchema schema = factory.getSchema(Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/issue347-v7.json"));
        } catch (Throwable e) {
            assertThat(e, instanceOf(JsonSchemaException.class));
            assertEquals("#/$id: null is an invalid segment for URI test", e.getMessage());
        }
    }
}
