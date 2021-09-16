package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpecVersionDetectorTest {

    private static final String SCHEMA_TAG_JSON = "schemaTag.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void detectV4() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("draft4/" + SCHEMA_TAG_JSON);
        JsonNode node = mapper.readTree(in);
        SpecVersion.VersionFlag flag = SpecVersionDetector.detect(node);
        assertEquals(SpecVersion.VersionFlag.V4, flag);
    }

    @Test
    public void detectV6() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("draft6/" + SCHEMA_TAG_JSON);
        JsonNode node = mapper.readTree(in);
        SpecVersion.VersionFlag flag = SpecVersionDetector.detect(node);
        assertEquals(SpecVersion.VersionFlag.V6, flag);
    }

    @Test
    public void detectV7() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("draft7/" + SCHEMA_TAG_JSON);
        JsonNode node = mapper.readTree(in);
        SpecVersion.VersionFlag flag = SpecVersionDetector.detect(node);
        assertEquals(SpecVersion.VersionFlag.V7, flag);
    }

    @Test
    public void detectV201909() throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("draft2019-09/" + SCHEMA_TAG_JSON);
        JsonNode node = mapper.readTree(in);
        SpecVersion.VersionFlag flag = SpecVersionDetector.detect(node);
        assertEquals(SpecVersion.VersionFlag.V201909, flag);
    }

    @Test
    public void detectUnsupportedSchemaVersion() throws IOException {
        assertThrows(JsonSchemaException.class, () -> {
            InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("data/" + SCHEMA_TAG_JSON);
            JsonNode node = mapper.readTree(in);
            SpecVersion.VersionFlag flag = SpecVersionDetector.detect(node);
        });
    }

    @Test
    public void detectMissingSchemaVersion() throws IOException {
        assertThrows(JsonSchemaException.class, () -> {
            InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("data/" + "schemaTagMissing.json");
            JsonNode node = mapper.readTree(in);
            SpecVersion.VersionFlag flag = SpecVersionDetector.detect(node);
        });
    }

}