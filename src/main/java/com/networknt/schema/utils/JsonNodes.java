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
package com.networknt.schema.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.serialization.node.TokenStreamLocationAware;
import com.networknt.schema.serialization.node.JsonNodeFactoryFactory;

/**
 * Utility methods for JsonNode.
 */
public class JsonNodes {
    /** Gets an exact count-limit token without changing the node's ordinary numeric behavior. */
    public static BigDecimal decimalValueForCountLimit(JsonNode node) {
        return node instanceof CountLimitDoubleNode ? ((CountLimitDoubleNode) node).exactValue : node.decimalValue();
    }

    /** Checks count-limit integrality, including finite tokens that overflow a double. */
    public static boolean isIntegralCountLimit(JsonNode node) {
        if (node instanceof CountLimitDoubleNode) {
            BigDecimal value = ((CountLimitDoubleNode) node).exactValue;
            return value.scale() <= 0 || value.stripTrailingZeros().scale() <= 0;
        }
        return node.canConvertToExactIntegral();
    }

    private static final class CountLimitDoubleNode extends DoubleNode implements TokenStreamLocationAware {
        private static final long serialVersionUID = 1L;
        private final BigDecimal exactValue;
        private final JsonLocation location;

        CountLimitDoubleNode(DoubleNode ordinary, BigDecimal exactValue) {
            super(ordinary.doubleValue());
            this.exactValue = exactValue;
            this.location = ordinary instanceof TokenStreamLocationAware
                    ? ((TokenStreamLocationAware) ordinary).tokenStreamLocation() : null;
        }

        @Override
        public JsonLocation tokenStreamLocation() {
            return this.location;
        }
    }

    /** Delegates normal node creation, attaching metadata only to default double count tokens. */
    private static final class CountLimitNodeFactory extends JsonNodeFactory {
        private static final long serialVersionUID = 1L;
        private final JsonNodeFactory delegate;
        private final CountLimitParser parser;
        private final boolean decimalFloats;

        CountLimitNodeFactory(JsonNodeFactory delegate, CountLimitParser parser, boolean decimalFloats) {
            this.delegate = delegate;
            this.parser = parser;
            this.decimalFloats = decimalFloats;
        }

        @Override
        public ValueNode numberNode(BigDecimal value) {
            if (this.parser.countLimit && !this.decimalFloats) {
                NumericNode ordinary = this.delegate.numberNode(value.doubleValue());
                if (ordinary instanceof DoubleNode) {
                    return new CountLimitDoubleNode((DoubleNode) ordinary, value);
                }
            }
            return this.delegate.numberNode(value);
        }

        @Override
        public BooleanNode booleanNode(boolean value) {
            return this.delegate.booleanNode(value);
        }

        @Override
        public JsonNode missingNode() {
            return this.delegate.missingNode();
        }

        @Override
        public NullNode nullNode() {
            return this.delegate.nullNode();
        }

        @Override
        public NumericNode numberNode(byte value) {
            return this.delegate.numberNode(value);
        }

        @Override
        public NumericNode numberNode(short value) {
            return this.delegate.numberNode(value);
        }

        @Override
        public NumericNode numberNode(int value) {
            return this.delegate.numberNode(value);
        }

        @Override
        public NumericNode numberNode(long value) {
            return this.delegate.numberNode(value);
        }

        @Override
        public ValueNode numberNode(BigInteger value) {
            return this.delegate.numberNode(value);
        }

        @Override
        public NumericNode numberNode(float value) {
            return this.delegate.numberNode(value);
        }

        @Override
        public NumericNode numberNode(double value) {
            return this.delegate.numberNode(value);
        }

        @Override
        public TextNode textNode(String value) {
            return this.delegate.textNode(value);
        }

        @Override
        public BinaryNode binaryNode(byte[] value) {
            return this.delegate.binaryNode(value);
        }

        @Override
        public BinaryNode binaryNode(byte[] value, int offset, int length) {
            return this.delegate.binaryNode(value, offset, length);
        }

        @Override
        public ArrayNode arrayNode() {
            return this.delegate.arrayNode();
        }

        @Override
        public ArrayNode arrayNode(int capacity) {
            return this.delegate.arrayNode(capacity);
        }

        @Override
        public ObjectNode objectNode() {
            return this.delegate.objectNode();
        }
    }


