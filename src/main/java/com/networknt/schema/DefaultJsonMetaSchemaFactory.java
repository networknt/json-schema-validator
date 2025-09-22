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
package com.networknt.schema;

import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.Dialect;

/**
 * Default {@link JsonMetaSchemaFactory}.
 */
public class DefaultJsonMetaSchemaFactory implements JsonMetaSchemaFactory {
    @Override
    public Dialect getMetaSchema(String iri, JsonSchemaFactory schemaFactory, SchemaValidatorsConfig config) {
        // Is it a well-known dialect?
        return Specification.Version.fromDialectId(iri)
                .map(JsonSchemaFactory::checkVersion)
                .orElseGet(() -> {
                    // Custom meta schema
                    return loadMetaSchema(iri, schemaFactory, config);
                });
    }

    protected Dialect loadMetaSchema(String iri, JsonSchemaFactory schemaFactory,
            SchemaValidatorsConfig config) {
        try {
            Dialect result = loadMetaSchemaBuilder(iri, schemaFactory, config).build();
            return result;
        } catch (InvalidSchemaException e) {
            throw e;
        } catch (Exception e) {
            Error error = Error.builder()
                    .message("Failed to load meta-schema ''{0}''").arguments(iri).build();
            throw new InvalidSchemaException(error, e);
        }
    }

    protected Dialect.Builder loadMetaSchemaBuilder(String iri, JsonSchemaFactory schemaFactory,
            SchemaValidatorsConfig config) {
        JsonSchema schema = schemaFactory.getSchema(SchemaLocation.of(iri), config);
        Dialect.Builder builder = Dialect.builder(iri, schema.getValidationContext().getMetaSchema());
        Version specification = schema.getValidationContext().getMetaSchema().getSpecification();
        if (specification != null) {
            if (specification.getOrder() >= Version.DRAFT_2019_09.getOrder()) {
                // Process vocabularies
                JsonNode vocabulary = schema.getSchemaNode().get("$vocabulary");
                if (vocabulary != null) {
                    builder.vocabularies(Map::clear);
                    for (Entry<String, JsonNode> vocabs : vocabulary.properties()) {
                        builder.vocabulary(vocabs.getKey(), vocabs.getValue().booleanValue());
                    }
                }
            }
        }
        return builder;
    }

    private static class Holder {
        private static final DefaultJsonMetaSchemaFactory INSTANCE = new DefaultJsonMetaSchemaFactory();
    }

    public static DefaultJsonMetaSchemaFactory getInstance() {
        return Holder.INSTANCE;
    }
}
