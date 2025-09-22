package com.networknt.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;

import java.io.InputStream;

class Issue518Test {
    private static final Dialect igluMetaSchema =
            Dialect
                    .builder("http://iglucentral.com/schemas/com.snowplowanalytics.self-desc/schema/jsonschema/1-0-0#", Dialects.getDraft7())
                    .build();

    private static final SchemaRegistry REGISTRY =
            SchemaRegistry.withDialect(igluMetaSchema);

    @Test
    void testPreservingEmptyFragmentSuffix() {
        String schemaPath = "/schema/issue518-v7.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        Schema schema = REGISTRY.getSchema(schemaInputStream);

        Assertions.assertNotNull(schema);
    }
}
