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
package com.networknt.schema.serialization.node;

import java.math.BigDecimal;
import java.math.BigInteger;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BinaryNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.NumericNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;
import tools.jackson.databind.node.ValueNode;
import tools.jackson.databind.util.RawValue;

/**
 * {@link JsonNodeFactory} that creates {@link TokenStreamLocationAware} nodes.
 * <p>
 * Note that this will adversely affect performance as nodes with the same value
 * can no longer be cached and reused.
 */
public class LocationJsonNodeFactory extends JsonNodeFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final JsonParser jsonParser;

    /**
     * Constructor.
     *
     * @param jsonParser the json parser
     */
    public LocationJsonNodeFactory(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    @Override
    public BooleanNode booleanNode(boolean v) {
        return new TokenStreamLocationAwareBooleanNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public NullNode nullNode() {
        return new TokenStreamLocationAwareNullNode(this.jsonParser.currentTokenLocation());
    }

    @Override
    public JsonNode missingNode() {
        return super.missingNode();
    }

    @Override
    public NumericNode numberNode(byte v) {
        return new TokenStreamLocationAwareIntNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Byte v) {
        return (v == null) ? nullNode() : numberNode(v.intValue());
    }

    @Override
    public NumericNode numberNode(short v) {
        return new TokenStreamLocationAwareShortNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Short value) {
        return (value == null) ? nullNode() : numberNode(value.shortValue());
    }

    @Override
    public NumericNode numberNode(int v) {
        return new TokenStreamLocationAwareIntNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Integer v) {
        return (v == null) ? nullNode() : numberNode(v.intValue());
    }

    @Override
    public NumericNode numberNode(long v) {
        return new TokenStreamLocationAwareLongNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Long v) {
        return (v == null) ? nullNode() : numberNode(v.longValue());
    }

    @Override
    public ValueNode numberNode(BigInteger v) {
        return (v == null) ? nullNode() : new TokenStreamLocationAwareBigIntegerNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public NumericNode numberNode(float v) {
        return new TokenStreamLocationAwareFloatNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Float v) {
        return (v == null) ? nullNode() : numberNode(v.floatValue());
    }

    @Override
    public NumericNode numberNode(double v) {
        return new TokenStreamLocationAwareDoubleNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Double v) {
        return (v == null) ? nullNode() : numberNode(v.doubleValue());
    }

    @Override
    public ValueNode numberNode(BigDecimal v) {
        return (v == null) ? nullNode() : new TokenStreamLocationAwareDecimalNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public StringNode stringNode(String text) {
        return new TokenStreamLocationAwareStringNode(text, this.jsonParser.currentTokenLocation());
    }

    @Override
    public BinaryNode binaryNode(byte[] data) {
        return new TokenStreamLocationAwareBinaryNode(data, this.jsonParser.currentTokenLocation());
    }

    @Override
    public BinaryNode binaryNode(byte[] data, int offset, int length) {
        return new TokenStreamLocationAwareBinaryNode(data, offset, length, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ArrayNode arrayNode() {
        return new TokenStreamLocationAwareArrayNode(this, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ArrayNode arrayNode(int capacity) {
        return new TokenStreamLocationAwareArrayNode(this, capacity, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ObjectNode objectNode() {
        return new TokenStreamLocationAwareObjectNode(this, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode pojoNode(Object pojo) {
        return new TokenStreamLocationAwarePOJONode(pojo, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode rawValueNode(RawValue value) {
        return new TokenStreamLocationAwarePOJONode(value, this.jsonParser.currentTokenLocation());
    }

}
