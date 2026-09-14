/*
 * Copyright (c) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema.oas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.OutputFormat;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.path.PathType;
import com.networknt.schema.Error;

/**
 * OpenApi30Test.
 */
class OpenApi30Test {
    /** The trailing components block shared by the nullable tests below. */
    private static final String MONEY_COMPONENT = "  \"components\": {\r\n"
            + "    \"schemas\": {\r\n"
            + "      \"Money\": {\r\n"
            + "        \"type\": \"object\",\r\n"
            + "        \"required\": [\"amount\"],\r\n"
            + "        \"properties\": {\r\n"
            + "          \"amount\": { \"type\": \"integer\" }\r\n"
            + "        }\r\n"
            + "      }\r\n"
            + "    }\r\n"
            + "  }\r\n"
            + "}\r\n";

    private static Schema openApi30Schema(String schemaData) {
        return SchemaRegistry.withDialect(Dialects.getOpenApi30()).getSchema(schemaData);
    }

    /**
     * Test with the explicitly configured OpenApi30 instance.
     */
    @Test
    void validateMetaSchema() {
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi30());
        Schema schema = factory.getSchema(SchemaLocation.of(
                "classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/requestBody/content/application~1json/schema"));
        String input = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"bark\": \"woof\"\r\n"
                + "}";
        List<Error> messages = schema.validate(input, InputFormat.JSON);
        assertEquals(0, messages.size());

