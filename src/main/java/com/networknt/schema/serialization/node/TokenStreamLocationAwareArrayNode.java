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

import java.util.List;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * {@link ArrayNode} that is {@link TokenStreamLocationAware}.
 */
public class TokenStreamLocationAwareArrayNode extends ArrayNode implements TokenStreamLocationAware {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final JsonLocation tokenStreamLocation;

    public TokenStreamLocationAwareArrayNode(JsonNodeFactory nf, int capacity, JsonLocation tokenStreamLocation) {
        super(nf, capacity);
        this.tokenStreamLocation = tokenStreamLocation;
    }

    public TokenStreamLocationAwareArrayNode(JsonNodeFactory nf, List<JsonNode> children, JsonLocation tokenStreamLocation) {
        super(nf, children);
        this.tokenStreamLocation = tokenStreamLocation;
    }

    public TokenStreamLocationAwareArrayNode(JsonNodeFactory nf, JsonLocation tokenStreamLocation) {
        super(nf);
        this.tokenStreamLocation = tokenStreamLocation;
    }

    @Override
    public JsonLocation tokenStreamLocation() {
        return this.tokenStreamLocation;
    }
}
