package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Issue928Test {
    private final ObjectMapper mapper = new ObjectMapper();

    private JsonSchemaFactory factoryFor(SpecVersion.VersionFlag version) {
        return JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(version))
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://example.org", "classpath:"))
                .build();
    }

    @Test
    void test_07() {
        test_spec(SpecVersion.VersionFlag.V7);
    }

    @Test
    void test_201909() {
        test_spec(SpecVersion.VersionFlag.V201909);
    }

    @Test
    void test_202012() {
        test_spec(SpecVersion.VersionFlag.V202012);
    }

    void test_spec(SpecVersion.VersionFlag specVersion) {
        JsonSchemaFactory schemaFactory = factoryFor(specVersion);

        String versionId = specVersion.getId();
        String versionStr = versionId.substring(versionId.indexOf("draft") + 6, versionId.indexOf("/schema"));

        String baseUrl = String.format("https://example.org/schema/issue928-v%s.json", versionStr);
        System.out.println("baseUrl: " + baseUrl);

        JsonSchema byPointer = schemaFactory.getSchema(
                SchemaLocation.of(baseUrl + "#/definitions/example"));

        Assertions.assertEquals(byPointer.validate(mapper.valueToTree("A")).size(), 0);
        Assertions.assertEquals(byPointer.validate(mapper.valueToTree("Z")).size(), 1);

        JsonSchema byAnchor = schemaFactory.getSchema(
                SchemaLocation.of(baseUrl + "#example"));

        Assertions.assertEquals(
                byPointer.getSchemaNode(),
                byAnchor.getSchemaNode());

        Assertions.assertEquals(byAnchor.validate(mapper.valueToTree("A")).size(), 0);
        Assertions.assertEquals(byAnchor.validate(mapper.valueToTree("Z")).size(), 1);
    }
}
