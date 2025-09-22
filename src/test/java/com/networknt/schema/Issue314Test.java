package com.networknt.schema;

import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;

class Issue314Test {
    private static final SchemaRegistry REGISTRY =
            SchemaRegistry.withDialect(
                            Dialect.builder(
                                    "http://iglucentral.com/schemas/com.snowplowanalytics.self-desc/schema/jsonschema/1-0-0#",
                                    Dialects.getDraft7())
                                    .build());

    @Test
    void testNormalizeHttpOnly() {
        String schemaPath = "/schema/issue314-v7.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        Schema schema = REGISTRY.getSchema(schemaInputStream);

        Assertions.assertNotNull(schema);
    }
}
