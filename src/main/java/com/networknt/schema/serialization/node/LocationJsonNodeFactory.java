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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.databind.util.RawValue;

/**
 * {@link JsonNodeFactory} that creates {@link JsonLocationAware} nodes.
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
        return new JsonLocationAwareBooleanNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public NullNode nullNode() {
        return new JsonLocationAwareNullNode(this.jsonParser.currentTokenLocation());
    }

    @Override
    public JsonNode missingNode() {
        return super.missingNode();
    }

    @Override
    public NumericNode numberNode(byte v) {
        return new JsonLocationAwareIntNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Byte v) {
        return (v == null) ? nullNode() : numberNode(v.intValue());
    }

    @Override
    public NumericNode numberNode(short v) {
        return new JsonLocationAwareShortNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Short value) {
        return (value == null) ? nullNode() : numberNode(value.shortValue());
    }

    @Override
    public NumericNode numberNode(int v) {
        return new JsonLocationAwareIntNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Integer v) {
        return (v == null) ? nullNode() : numberNode(v.intValue());
    }

    @Override
    public NumericNode numberNode(long v) {
        return new JsonLocationAwareLongNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Long v) {
        return (v == null) ? nullNode() : numberNode(v.longValue());
    }

    @Override
    public ValueNode numberNode(BigInteger v) {
        return (v == null) ? nullNode() : new JsonLocationAwareBigIntegerNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public NumericNode numberNode(float v) {
        return new JsonLocationAwareFloatNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Float v) {
        return (v == null) ? nullNode() : numberNode(v.floatValue());
    }

    @Override
    public NumericNode numberNode(double v) {
        return new JsonLocationAwareDoubleNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode numberNode(Double v) {
        return (v == null) ? nullNode() : numberNode(v.doubleValue());
    }

    @Override
    public ValueNode numberNode(BigDecimal v) {
        return (v == null) ? nullNode() : new JsonLocationAwareDecimalNode(v, this.jsonParser.currentTokenLocation());
    }

    @Override
    public TextNode textNode(String text) {
        return new JsonLocationAwareTextNode(text, this.jsonParser.currentTokenLocation());
    }

    @Override
    public BinaryNode binaryNode(byte[] data) {
        return new JsonLocationAwareBinaryNode(data, this.jsonParser.currentTokenLocation());
    }

    @Override
    public BinaryNode binaryNode(byte[] data, int offset, int length) {
        return new JsonLocationAwareBinaryNode(data, offset, length, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ArrayNode arrayNode() {
        return new JsonLocationAwareArrayNode(this, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ArrayNode arrayNode(int capacity) {
        return new JsonLocationAwareArrayNode(this, capacity, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ObjectNode objectNode() {
        return new JsonLocationAwareObjectNode(this, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode pojoNode(Object pojo) {
        return new JsonLocationAwarePOJONode(pojo, this.jsonParser.currentTokenLocation());
    }

    @Override
    public ValueNode rawValueNode(RawValue value) {
        return new JsonLocationAwarePOJONode(value, this.jsonParser.currentTokenLocation());
    }

}
