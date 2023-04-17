package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Issue673Test {
    private static JsonSchema schema;

    @BeforeAll
    static void init() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        String schemaPath = "/schema/issue673-openapi-schema.json";
        InputStream schemaInputStream = Issue673Test.class.getResourceAsStream(schemaPath);
        schema = factory.getSchema(schemaInputStream);
    }

    @Test
    void testInvalidTimeRepresentations() throws IOException {
        String validationPath = "/schema/issue673-validation.json";
        Set<ValidationMessage> errors = schema.validate(new ObjectMapper().readTree(Issue673Test.class.getResourceAsStream(validationPath)));

        System.out.println(errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n")));

        Assertions.assertTrue(errors.isEmpty());
    }
}
