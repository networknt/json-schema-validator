package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpecificationVersionDetectorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({
            "draft4,       DRAFT_4",
            "draft6,       DRAFT_6",
            "draft7,       DRAFT_7",
            "draft2019-09, DRAFT_2019_09",
            "draft2020-12, DRAFT_2020_12"
    })
    void detectVersion(String resourceDirectory, Specification.Version expectedFlag) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourceDirectory + "/schemaTag.json");
        JsonNode node = mapper.readTree(in);
        Specification.Version flag = SpecificationVersionDetector.detect(node);
        assertEquals(expectedFlag, flag);
    }

    @ParameterizedTest
    @CsvSource({
            "data/schemaTag.json,        'http://json-schema.org/draft-03/schema#' is unrecognizable schema",
            "data/schemaTagMissing.json, '$schema' tag is not present"
    })
    void detectInvalidSchemaVersion(String schemaPath, String expectedError) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaPath);
        JsonNode node = mapper.readTree(in);
        SchemaException exception = assertThrows(SchemaException.class, () -> SpecificationVersionDetector.detect(node));
        assertEquals(expectedError, exception.getMessage());
    }

    @Test
    void detectOptionalSpecVersion() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "data/schemaTagMissing.json");
        JsonNode node = mapper.readTree(in);
        Optional<Specification.Version> flag = SpecificationVersionDetector.detectOptionalVersion(node, true);
        assertEquals(Optional.empty(), flag);
    }
}
