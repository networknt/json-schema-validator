package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.path.PathType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
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

    static Stream<Arguments> errors() {
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
        StringBuilder builder = new StringBuilder();
        builder.append(currentPath);
        pathType.append(builder, token);
        assertEquals(expected, builder.toString());
    }

    @ParameterizedTest
    @MethodSource("appendIndexes")
    void testAppendIndex(PathType pathType, String currentPath, Integer index, String expected) {
        StringBuilder builder = new StringBuilder();
        builder.append(currentPath);
        pathType.append(builder, index);
        assertEquals(expected, builder.toString());
    }

    @ParameterizedTest
    @MethodSource("errors")
    void testError(PathType pathType, String schemaPath, String content, String[] expectedMessagePaths) throws JsonProcessingException {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().pathType(pathType).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09, builder -> builder.schemaRegistryConfig(config));
        Schema schema = factory.getSchema(Issue687Test.class.getResourceAsStream(schemaPath));
        List<Error> messages = schema.validate(new ObjectMapper().readTree(content));
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
        SchemaRegistryConfig schemaValidatorsConfig = SchemaRegistryConfig.builder().pathType(pathType).build();
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09, builder -> builder.schemaRegistryConfig(schemaValidatorsConfig))
                .getSchema(mapper.readTree("{\n" +
                        "    \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\n" +
                        "    \"type\": \"object\",\n" +
                        "    \"properties\": {\n" +
                        "        \""+propertyName+"\": {\n" +
                        "            \"type\": \"boolean\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"));
        List<Error> errors = schema.validate(mapper.readTree("{\""+propertyName+"\": 1}"));
        assertEquals(1, errors.size());
        assertEquals(expectedPath, errors.iterator().next().getInstanceLocation().toString());
    }

}