        String invalid = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"meow\": \"meeeooow\"\r\n"
                + "}";
        messages = schema.validate(invalid, InputFormat.JSON);
        assertEquals(2, messages.size());
        List<Error> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getKeyword());
        assertEquals("required", list.get(1).getKeyword());
        assertEquals("bark", list.get(1).getProperty());
    }

    /**
     * Tests that schema location with number in fragment can resolve.
     */
    @Test
    void jsonPointerWithNumberInFragment() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().pathType(PathType.JSON_PATH).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi30(), builder -> builder.schemaRegistryConfig(config));
        Schema schema = factory.getSchema(SchemaLocation.of(
                "classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/responses/200/content/application~1json/schema")
                );
        assertNotNull(schema);
        //assertEquals("$.paths['/pet'].post.responses['200'].content['application/json'].schema",
        //        schema.getEvaluationPath().toString());
    }

    /**
     * Exclusive maximum true.
     */
    @Test
    void exclusiveMaximum() {
        String schemaData = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"minimum\": 0,\r\n"
                + "  \"maximum\": 100,\r\n"
                + "  \"exclusiveMaximum\": true\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);
        assertFalse(schema.validate("100", InputFormat.JSON, OutputFormat.BOOLEAN));
    }

    /**
     * Exclusive minimum true.
     */
    @Test
    void exclusiveMinimum() {
        String schemaData = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"minimum\": 0,\r\n"
                + "  \"maximum\": 100,\r\n"
                + "  \"exclusiveMinimum\": true\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);
        assertFalse(schema.validate("0", InputFormat.JSON, OutputFormat.BOOLEAN));
    }

    @ParameterizedTest
    @ValueSource(strings = { "allOf", "oneOf", "anyOf" })
    void nullableComposingKeywordRefAcceptsNull(String composingKeyword) {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\"value\"],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"" + composingKeyword + "\": [ { \"$ref\": \"#/components/schemas/Money\" } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableDirectRefSiblingIsIgnored() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\"value\"],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"$ref\": \"#/components/schemas/Money\",\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoRefProperty() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"order\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"required\": [\"customer\"],\r\n"
                + "      \"properties\": {\r\n"
                + "        \"customer\": { \"$ref\": \"#/components/schemas/Customer\" }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Customer\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"required\": [\"name\"],\r\n"
                + "        \"properties\": {\r\n"
                + "          \"name\": { \"type\": \"string\" }\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"order\": { \"customer\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableAllOfRefWithTwoLevelsOfComposingDepth() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\"value\"],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"allOf\": [ { \"$ref\": \"#/components/schemas/Money\" } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Money\": {\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"allOf\": [\r\n"
                + "              {\r\n"
                + "                \"type\": \"object\",\r\n"
                + "                \"required\": [\"amount\"],\r\n"
                + "                \"properties\": {\r\n"
                + "                  \"amount\": { \"type\": \"integer\" }\r\n"
                + "                }\r\n"
                + "              }\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableAllOfRefWithTwoLevelsOfReferenceDepth() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\"value\"],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"allOf\": [ { \"$ref\": \"#/components/schemas/Money\" } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Money\": {\r\n"
                + "        \"allOf\": [ { \"$ref\": \"#/components/schemas/Amount\" } ]\r\n"
                + "      },\r\n"
                + "      \"Amount\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"required\": [\"amount\"],\r\n"
                + "        \"properties\": {\r\n"
                + "          \"amount\": { \"type\": \"integer\" }\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableNotDoesNotPropagateThroughNot() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\"value\"],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"not\": { \"$ref\": \"#/components/schemas/Impossible\" },\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Impossible\": { \"type\": \"string\" }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableContainerNotLeakedIntoRefItem() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"list\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"items\": { \"$ref\": \"#/components/schemas/Money\" }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"list\": [ null ] }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoRefAdditionalProperties() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"meta\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"additionalProperties\": { \"$ref\": \"#/components/schemas/Money\" }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"meta\": { \"extra\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableAdditionalPropertiesNotLeakedWhenPropertyNamedAllOf() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"allOf\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"additionalProperties\": { \"$ref\": \"#/components/schemas/Money\" }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"allOf\": { \"extra\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableItemsNotLeakedWhenPropertyNamedAllOf() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"allOf\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"items\": { \"$ref\": \"#/components/schemas/Money\" }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"allOf\": [ null ] }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableNotLeakedThroughAncestorWhenPropertyNamedAllOfHoldsRefDirectly() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"outer\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"properties\": {\r\n"
                + "        \"allOf\": { \"$ref\": \"#/components/schemas/Money\" }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"outer\": { \"allOf\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableAllOfRefNotLostWhenBranchDeclaresId() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"allOf\": [ { \"id\": \"http://example.com/inner\", \"type\": \"string\" } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableMultiBranchOneOfRef() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"oneOf\": [ { \"$ref\": \"#/components/schemas/A\" }, { \"$ref\": \"#/components/schemas/B\" } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"A\": { \"type\": \"object\", \"properties\": { \"a\": { \"type\": \"string\" } } },\r\n"
                + "      \"B\": { \"type\": \"object\", \"properties\": { \"b\": { \"type\": \"string\" } } }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableContainerNotLeakedIntoInlineProperty() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"order\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"properties\": { \"name\": { \"type\": \"string\" } }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"order\": { \"name\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoInlineItem() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"list\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"items\": { \"type\": \"string\" }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"list\": [ null ] }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoInlineAdditionalProperties() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"meta\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"additionalProperties\": { \"type\": \"string\" }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"meta\": { \"extra\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerStillAllowsNullForItself() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"order\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"properties\": { \"name\": { \"type\": \"string\" } }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"order\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableAllOfRefToEnumComponent() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"allOf\": [ { \"$ref\": \"#/components/schemas/E\" } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"E\": { \"type\": \"string\", \"enum\": [\"a\", \"b\"] }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void typeLooseNullableEnumRejectsTheStringNull() {
        // The accepted set no longer holds a NullNode, so the typeLoose text
        // comparison no longer sees "null" as one of the permitted values.
        String schemaData = "{\r\n"
                + "  \"properties\": {\r\n"
                + "    \"v\": { \"enum\": [\"a\"], \"nullable\": true }\r\n"
                + "  }\r\n"
                + "}\r\n";
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().typeLoose(true).build();
        Schema schema = SchemaRegistry
                .withDialect(Dialects.getOpenApi30(), builder -> builder.schemaRegistryConfig(config))
                .getSchema(schemaData);

        List<Error> messages = schema.validate("{ \"v\": \"null\" }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("enum", messages.get(0).getKeyword());

        assertEquals(0, schema.validate("{ \"v\": null }", InputFormat.JSON).size());
        assertEquals(0, schema.validate("{ \"v\": \"a\" }", InputFormat.JSON).size());
    }

    @Test
    void nullableEnumStillRejectsNonNullValueOutsideEnum() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"allOf\": [ { \"$ref\": \"#/components/schemas/E\" } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"E\": { \"type\": \"string\", \"enum\": [\"a\", \"b\"] }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": \"c\" }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("enum", messages.get(0).getKeyword());
    }

    @Test
    void nullableMultiBranchOneOfRefWrappedInAllOf() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"value\": {\r\n"
                + "      \"allOf\": [ {\r\n"
                + "        \"oneOf\": [ { \"$ref\": \"#/components/schemas/A\" }, { \"$ref\": \"#/components/schemas/B\" } ]\r\n"
                + "      } ],\r\n"
                + "      \"nullable\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"A\": { \"type\": \"object\", \"properties\": { \"a\": { \"type\": \"string\" } } },\r\n"
                + "      \"B\": { \"type\": \"object\", \"properties\": { \"b\": { \"type\": \"string\" } } }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }
}
