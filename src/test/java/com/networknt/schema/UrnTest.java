package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class UrnTest {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Validate that a JSON URI Mapping file containing the URI Mapping schema is
     * schema valid.
     *
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    void testURNToURI() throws Exception {
        InputStream urlTestData = UrnTest.class.getResourceAsStream("/draft7/urn/test.json");
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7,
                builder -> builder.schemaIdResolvers(schemaIdResolvers -> schemaIdResolvers.add(value -> {
                    return AbsoluteIri.of(String.format("%s.schema.json", value.toString()));
                })));
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of("classpath:/draft7/urn/urn"));
        assertEquals(0, schema.validate(mapper.readTree(urlTestData)).size());
    }
}
