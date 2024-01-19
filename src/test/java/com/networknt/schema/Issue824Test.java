package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.uri.URITranslator;

public class Issue824Test {
    @Test
    void validate() throws JsonProcessingException {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.addUriTranslator(URITranslator.prefix("https://json-schema.org", "resource:"));
        final JsonSchema v201909SpecSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(URI.create(JsonMetaSchema.getV201909().getUri()), config);
        v201909SpecSchema.preloadJsonSchema();
        final JsonNode invalidSchema = new ObjectMapper().readTree(
                "{"+
                "    \"$schema\": \"https://json-schema.org/draft/2019-09/schema\","+
                "    \"type\": \"cat\" "+
                "}");

        // Validate same JSON schema against v2019-09 spec schema twice
        final Set<ValidationMessage> validationErrors1 = v201909SpecSchema.validate(invalidSchema);
        final Set<ValidationMessage> validationErrors2 = v201909SpecSchema.validate(invalidSchema);

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
