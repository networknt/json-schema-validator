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

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.jackson.JsonNodeDeserializer;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** Applies selective precision to tree reads, retaining Jackson's native tree behavior. */
final class SelectiveTreeDeserializer extends DelegatingDeserializer {
    private static final long serialVersionUID = 1L;

    private SelectiveTreeDeserializer(ValueDeserializer<?> delegate) {
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
    protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> delegate) {
        return new SelectiveTreeDeserializer(delegate);
    }

    private JsonParser parser(JsonParser source, DeserializationContext context) {
        return SchemaNodeReader.precisionParser(source,
                context.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }

    @Override
    public Object deserialize(JsonParser source, DeserializationContext context) throws tools.jackson.core.JacksonException {
        return _delegatee.deserialize(parser(source, context), context);
    }

    @Override
    public Object deserialize(JsonParser source, DeserializationContext context, Object value) throws tools.jackson.core.JacksonException {
        return super.deserialize(parser(source, context), context, value);
    }

    @Override
    public Object deserializeWithType(JsonParser source, DeserializationContext context, TypeDeserializer type)
            throws tools.jackson.core.JacksonException {
        return _delegatee.deserializeWithType(parser(source, context), context, type);
    }
}
