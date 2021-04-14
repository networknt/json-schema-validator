package com.networknt.schema;

import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;

public class Issue314Test {
    private static final JsonSchemaFactory FACTORY =
            JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7))
                    .addMetaSchema(
                            JsonMetaSchema.builder(
                                    "http://iglucentral.com/schemas/com.snowplowanalytics.self-desc/schema/jsonschema/1-0-0",
                                    JsonMetaSchema.getV7())
                                    .build())
                    .forceHttps(false)
                    .build();

    @Test
    public void testNormalizeHttpOnly() {
        String schemaPath = "/schema/issue314-v7.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = FACTORY.getSchema(schemaInputStream);

        Assert.assertNotNull(schema);
    }
}
