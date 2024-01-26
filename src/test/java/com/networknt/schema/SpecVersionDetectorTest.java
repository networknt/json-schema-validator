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

class SpecVersionDetectorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({
            "draft4,       V4",
            "draft6,       V6",
            "draft7,       V7",
            "draft2019-09, V201909",
            "draft2020-12, V202012"
    })
    void detectVersion(String resourceDirectory, SpecVersion.VersionFlag expectedFlag) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourceDirectory + "/schemaTag.json");
        JsonNode node = mapper.readTree(in);
        SpecVersion.VersionFlag flag = SpecVersionDetector.detect(node);
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
        JsonSchemaException exception = assertThrows(JsonSchemaException.class, () -> SpecVersionDetector.detect(node));
        assertEquals(expectedError, exception.getMessage());
    }

    @Test
    void detectOptionalSpecVersion() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "data/schemaTagMissing.json");
        JsonNode node = mapper.readTree(in);
        Optional<SpecVersion.VersionFlag> flag = SpecVersionDetector.detectOptionalVersion(node, true);
        assertEquals(Optional.empty(), flag);
    }
}
