/*
 * Copyright (c) 2016 Network New Technologies Inc.
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
package com.networknt.schema.serialization;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/** Upgrade contract for the Jackson tree deserializers wrapped by the public factories. */
@Tag("jackson-upgrade")
class Issue1363Test {
    private static final String LOSSY = "9007199254740993.0";
    private static final String OBJECT = "{\"value\":123.25,\"negativeZero\":-0.0,\"nested\":[null,true,\"text\",7]}";
    private static final String ARRAY = "[123.25,-0.0,{\"value\":7},null]";

    enum Format {
        JSON, YAML;

        ObjectMapper selective() {
            return this == JSON ? JsonMapperFactory.getInstance() : YamlMapperFactory.getInstance();
        }

        ObjectMapper stock() {
            return this == JSON ? JsonMapper.builder().build() : YAMLMapper.builder().build();
        }
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void treeAndDeclaredRootTypesRetainNativeShape(Format format) throws Exception {
        ObjectMapper selective = format.selective();
        ObjectMapper stock = format.stock();
        for (String input : new String[] { OBJECT, ARRAY, "null", "true", "\"text\"", "123.25", "-0.0" }) {
            sameTree(stock.readTree(input), selective.readTree(input));
            sameTree(stock.readValue(input, JsonNode.class), selective.readValue(input, JsonNode.class));
            sameTree(stock.readerFor(JsonNode.class).readValue(input), selective.readerFor(JsonNode.class).readValue(input));
        }
        assertInstanceOf(ObjectNode.class, selective.readValue(OBJECT, ObjectNode.class));
        assertInstanceOf(ArrayNode.class, selective.readValue(ARRAY, ArrayNode.class));
        sameTree(stock.readValue(OBJECT, ObjectNode.class), selective.readValue(OBJECT, ObjectNode.class));
        sameTree(stock.readValue(ARRAY, ArrayNode.class), selective.readValue(ARRAY, ArrayNode.class));
        for (Class<? extends JsonNode> type : rootTypes()) {
            sameTree(stock.readValue("null", type), selective.readValue("null", type));
        }
        sameFailure(stock.readerFor(ObjectNode.class), selective.readerFor(ObjectNode.class), ARRAY);
        sameFailure(stock.readerFor(ArrayNode.class), selective.readerFor(ArrayNode.class), OBJECT);
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void lossyDecimalsArePreservedForEveryDeclaredTreeRoot(Format format) throws Exception {
        ObjectMapper mapper = format.selective();
        ObjectMapper stock = format.stock();
        for (Class<? extends JsonNode> type : rootTypes()) {
            String input = type == ArrayNode.class ? "[" + LOSSY + "]" : "{\"value\":" + LOSSY + "}";
            JsonNode actual = mapper.readValue(input, type);
            JsonNode value = type == ArrayNode.class ? actual.get(0) : actual.get("value");
            assertTrue(value.isBigDecimal());
            assertEquals(0, new BigDecimal(LOSSY).compareTo(value.decimalValue()));
            // The stock mapper is the shape oracle; exact mode supplies the intentional decimal difference.
            sameTree(stock.readerFor(type).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(input), actual);
        }
        assertTrue(mapper.readValue(LOSSY, JsonNode.class).isBigDecimal());
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void emptyInputNullAndAbsentPropertiesRetainNativeHandling(Format format) throws Exception {
        ObjectMapper selective = format.selective();
        ObjectMapper stock = format.stock();
        for (String input : new String[] { "", "   " }) {
            sameTree(stock.readTree(input), selective.readTree(input));
            sameTree(stock.reader().readTree(input), selective.reader().readTree(input));
            for (Class<? extends JsonNode> type : rootTypes()) {
                sameFailure(stock.readerFor(type), selective.readerFor(type), input);
            }
            try (JsonParser expected = stock.getFactory().createParser(input); JsonParser actual = selective.getFactory().createParser(input)) {
                sameTree(stock.readTree(expected), selective.readTree(actual));
            }
        }
        TreeFields absent = selective.readValue("{}", TreeFields.class);
        assertNull(absent.node);
        assertNull(absent.object);
        assertNull(absent.array);
        TreeFields expected = stock.readValue("{\"node\":null,\"object\":null,\"array\":null}", TreeFields.class);
        TreeFields actual = selective.readValue("{\"node\":null,\"object\":null,\"array\":null}", TreeFields.class);
        sameTree(expected.node, actual.node);
        sameTree(expected.object, actual.object);
        sameTree(expected.array, actual.array);
        assertTrue(actual.node.isNull());
        assertTrue(selective.createObjectNode().path("absent").isMissingNode());
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void updatingObjectsAndArraysKeepsIdentityAndNativeMergeRules(Format format) throws Exception {
        ObjectMapper selective = format.selective();
        ObjectMapper stock = format.stock();
        String initial = "{\"keep\":true,\"nested\":{\"old\":1},\"items\":[1]}";
        String patch = "{\"nested\":{\"new\":2},\"items\":[2],\"added\":null}";
        ObjectNode expected = (ObjectNode) stock.readTree(initial);
        ObjectNode actual = (ObjectNode) selective.readTree(initial);
        JsonNode nested = actual.get("nested");
        JsonNode items = actual.get("items");
        assertSame(expected, stock.readerForUpdating(expected).readValue(patch));
        assertSame(actual, selective.readerForUpdating(actual).readValue(patch));
        assertSame(nested, actual.get("nested"));
        assertSame(items, actual.get("items"));
        assertEquals("{\"keep\":true,\"nested\":{\"old\":1,\"new\":2},\"items\":[1,2],\"added\":null}", actual.toString());
        sameTree(expected, actual);
        ArrayNode expectedArray = (ArrayNode) stock.readTree("[1]");
        ArrayNode actualArray = (ArrayNode) selective.readTree("[1]");
        assertSame(expectedArray, stock.readerForUpdating(expectedArray).readValue("[2,null]"));
        assertSame(actualArray, selective.readerForUpdating(actualArray).readValue("[2,null]"));
        assertEquals("[1,2,null]", actualArray.toString());
        sameTree(expectedArray, actualArray);
        for (String input : new String[] { "null", "{}" }) {
            ArrayNode left = stock.createArrayNode();
            ArrayNode right = selective.createArrayNode();
            if (input.equals("null")) {
                sameTree(stock.readerForUpdating(left).readValue(input), selective.readerForUpdating(right).readValue(input));
            } else {
                sameFailure(stock.readerForUpdating(left), selective.readerForUpdating(right), input);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void updatingAndAnnotatedMergeStillApplySelectivePrecision(Format format) throws Exception {
        ObjectMapper mapper = format.selective();
        ObjectNode object = mapper.createObjectNode().put("keep", true);
        assertSame(object, mapper.readerForUpdating(object).readValue("{\"value\":" + LOSSY + "}"));
        assertTrue(object.get("keep").booleanValue());
        assertTrue(object.get("value").isBigDecimal());
        ArrayNode array = mapper.createArrayNode().add(1);
        assertSame(array, mapper.readerForUpdating(array).readValue("[" + LOSSY + "]"));
        assertTrue(array.get(1).isBigDecimal());
        String initial = "{\"node\":{\"keep\":true},\"object\":{\"keep\":true},\"array\":[1]}";
        String patch = "{\"node\":{\"value\":" + LOSSY + "},\"object\":{\"value\":" + LOSSY + "},\"array\":[" + LOSSY + "]}";
        MergeFields actual = mapper.readValue(initial, MergeFields.class);
        JsonNode nodeBefore = actual.node;
        ObjectNode objectBefore = actual.object;
        ArrayNode arrayBefore = actual.array;
        assertSame(actual, mapper.readerForUpdating(actual).readValue(patch));
        assertNotSame(nodeBefore, actual.node); // Native generic JsonNode updates replace the value.
        assertSame(objectBefore, actual.object);
        assertSame(arrayBefore, actual.array);
        assertNull(actual.node.get("keep"));
        assertTrue(actual.object.get("keep").booleanValue());
        assertTrue(actual.node.get("value").isBigDecimal());
        assertTrue(actual.object.get("value").isBigDecimal());
        assertTrue(actual.array.get(1).isBigDecimal());
        MergeFields expected = format.stock().reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .forType(MergeFields.class).readValue(initial);
        format.stock().readerForUpdating(expected).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(patch);
        sameTree(expected.node, actual.node);
        sameTree(expected.object, actual.object);
        sameTree(expected.array, actual.array);
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void contextualContainerMergeOverridesReplaceNestedValues(Format format) throws Exception {
        ObjectMapper selective = noContainerMerging(format.selective());
        ObjectMapper stock = noContainerMerging(format.stock());
        String initial = "{\"nested\":{\"old\":1},\"items\":[1]}";
        String patch = "{\"nested\":{\"value\":" + LOSSY + "},\"items\":[" + LOSSY + "]}";
        ObjectNode actual = (ObjectNode) selective.readTree(initial);
        JsonNode oldObject = actual.get("nested");
        JsonNode oldArray = actual.get("items");
        selective.readerForUpdating(actual).readValue(patch);
        assertNotSame(oldObject, actual.get("nested"));
        assertNotSame(oldArray, actual.get("items"));
        assertNull(actual.get("nested").get("old"));
        assertEquals(1, actual.get("items").size());
        assertTrue(actual.get("nested").get("value").isBigDecimal());
        assertTrue(actual.get("items").get(0).isBigDecimal());
        ObjectNode expected = (ObjectNode) stock.readTree(initial);
        stock.readerForUpdating(expected).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(patch);
        sameTree(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void perReaderFeatureOverridesAreHonoredWithoutLeaking(Format format) throws Exception {
        ObjectMapper mapper = format.selective();
        ObjectMapper stock = format.stock();
        for (Class<? extends JsonNode> type : rootTypes()) {
            String input = type == ArrayNode.class ? "[0.1,1e400,7]" : "{\"small\":0.1,\"huge\":1e400,\"integer\":7}";
            ObjectReader precise = mapper.readerFor(type).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
            sameTree(stock.readerFor(type).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(input), precise.readValue(input));
            sameTree(stock.readerFor(type).with(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS).readValue(type == ArrayNode.class ? "[7]" : "{\"value\":7}"),
                    mapper.readerFor(type).with(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS).readValue(type == ArrayNode.class ? "[7]" : "{\"value\":7}"));
            String lossless = type == ArrayNode.class ? "[0.1]" : "{\"value\":0.1}";
            JsonNode reverted = precise.without(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(lossless);
            assertTrue((type == ArrayNode.class ? reverted.get(0) : reverted.get("value")).isDouble());
        }
        assertTrue(mapper.readTree("0.1").isDouble());
        assertTrue(mapper.reader().with(DeserializationFeature.USE_LONG_FOR_INTS).readTree("7").isLong());
        assertTrue(mapper.reader().with(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS).readTree("7").isBigInteger());
        assertTrue(mapper.readTree(LOSSY).isBigDecimal());
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void treeFeatureTogglesRetainNativeErrorsAndResults(Format format) throws Exception {
        ObjectMapper selective = format.selective();
        ObjectMapper stock = format.stock();
        String duplicate = "{\"value\":1,\"value\":2}";
        for (Class<? extends JsonNode> type : Arrays.asList(JsonNode.class, ObjectNode.class)) {
            sameFailure(stock.readerFor(type).with(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY),
                    selective.readerFor(type).with(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY), duplicate);
            JsonNode allowed = selective.readerFor(type).without(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY).readValue(duplicate);
            assertEquals(2, allowed.get("value").intValue());
            sameTree(stock.readerFor(type).without(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY).readValue(duplicate), allowed);
        }
        String trailing = format == Format.JSON ? "{} []" : "---\n{}\n---\n[]\n";
        sameFailure(stock.readerFor(JsonNode.class).with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS),
                selective.readerFor(JsonNode.class).with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS), trailing);
        sameTree(stock.readerFor(JsonNode.class).without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readValue(trailing),
                selective.readerFor(JsonNode.class).without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readValue(trailing));
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void malformedInputKeepsDiagnosticDetailsAndCauses(Format format) {
        Exception failure = sameFailure(format.stock().readerFor(JsonNode.class),
                format.selective().readerFor(JsonNode.class), "{\n\"value\": [}\n");
        JsonProcessingException diagnostic = assertInstanceOf(JsonProcessingException.class, failure);
        assertNotNull(diagnostic.getLocation());
        assertTrue(diagnostic.getLocation().getLineNr() > 0);
        if (format == Format.YAML) {
            assertNotNull(failure.getCause()); // Exercise nonempty SnakeYAML cause chains too.
        }
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void customNodeFactoryAndDecimalScaleSettingsAreHonored(Format format) throws Exception {
        JsonNodeFactory factory = new TaggedNodeFactory();
        ObjectReader selective = format.selective().reader(factory);
        ObjectReader stock = format.stock().reader(factory);
        for (Class<? extends JsonNode> type : rootTypes()) {
            String input = type == ArrayNode.class ? "[" + LOSSY + "]" : "{\"value\":" + LOSSY + "}";
            JsonNode actual = selective.forType(type).readValue(input);
            assertEquals(type == ArrayNode.class ? TaggedArray.class : TaggedObject.class, actual.getClass());
            JsonNode value = type == ArrayNode.class ? actual.get(0) : actual.get("value");
            assertEquals(6, value.decimalValue().scale());
            sameTree(stock.forType(type).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(input), actual);
        }
        for (boolean strip : new boolean[] { false, true }) {
            ObjectReader left = format.stock().reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
            ObjectReader right = format.selective().reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
            left = strip ? left.with(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES) : left.without(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);
            right = strip ? right.with(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES) : right.without(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES);
            JsonNode actual = right.readTree("{\"value\":1.2300}");
            assertEquals(strip ? 2 : 4, actual.get("value").decimalValue().scale());
            sameTree(left.readTree("{\"value\":1.2300}"), actual);
        }
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void polymorphicTreePropertiesStillDelegateTypeHandling(Format format) throws Exception {
        ObjectMapper selective = format.selective();
        ObjectMapper stock = format.stock();
        for (String input : new String[] { "{\"node\":[\"object\",{\"value\":123.25}]}", "{\"node\":[\"array\",[123.25]]}" }) {
            sameTree(stock.readValue(input, TypedFields.class).node, selective.readValue(input, TypedFields.class).node);
        }
        JsonNode exact = selective.readValue("{\"node\":[\"object\",{\"value\":" + LOSSY + "}]}", TypedFields.class).node;
        assertInstanceOf(ObjectNode.class, exact);
        assertTrue(exact.get("value").isBigDecimal());
        String typedContainers = "{\"object\":[\"object\",{\"value\":" + LOSSY + "}],\"array\":[\"array\",[" + LOSSY + "]]}";
        TypedFields containers = selective.readValue(typedContainers, TypedFields.class);
        assertTrue(containers.object.get("value").isBigDecimal());
        assertTrue(containers.array.get(0).isBigDecimal());
        TypedFields expected = stock.readerFor(TypedFields.class)
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readValue(typedContainers);
        sameTree(expected.object, containers.object);
        sameTree(expected.array, containers.array);
        sameFailure(stock.readerFor(TypedFields.class), selective.readerFor(TypedFields.class), "{\"node\":[\"unknown\",{}]}");
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void typedTreePrecisionReachesANativeSubtypeDeserializer(Format format) throws Exception {
        // A user module can supply a native subtype deserializer after the factory module.
        // It does not install a second precision wrapper: deserializeWithType must supply it.
        ObjectMapper mapper = withNativeObjectDeserializer(format.selective());
        String object = "{\"value\":" + LOSSY + "}";
        assertTrue(mapper.readValue(object, ObjectNode.class).get("value").isDouble());
        JsonNode typed = mapper.readValue("{\"node\":[\"object\"," + object + "]}", TypedFields.class).node;
        assertTrue(typed.get("value").isBigDecimal());
        assertEquals(0, new BigDecimal(LOSSY).compareTo(typed.get("value").decimalValue()));
        JsonNode stock = withNativeObjectDeserializer(format.stock()).readerFor(TypedFields.class)
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .<TypedFields>readValue("{\"node\":[\"object\"," + object + "]}").node;
        sameTree(stock, typed);
    }

    private static ObjectMapper withNativeObjectDeserializer(ObjectMapper mapper) {
        SimpleModule nativeSubtype = new SimpleModule("native-object-subtype");
        nativeSubtype.addDeserializer(ObjectNode.class, new NativeObjectDeserializer());
        return mapper.copy().registerModule(nativeSubtype);
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void scalarUntypedAndPojoBindingRemainNative(Format format) throws Exception {
        ObjectMapper selective = format.selective();
        ObjectMapper stock = format.stock();
        assertEquals(stock.readValue(LOSSY, Double.class), selective.readValue(LOSSY, Double.class));
        assertEquals(Double.valueOf(9007199254740992d), selective.readValue(LOSSY, Double.class));
        String input = "{\"value\":" + LOSSY + ",\"other\":[0.1,7]}";
        Object expected = stock.readValue(input, Object.class);
        Object actual = selective.readValue(input, Object.class);
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected, actual);
        assertInstanceOf(Double.class, ((Map<?, ?>) actual).get("value"));
        DoubleBean left = stock.readValue("{\"value\":" + LOSSY + "}", DoubleBean.class);
        DoubleBean right = selective.readValue("{\"value\":" + LOSSY + "}", DoubleBean.class);
        assertEquals(left.value, right.value);
        assertEquals(Double.valueOf(9007199254740992d), right.value);
        ObjectReader precise = selective.readerFor(Object.class).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        assertEquals(new BigDecimal(LOSSY), precise.readValue(LOSSY));
        assertInstanceOf(Double.class, selective.readValue(LOSSY, Object.class));
    }

    private static ObjectMapper noContainerMerging(ObjectMapper mapper) {
        ObjectMapper copy = mapper.copy();
        copy.configOverride(ObjectNode.class).setMergeable(false);
        copy.configOverride(ArrayNode.class).setMergeable(false);
        return copy;
    }

    private static List<Class<? extends JsonNode>> rootTypes() {
        return Arrays.asList(JsonNode.class, ObjectNode.class, ArrayNode.class);
    }

    private static Exception sameFailure(ObjectReader stock, ObjectReader selective, String input) {
        Exception expected = assertThrows(Exception.class, () -> stock.readValue(input));
        Exception actual = assertThrows(Exception.class, () -> selective.readValue(input));
        sameException(expected, actual);
        return actual;
    }

    private static void sameException(Throwable expected, Throwable actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getMessage(), actual.getMessage());
        if (expected instanceof JsonProcessingException) {
            JsonProcessingException left = (JsonProcessingException) expected;
            JsonProcessingException right = (JsonProcessingException) actual;
            assertEquals(left.getOriginalMessage(), right.getOriginalMessage());
            JsonLocation leftLocation = left.getLocation();
            JsonLocation rightLocation = right.getLocation();
            if (leftLocation == null) {
                assertNull(rightLocation);
            } else {
                assertNotNull(rightLocation);
                assertEquals(leftLocation.getLineNr(), rightLocation.getLineNr());
                assertEquals(leftLocation.getColumnNr(), rightLocation.getColumnNr());
                assertEquals(leftLocation.getCharOffset(), rightLocation.getCharOffset());
                assertEquals(leftLocation.getByteOffset(), rightLocation.getByteOffset());
            }
        }
        sameException(expected.getCause(), actual.getCause());
    }

    private static void sameTree(JsonNode expected, JsonNode actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.toString(), actual.toString());
        if (expected.isBigDecimal()) {
            assertEquals(expected.decimalValue(), actual.decimalValue());
        } else if (expected.isDouble()) {
            assertEquals(Double.doubleToRawLongBits(expected.doubleValue()), Double.doubleToRawLongBits(actual.doubleValue()));
        }
        if (expected.isArray()) {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) sameTree(expected.get(i), actual.get(i));
        } else if (expected.isObject()) {
            java.util.Iterator<String> names = expected.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                sameTree(expected.get(name), actual.get(name));
            }
        }
    }

    public static class TreeFields {
        public JsonNode node;
        public ObjectNode object;
        public ArrayNode array;
    }

    public static class MergeFields {
        @JsonMerge public JsonNode node;
        @JsonMerge public ObjectNode object;
        @JsonMerge public ArrayNode array;
    }

    public static class DoubleBean {
        public Double value;
    }

    public static class TypedFields {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_ARRAY)
        @JsonSubTypes({ @JsonSubTypes.Type(value = ObjectNode.class, name = "object"),
                @JsonSubTypes.Type(value = ArrayNode.class, name = "array") })
        public JsonNode node;
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_ARRAY)
        @JsonSubTypes({ @JsonSubTypes.Type(value = ObjectNode.class, name = "object") })
        public ObjectNode object;
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_ARRAY)
        @JsonSubTypes({ @JsonSubTypes.Type(value = ArrayNode.class, name = "array") })
        public ArrayNode array;
    }

    static class NativeObjectDeserializer extends JsonDeserializer<ObjectNode> {
        @Override
        public ObjectNode deserialize(JsonParser parser, DeserializationContext context) throws java.io.IOException {
            return (ObjectNode) JsonNodeDeserializer.getDeserializer(ObjectNode.class).deserialize(parser, context);
        }
    }

    static class TaggedNodeFactory extends JsonNodeFactory {
        private static final long serialVersionUID = 1L;
        @Override public ObjectNode objectNode() { return new TaggedObject(this); }
        @Override public ArrayNode arrayNode() { return new TaggedArray(this); }
        @Override public NumericNode numberNode(BigDecimal value) {
            return DecimalNode.valueOf(value.setScale(6));
        }
    }

    static class TaggedObject extends ObjectNode {
        private static final long serialVersionUID = 1L;
        TaggedObject(JsonNodeFactory factory) { super(factory); }
    }

    static class TaggedArray extends ArrayNode {
        private static final long serialVersionUID = 1L;
        TaggedArray(JsonNodeFactory factory) { super(factory); }
    }
}
