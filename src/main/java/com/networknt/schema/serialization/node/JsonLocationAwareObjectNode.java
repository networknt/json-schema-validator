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

import java.util.Map;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link ObjectNode} that is {@link JsonLocationAware}.
 */
public class JsonLocationAwareObjectNode extends ObjectNode implements JsonLocationAware {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final JsonLocation tokenLocation;

    public JsonLocationAwareObjectNode(JsonNodeFactory nc, Map<String, JsonNode> children, JsonLocation tokenLocation) {
        super(nc, children);
        this.tokenLocation = tokenLocation;
    }

    public JsonLocationAwareObjectNode(JsonNodeFactory nc, JsonLocation tokenLocation) {
        super(nc);
        this.tokenLocation = tokenLocation;
    }

    @Override
    public JsonLocation tokenLocation() {
        return this.tokenLocation;
    }
}
