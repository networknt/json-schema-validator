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
package com.networknt.schema.dialect;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.Specification;
import com.networknt.schema.Specification.Version;

/**
 * Default {@link DialectRegistry}.
 */
public class DefaultDialectRegistry implements DialectRegistry {
    private final ConcurrentMap<String, Dialect> dialects = new ConcurrentHashMap<>();
    
    @Override
    public Dialect getDialect(String dialectId, SchemaRegistry schemaFactory, SchemaValidatorsConfig config) {
        // Is it a well-known dialect?
        Dialect dialect = Specification.getDialect(dialectId);
        if (dialect != null) {
            return dialect;
        }
        return dialects.computeIfAbsent(dialectId, id -> loadDialect(id, schemaFactory, config));
    }

    protected Dialect loadDialect(String iri, SchemaRegistry schemaFactory, SchemaValidatorsConfig config) {
        try {
            Dialect result = loadDialectBuilder(iri, schemaFactory, config).build();
            return result;
        } catch (InvalidSchemaException e) {
            throw e;
        } catch (Exception e) {
            Error error = Error.builder().message("Failed to load dialect ''{0}''").arguments(iri).build();
            throw new InvalidSchemaException(error, e);
        }
    }

    protected Dialect.Builder loadDialectBuilder(String iri, SchemaRegistry schemaFactory,
            SchemaValidatorsConfig config) {
        Schema schema = schemaFactory.getSchema(SchemaLocation.of(iri), config);
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
        private static final DefaultDialectRegistry INSTANCE = new DefaultDialectRegistry();
    }

    public static DefaultDialectRegistry getInstance() {
        return Holder.INSTANCE;
    }
}
