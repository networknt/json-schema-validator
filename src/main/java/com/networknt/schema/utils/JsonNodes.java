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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.serialization.node.JsonLocationAware;
import com.networknt.schema.serialization.node.JsonNodeFactoryFactory;

/**
 * Utility methods for JsonNode.
 */
public class JsonNodes {
    /**
     * Gets the node found at the path.
     *
     * @param <T> the type of the node
     * @param node the node
     * @param path the path
     * @return the node found at the path or null
     */
    @SuppressWarnings("unchecked")
    public static <T extends JsonNode> T get(JsonNode node, JsonNodePath path) {
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
     * Gets the token location of the {@link JsonNode} that implements {@link JsonLocationAware}.
     * 
     * @param jsonNode the node
     * @return the JsonLocation
     */
    public static JsonLocation tokenLocationOf(JsonNode jsonNode) {
        if (jsonNode instanceof JsonLocationAware) {
            return ((JsonLocationAware) jsonNode).tokenLocation();
        }
        throw new IllegalArgumentException("JsonNode does not contain the location information.");
    }
}
