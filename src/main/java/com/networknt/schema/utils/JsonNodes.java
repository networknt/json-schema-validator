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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonNodePath;

/**
 * Utility methods for JsonNode.
 */
public class JsonNodes {
    /**
     * Gets the node found at the path.
     * 
     * @param node the node
     * @param path the path
     * @return the node found at the path or null
     */
    public static JsonNode get(JsonNode node, JsonNodePath path) {
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
        return current;
    }

    /**
     * Gets the node given the property or index.
     * 
     * @param node the node
     * @param propertyOrIndex the property or index
     * @return the node given the property or index
     */
    public static JsonNode get(JsonNode node, Object propertyOrIndex) {
        JsonNode value = null;
        if (propertyOrIndex instanceof Number) {
            value = node.get(((Number) propertyOrIndex).intValue());
        } else {
            // In the case of string this represents an escaped json pointer and thus does not reflect the property directly
            String unescaped = propertyOrIndex.toString();
            if (unescaped.contains("~")) {
                unescaped = unescaped.replace("~1", "/");
                unescaped = unescaped.replace("~0", "~");
            }
            if (unescaped.contains("%")) {
                try {
                    unescaped = URLDecoder.decode(unescaped, StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    // Do nothing
                }
            }
            
            value = node.get(unescaped);
        }
        return value;
    }
}
