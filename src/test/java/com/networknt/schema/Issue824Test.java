package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.dialect.Dialects;

class Issue824Test {
    @Test
    void validate() throws JsonProcessingException {
        final Schema v201909SpecSchema = SchemaRegistry
                .builder(SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09))
                .schemaIdResolvers(schemaIdResolvers -> {
                    schemaIdResolvers.mapPrefix("https://json-schema.org", "resource:");
                }).build()
                .getSchema(SchemaLocation.of(Dialects.getDraft201909().getId()));
        final JsonNode invalidSchema = new ObjectMapper().readTree(
                "{"+
                "    \"$schema\": \"https://json-schema.org/draft/2019-09/schema\","+
                "    \"type\": \"cat\" "+
                "}");

        // Validate same JSON schema against v2019-09 spec schema twice
        final List<Error> validationErrors1 = v201909SpecSchema.validate(invalidSchema);
        final List<Error> validationErrors2 = v201909SpecSchema.validate(invalidSchema);

        // Validation errors should be the same
        assertEquals(validationErrors1, validationErrors2);

        // Results
        //
        // 1.0.73
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: should be valid to any of the schemas
        // array]
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: should be valid to any of the schemas
        // array]
        //
        // 1.0.74
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: string found, array expected]
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: string found, array expected]
        //
        // 1.0.78
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: should be valid to any of the schemas
        // array]
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: should be valid to any of the schemas
        // array]
        //
        // >= 1.0.82
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: string found, array expected]
        // [$.type: does not have a value in the enumeration [array, boolean, integer,
        // null, number, object, string], $.type: should be valid to any of the schemas
        // array]
        //
        // ?????
    }
}
