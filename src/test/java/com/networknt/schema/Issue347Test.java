package com.networknt.schema;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Issue347Test {

    @Test
    void failure() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/issue347-v7.json")));
        try {
            factory.getSchema(Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/issue347-v7.json"));
        } catch (Throwable e) {
            assertThat(e, instanceOf(JsonSchemaException.class));
            assertEquals("/$id: 'test' is not a valid $id", e.getMessage());
        }
    }
}
