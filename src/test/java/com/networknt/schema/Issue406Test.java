/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates.
 */
package com.networknt.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class Issue406Test {
    protected static final String INVALID_$REF_SCHEMA = "{\"$ref\":\"urn:unresolved\"}";
    protected static final String CIRCULAR_$REF_SCHEMA = "{\"$ref\":\"#/nestedSchema\","
            + "\"nestedSchema\":{\"$ref\":\"#/nestedSchema\"}}";

    @Test
    void testPreloadingNotHappening() {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(INVALID_$REF_SCHEMA);
        // not breaking - pass
        Assertions.assertNotNull(schema);
    }

    @Test
    void testPreloadingHappening() {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(INVALID_$REF_SCHEMA);
        Assertions.assertThrows(JsonSchemaException.class,
                            new Executable() {
                                @Override
                                public void execute() {
                                    schema.initializeValidators();
                                }
                            },
                            "#/$ref: Reference urn:unresolved cannot be resolved");
    }

    @Test
    void testPreloadingHappeningForCircularDependency() {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(CIRCULAR_$REF_SCHEMA);
        schema.initializeValidators();
    }
}
