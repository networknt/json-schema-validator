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

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.node.BinaryNode;

/**
 * {@link BinaryNode} that is {@link JsonLocationAware}.
 */
public class JsonLocationAwareBinaryNode extends BinaryNode implements JsonLocationAware {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final JsonLocation tokenLocation;

    public JsonLocationAwareBinaryNode(byte[] data, JsonLocation tokenLocation) {
        super(data);
        this.tokenLocation = tokenLocation;
    }
    
    public JsonLocationAwareBinaryNode(byte[] data, int offset, int length, JsonLocation tokenLocation) {
        super(data, offset, length);
        this.tokenLocation = tokenLocation;
    }


    @Override
    public JsonLocation tokenLocation() {
        return this.tokenLocation;
    }
}
