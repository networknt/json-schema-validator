package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Issue928Test {
    private final ObjectMapper mapper = new ObjectMapper();

    private SchemaRegistry factoryFor(Specification.Version version) {
        return SchemaRegistry
                .builder(SchemaRegistry.getInstance(version))
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://example.org", "classpath:"))
                .build();
    }

    @Test
    void test_07() {
        test_spec(Specification.Version.DRAFT_7);
    }

    @Test
    void test_201909() {
        test_spec(Specification.Version.DRAFT_2019_09);
    }

    @Test
    void test_202012() {
        test_spec(Specification.Version.DRAFT_2020_12);
    }

    void test_spec(Specification.Version specVersion) {
        SchemaRegistry schemaFactory = factoryFor(specVersion);

        String versionId = specVersion.getDialectId();
        String versionStr = versionId.substring(versionId.indexOf("draft") + 6, versionId.indexOf("/schema"));

        String baseUrl = String.format("https://example.org/schema/issue928-v%s.json", versionStr);
        System.out.println("baseUrl: " + baseUrl);

        Schema byPointer = schemaFactory.getSchema(
                SchemaLocation.of(baseUrl + "#/definitions/example"));

        Assertions.assertEquals(byPointer.validate(mapper.valueToTree("A")).size(), 0);
        Assertions.assertEquals(byPointer.validate(mapper.valueToTree("Z")).size(), 1);

        Schema byAnchor = schemaFactory.getSchema(
                SchemaLocation.of(baseUrl + "#example"));

        Assertions.assertEquals(
                byPointer.getSchemaNode(),
                byAnchor.getSchemaNode());

        Assertions.assertEquals(byAnchor.validate(mapper.valueToTree("A")).size(), 0);
        Assertions.assertEquals(byAnchor.validate(mapper.valueToTree("Z")).size(), 1);
    }
}
