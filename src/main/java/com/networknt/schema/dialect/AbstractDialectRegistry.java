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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

/**
 * Abstract {@link DialectRegistry}.
 */
public abstract class AbstractDialectRegistry implements DialectRegistry {
    protected Dialect loadDialect(String iri, SchemaRegistry schemaFactory) {
        try {
            Dialect result = loadDialectBuilder(iri, schemaFactory).build();
            return result;
        } catch (InvalidSchemaException e) {
            throw e;
        } catch (Exception e) {
            Error error = Error.builder().message("Failed to load dialect ''{0}''").arguments(iri).build();
            throw new InvalidSchemaException(error, e);
        }
    }

    protected Dialect.Builder loadDialectBuilder(String iri, SchemaRegistry schemaFactory) {
        Schema schema = schemaFactory.getSchema(SchemaLocation.of(iri));
        Dialect.Builder builder = Dialect.builder(iri, schema.getSchemaContext().getDialect());
        SpecificationVersion specification = schema.getSchemaContext().getDialect().getSpecificationVersion();
        if (specification != null) {
            if (specification.getOrder() >= SpecificationVersion.DRAFT_2019_09.getOrder()) {
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
}
