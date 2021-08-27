/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates.
 */
package com.networknt.schema;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class Issue406Test {
    protected static final String INVALID_$REF_SCHEMA = "{\"$ref\":\"urn:unresolved\"}";
    protected static final String CIRCULAR_$REF_SCHEMA = "{\"$ref\":\"#/nestedSchema\","
            + "\"nestedSchema\":{\"$ref\":\"#/nestedSchema\"}}";

    @Test
    public void testPreloadingNotHappening() {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(INVALID_$REF_SCHEMA);
        // not breaking - pass
        Assert.assertNotNull(schema);
    }

    @Test
    public void testPreloadingHappening() {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(INVALID_$REF_SCHEMA);
        Assert.assertThrows("#/$ref: Reference urn:unresolved cannot be resolved",
                            JsonSchemaException.class,
                            new ThrowingRunnable() {
                                @Override
                                public void run() {
                                    schema.initializeValidators();
                                }
                            });
    }

    @Test
    public void testPreloadingHappeningForCircularDependency() {
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final JsonSchema schema = factory.getSchema(CIRCULAR_$REF_SCHEMA);
        schema.initializeValidators();
    }
}
