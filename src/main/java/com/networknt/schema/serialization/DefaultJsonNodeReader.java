package com.networknt.schema.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.InputFormat;
import com.networknt.schema.serialization.node.JsonNodeFactoryFactory;
import com.networknt.schema.serialization.node.LocationJsonNodeFactoryFactory;
import com.networknt.schema.utils.JsonNodes;

/**
 * Default {@link JsonNodeReader}.
 */
public class DefaultJsonNodeReader implements JsonNodeReader {
    protected final ObjectMapper jsonMapper;
    protected final ObjectMapper yamlMapper;
    protected final JsonNodeFactoryFactory jsonNodeFactoryFactory;

    /**
     * Constructor.
     *
     * @param jsonMapper the json mapper
     * @param yamlMapper the yaml mapper
     * @param jsonNodeFactoryFactory the json node factory factory
     */
    protected DefaultJsonNodeReader(ObjectMapper jsonMapper, ObjectMapper yamlMapper,
            JsonNodeFactoryFactory jsonNodeFactoryFactory) {
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.jsonNodeFactoryFactory = jsonNodeFactoryFactory;
    }

    @Override
    public JsonNode readTree(String content, InputFormat inputFormat) throws IOException {
        if (this.jsonNodeFactoryFactory == null) {
            return getObjectMapper(inputFormat).readTree(content);
        } else {
            return JsonNodes.readTree(getObjectMapper(inputFormat), content, this.jsonNodeFactoryFactory);
        }
    }

    @Override
    public JsonNode readTree(InputStream content, InputFormat inputFormat) throws IOException {
        if (this.jsonNodeFactoryFactory == null) {
            return getObjectMapper(inputFormat).readTree(content);
        } else {
            return JsonNodes.readTree(getObjectMapper(inputFormat), content, this.jsonNodeFactoryFactory);
        }
    }

    /**
     * Gets the yaml mapper.
     * 
     * @return the yaml mapper
     */
    protected ObjectMapper getYamlMapper() {
        return this.yamlMapper != null ? this.yamlMapper : YamlMapperFactory.getInstance();
    }

    /**
     * Gets the json mapper.
     * 
     * @return the json mapper
     */
    protected ObjectMapper getJsonMapper() {
        return this.jsonMapper != null ? this.jsonMapper : JsonMapperFactory.getInstance();
    }

    /**
     * Gets the object mapper for the input format.
     * 
     * @param inputFormat the input format
     * @return the object mapper
     */
    protected ObjectMapper getObjectMapper(InputFormat inputFormat) {
        if (InputFormat.JSON.equals(inputFormat)) {
            return getJsonMapper();
        } else if (InputFormat.YAML.equals(inputFormat)) {
            return getYamlMapper();
        }
        throw new IllegalArgumentException("Unsupported input format "+inputFormat); 
    }

    /**
     * Gets the builder for {@link DefaultJsonNodeReader}.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder support for {@link JsonNodeReader}.
     * 
     * @param <T> the super type
     */
    public static abstract class BuilderSupport<T> {
        protected ObjectMapper jsonMapper = null;
        protected ObjectMapper yamlMapper = null;
        protected JsonNodeFactoryFactory jsonNodeFactoryFactory = null;

        protected abstract T self();

        /**
         * Sets the json mapper. 
         *
         * @param jsonMapper the json mapper
         * @return the builder
         */
        public T jsonMapper(ObjectMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
            return self();
        }

        /**
         * Sets the yaml mapper
         * 
         * @param yamlMapper the yaml mapper
         * @return the builder
         */
        public T yamlMapper(ObjectMapper yamlMapper) {
            this.yamlMapper = yamlMapper;
            return self();
        }

        /**
         * Configures the {@link JsonNodeFactoryFactory} to use.
         * <p>
         * To get location information from {@link JsonNode} the
         * {@link com.networknt.schema.serialization.node.LocationJsonNodeFactoryFactory}
         * can be used.
         *
         * @param jsonNodeFactoryFactory the factory to create json node factories
         * @return the builder
         */
        public T jsonNodeFactoryFactory(JsonNodeFactoryFactory jsonNodeFactoryFactory) {
            this.jsonNodeFactoryFactory = jsonNodeFactoryFactory;
            return self();
        }
    }

    /**
     * Builder for {@link DefaultJsonNodeReader}. 
     */
    public static class Builder extends BuilderSupport<Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Makes the nodes generated location aware.
         *
         * @return the builder
         */
        public Builder locationAware() {
            return jsonNodeFactoryFactory(LocationJsonNodeFactoryFactory.getInstance());
        }

        /**
         * Builds the {@link JsonNodeReader}.
         *
         * @return the object reader
         */
        public JsonNodeReader build() {
            return new DefaultJsonNodeReader(this.jsonMapper, this.yamlMapper, this.jsonNodeFactoryFactory);
        }
    }
}
