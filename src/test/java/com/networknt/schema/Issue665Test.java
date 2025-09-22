package com.networknt.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

class Issue665Test extends BaseJsonSchemaValidatorTest {

    @Test
    void testUrnUriAsLocalRef() throws IOException {
        Schema schema = getJsonSchemaFromClasspath("draft7/urn/issue665.json", Specification.Version.DRAFT_7);
        Assertions.assertNotNull(schema);
        Assertions.assertDoesNotThrow(schema::initializeValidators);
        List<Error> messages = schema.validate(getJsonNodeFromStringContent(
                "{\"myData\": {\"value\": \"hello\"}}"));
        Assertions.assertTrue(messages.isEmpty());
    }

    @Test
    void testUrnUriAsLocalRef_ExternalURN() {
        SchemaRegistry factory = SchemaRegistry
                .builder(SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7))
                .schemaMappers(schemaMappers -> {
                    schemaMappers.mappings(Collections.singletonMap("urn:data",
                            "classpath:draft7/urn/issue665_external_urn_subschema.json"));
                })
                .build();

        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("draft7/urn/issue665_external_urn_ref.json")) {
            Schema schema = factory.getSchema(is);
            Assertions.assertNotNull(schema);
            Assertions.assertDoesNotThrow(schema::initializeValidators);
            List<Error> messages = schema.validate(getJsonNodeFromStringContent(
                    "{\"myData\": {\"value\": \"hello\"}}"));
            Assertions.assertTrue(messages.isEmpty());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
