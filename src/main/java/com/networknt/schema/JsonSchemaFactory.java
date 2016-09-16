package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class JsonSchemaFactory {
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
            return new JsonSchema(mapper, schemaNode);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

}
