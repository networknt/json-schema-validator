/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class JsonSchemaFactory {

    // Draft 6 uses "$id"
    private static final String DRAFT_4_ID = "id";

    private static final Logger logger = LoggerFactory
            .getLogger(JsonSchemaFactory.class);
    private ObjectMapper mapper;

    public JsonSchemaFactory() {
        this(new ObjectMapper());
    }

    public JsonSchemaFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonSchema getSchema(String schema) {
        try {
            JsonNode schemaNode = mapper.readTree(schema);
            return new JsonSchema(mapper, schemaNode);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    public JsonSchema getSchema(InputStream schemaStream) {
        try {
            JsonNode schemaNode = mapper.readTree(schemaStream);
            return new JsonSchema(mapper, schemaNode);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    public JsonSchema getSchema(URL schemaURL) {
        try {

            JsonNode schemaNode = mapper.readTree(schemaURL.openStream());

            if (this.idMatchesSourceUrl(schemaNode, schemaURL)) {
                return new JsonSchema(mapper, schemaNode, null);
            }

            return new JsonSchema(mapper, schemaNode);

        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    public JsonSchema getSchema(JsonNode jsonNode) {
        return new JsonSchema(mapper, jsonNode);
    }

    private boolean idMatchesSourceUrl(JsonNode schema, URL schemaUrl) {

        JsonNode idNode = schema.get(DRAFT_4_ID);

        if (idNode == null) {
            return false;
        }

        String id = idNode.asText();
        logger.info("Matching " + id + " to " + schemaUrl.toString());
        return id.equals(schemaUrl.toString());

    }

}