    /** Reads schema count limits as decimals without changing other numbers or node factories. */
    public static JsonNode readSchemaTree(ObjectMapper objectMapper, String content,
            JsonNodeFactoryFactory jsonNodeFactoryFactory) {
        try (CountLimitParser parser = new CountLimitParser(objectMapper.createParser(content))) {
            JsonNodeFactory nodeFactory = jsonNodeFactoryFactory == null ? objectMapper.getNodeFactory()
                    : jsonNodeFactoryFactory.getJsonNodeFactory(parser);
            JsonNodeFactory schemaFactory = new CountLimitNodeFactory(nodeFactory, parser,
                    objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
            JsonNode result = objectMapper.reader(schemaFactory).readTree(parser);
            return result != null ? result : nodeFactory.missingNode();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid input", e);
        }
    }

    /** Reads schema count limits as decimals without changing other numbers or node factories. */
    public static JsonNode readSchemaTree(ObjectMapper objectMapper, InputStream content,
            JsonNodeFactoryFactory jsonNodeFactoryFactory) {
        try (CountLimitParser parser = new CountLimitParser(objectMapper.createParser(content))) {
            JsonNodeFactory nodeFactory = jsonNodeFactoryFactory == null ? objectMapper.getNodeFactory()
                    : jsonNodeFactoryFactory.getJsonNodeFactory(parser);
            JsonNodeFactory schemaFactory = new CountLimitNodeFactory(nodeFactory, parser,
                    objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
            JsonNode result = objectMapper.reader(schemaFactory).readTree(parser);
            return result != null ? result : nodeFactory.missingNode();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid input", e);
        }
    }

    /** Preserves the token's decimal value only for count keywords in schemas. */
    private static final class CountLimitParser extends JsonParserDelegate {
        private boolean countLimit;

        CountLimitParser(JsonParser parser) {
            super(parser);
        }

        private boolean isCountLimit() throws IOException {
            String name = currentName();
            this.countLimit = "minItems".equals(name) || "maxItems".equals(name)
                    || "minProperties".equals(name) || "maxProperties".equals(name)
                    || "minLength".equals(name) || "maxLength".equals(name)
                    || "minContains".equals(name) || "maxContains".equals(name);
            return this.countLimit;
        }

        @Override
        public NumberType getNumberType() throws IOException {
            return currentToken() == com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT
                    && isCountLimit() && !isNaN() ? NumberType.BIG_DECIMAL : super.getNumberType();
        }

        @Override
        public NumberTypeFP getNumberTypeFP() throws IOException {
            return isCountLimit() && !isNaN() ? NumberTypeFP.BIG_DECIMAL : super.getNumberTypeFP();
        }
    }


    /**
     * Gets the node found at the path.
     *
     * @param <T> the type of the node
     * @param node the node
     * @param path the path
     * @return the node found at the path or null
     */
    @SuppressWarnings("unchecked")
    public static <T extends JsonNode> T get(JsonNode node, NodePath path) {
        int nameCount = path.getNameCount();
        JsonNode current = node;
        for (int x = 0; x < nameCount; x++) {
            Object segment = path.getElement(x);
            JsonNode result = get(current, segment);
            if (result == null) {
                return null;
            }
            current = result;
        }
        return (T) current;
    }

    /**
     * Gets the node given the property or index.
     *
     * @param <T> the type of the node
     * @param node the node
     * @param propertyOrIndex the property or index
     * @return the node given the property or index
     */
    @SuppressWarnings("unchecked")
    public static <T extends JsonNode> T get(JsonNode node, Object propertyOrIndex) {
        JsonNode value = null;
        if (propertyOrIndex instanceof Number && node.isArray()) {
            value = node.get(((Number) propertyOrIndex).intValue());
        } else {
            value = node.get(propertyOrIndex.toString());
        }
        return (T) value;
    }

    /**
     * Read a {@link JsonNode} from {@link String} content.
     * 
     * @param objectMapper the object mapper
     * @param content the string content
     * @param jsonNodeFactoryFactory the factory
     * @return the json node
     */
    public static JsonNode readTree(ObjectMapper objectMapper, String content,
            JsonNodeFactoryFactory jsonNodeFactoryFactory) {
        JsonFactory factory = objectMapper.getFactory();
        try (JsonParser parser = factory.createParser(content)) {
            JsonNodeFactory nodeFactory = jsonNodeFactoryFactory.getJsonNodeFactory(parser);
            ObjectReader reader = objectMapper.reader(nodeFactory);
            JsonNode result = reader.readTree(parser);
            return (result != null) ? result : nodeFactory.missingNode();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid input", e);
        }
    }

    /**
     * Read a {@link JsonNode} from an {@link InputStream}.
     * 
     * @param objectMapper the object mapper
     * @param inputStream the string content
     * @param jsonNodeFactoryFactory the factory 
     * @return the json node
     */
    public static JsonNode readTree(ObjectMapper objectMapper, InputStream inputStream,
            JsonNodeFactoryFactory jsonNodeFactoryFactory) {
        JsonFactory factory = objectMapper.getFactory();
        try (JsonParser parser = factory.createParser(inputStream)) {
            JsonNodeFactory nodeFactory = jsonNodeFactoryFactory.getJsonNodeFactory(parser);
            ObjectReader reader = objectMapper.reader(nodeFactory);
            JsonNode result = reader.readTree(parser);
            return (result != null) ? result : nodeFactory.missingNode();
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid input", e);
        }
    }

    /**
     * Gets the token location of the {@link JsonNode} that implements {@link TokenStreamLocationAware}.
     * 
     * @param jsonNode the node
     * @return the JsonLocation
     */
    public static JsonLocation tokenStreamLocationOf(JsonNode jsonNode) {
        if (jsonNode instanceof TokenStreamLocationAware) {
            return ((TokenStreamLocationAware) jsonNode).tokenStreamLocation();
        }
        throw new IllegalArgumentException("JsonNode does not contain the location information.");
    }
}
