package com.networknt.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class Issue518Test {
    private static final JsonMetaSchema igluMetaSchema =
            JsonMetaSchema
                    .builder("http://iglucentral.com/schemas/com.snowplowanalytics.self-desc/schema/jsonschema/1-0-0#", JsonMetaSchema.getV7())
                    .build();

    private static final JsonSchemaFactory FACTORY =
            JsonSchemaFactory
                    .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7))
                    .metaSchema(igluMetaSchema)
                    .build();

    @Test
    void testPreservingEmptyFragmentSuffix() {
        String schemaPath = "/schema/issue518-v7.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = FACTORY.getSchema(schemaInputStream);

        Assertions.assertNotNull(schema);
    }
}
