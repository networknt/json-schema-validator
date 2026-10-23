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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Applies selective precision to tree reads, retaining Jackson's native tree behavior. */
final class SelectiveTreeDeserializer extends DelegatingDeserializer {
    private static final long serialVersionUID = 1L;

    private SelectiveTreeDeserializer(JsonDeserializer<?> delegate) {
        super(delegate);
    }

    static SimpleModule module() {
        SimpleModule module = new SimpleModule("networknt-selective-decimal-trees");
        add(module, JsonNode.class);
        add(module, ObjectNode.class);
        add(module, ArrayNode.class);
        return module;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void add(SimpleModule module, Class<? extends JsonNode> type) {
        // Jackson's tree factory returns a wildcard; its native delegate retains the requested root type.
        module.addDeserializer((Class) type, new SelectiveTreeDeserializer(JsonNodeDeserializer.getDeserializer(type)));
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> delegate) {
        return new SelectiveTreeDeserializer(delegate);
    }

    private JsonParser parser(JsonParser source, DeserializationContext context) {
        return SchemaNodeReader.precisionParser(source,
                context.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }

    @Override
    public Object deserialize(JsonParser source, DeserializationContext context) throws java.io.IOException {
        return _delegatee.deserialize(parser(source, context), context);
    }

    @Override
    public Object deserialize(JsonParser source, DeserializationContext context, Object value) throws java.io.IOException {
        return super.deserialize(parser(source, context), context, value);
    }

    @Override
    public Object deserializeWithType(JsonParser source, DeserializationContext context, TypeDeserializer type)
            throws java.io.IOException {
        return _delegatee.deserializeWithType(parser(source, context), context, type);
    }
}
