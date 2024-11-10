package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Issue687Test {

    @Test
    void testRoot() {
        assertEquals("$", PathType.LEGACY.getRoot());
        assertEquals("$", PathType.JSON_PATH.getRoot());
        assertEquals("", PathType.JSON_POINTER.getRoot());
    }

    @Test
    void testDefault() {
        assertEquals(PathType.LEGACY, PathType.DEFAULT);
    }

    static Stream<Arguments> appendTokens() {
        return Stream.of(
                Arguments.of(PathType.LEGACY, "$.foo", "bar", "$.foo.bar"),
                Arguments.of(PathType.LEGACY, "$.foo", "b.ar", "$.foo.b.ar"),
                Arguments.of(PathType.LEGACY, "$.foo", "b~ar", "$.foo.b~ar"),
                Arguments.of(PathType.LEGACY, "$.foo", "b/ar", "$.foo.b/ar"),
                Arguments.of(PathType.JSON_PATH, "$.foo", "bar", "$.foo.bar"),
                Arguments.of(PathType.JSON_PATH, "$.foo", "b.ar", "$.foo['b.ar']"),
                Arguments.of(PathType.JSON_PATH, "$.foo", "b~ar", "$.foo['b~ar']"),
                Arguments.of(PathType.JSON_PATH, "$.foo", "b/ar", "$.foo['b/ar']"),
                Arguments.of(PathType.JSON_PATH, "$", "'", "$['\\'']"),
                Arguments.of(PathType.JSON_PATH, "$", "b'ar", "$['b\\'ar']"),
                Arguments.of(PathType.JSON_POINTER, "/foo", "bar", "/foo/bar"),
                Arguments.of(PathType.JSON_POINTER, "/foo", "b.ar", "/foo/b.ar"),
                Arguments.of(PathType.JSON_POINTER, "/foo", "b~ar", "/foo/b~0ar"),
                Arguments.of(PathType.JSON_POINTER, "/foo", "b/ar", "/foo/b~1ar")
        );
    }

    static Stream<Arguments> appendIndexes() {
        return Stream.of(
                Arguments.of(PathType.LEGACY, "$.foo", 0, "$.foo[0]"),
                Arguments.of(PathType.JSON_PATH, "$.foo", 0, "$.foo[0]"),
                Arguments.of(PathType.JSON_POINTER, "/foo", 0, "/foo/0")
        );
    }

    static Stream<Arguments> validationMessages() {
        String schemaPath = "/schema/issue687.json";
        String content = "{ \"foo\": \"a\", \"b.ar\": 1, \"children\": [ { \"childFoo\": \"a\", \"c/hildBar\": 1 } ] }";
        return Stream.of(
                Arguments.of(PathType.LEGACY, schemaPath, content, new String[] { "$.b.ar", "$.children[0].c/hildBar" }),
                Arguments.of(PathType.JSON_PATH, schemaPath, content, new String[] { "$['b.ar']", "$.children[0]['c/hildBar']" }),
                Arguments.of(PathType.JSON_PATH, schemaPath, content, new String[] { "$['b.ar']", "$.children[0]['c/hildBar']" }),
                Arguments.of(PathType.JSON_POINTER, schemaPath, content, new String[] { "/b.ar", "/children/0/c~1hildBar" })
        );
    }

    @ParameterizedTest
    @MethodSource("appendTokens")
    void testAppendToken(PathType pathType, String currentPath, String token, String expected) {
        assertEquals(expected, pathType.append(currentPath, token));
    }

    @ParameterizedTest
    @MethodSource("appendIndexes")
    void testAppendIndex(PathType pathType, String currentPath, Integer index, String expected) {
        assertEquals(expected, pathType.append(currentPath, index));
    }

    @ParameterizedTest
    @MethodSource("validationMessages")
    void testValidationMessage(PathType pathType, String schemaPath, String content, String[] expectedMessagePaths) throws JsonProcessingException {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().pathType(pathType).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchema schema = factory.getSchema(Issue687Test.class.getResourceAsStream(schemaPath), config);
        Set<ValidationMessage> messages = schema.validate(new ObjectMapper().readTree(content));
        assertEquals(expectedMessagePaths.length, messages.size());
        for (String expectedPath: expectedMessagePaths) {
            assertTrue(messages.stream().anyMatch(msg -> expectedPath.equals(msg.getInstanceLocation().toString())));
        }
    }

    static Stream<Arguments> specialCharacterTests() {
        return Stream.of(
                Arguments.of(PathType.JSON_PATH, "'", "$['\\'']"),
                Arguments.of(PathType.JSON_PATH, "\\\"", "$['\"']"),
                Arguments.of(PathType.JSON_PATH, "\\n", "$['\\n']"),
                Arguments.of(PathType.JSON_PATH, "\\r", "$['\\r']"),
                Arguments.of(PathType.JSON_PATH, "\\t", "$['\\t']"),
                Arguments.of(PathType.JSON_PATH, "\\f", "$['\\f']"),
                Arguments.of(PathType.JSON_PATH, "\\b", "$['\\b']"),
                Arguments.of(PathType.JSON_POINTER, "~", "/~0"),
                Arguments.of(PathType.JSON_POINTER, "/", "/~1"),
                Arguments.of(PathType.JSON_POINTER, "\\n", "/\\n"),
                Arguments.of(PathType.JSON_POINTER, "\\r", "/\\r"),
                Arguments.of(PathType.JSON_POINTER, "\\t", "/\\t"),
                Arguments.of(PathType.JSON_POINTER, "\\f", "/\\f"),
                Arguments.of(PathType.JSON_POINTER, "\\b", "/\\b")
        );
    }

    @ParameterizedTest
    @MethodSource("specialCharacterTests")
    void testSpecialCharacters(PathType pathType, String propertyName, String expectedPath) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().pathType(pathType).build();
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(mapper.readTree("{\n" +
                        "    \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\n" +
                        "    \"type\": \"object\",\n" +
                        "    \"properties\": {\n" +
                        "        \""+propertyName+"\": {\n" +
                        "            \"type\": \"boolean\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"), schemaValidatorsConfig);
        Set<ValidationMessage> validationMessages = schema.validate(mapper.readTree("{\""+propertyName+"\": 1}"));
        assertEquals(1, validationMessages.size());
        assertEquals(expectedPath, validationMessages.iterator().next().getInstanceLocation().toString());
    }

}
