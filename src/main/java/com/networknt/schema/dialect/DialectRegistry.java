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

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;

/**
 * Registry for {@link Dialect} that can be retrieved using the dialect id which
 * is the IRI that indicates the meta-schema that can be used to validate the
 * schema conforms to the dialect.
 */
@FunctionalInterface
public interface DialectRegistry {
    /**
     * Gets the dialect given the dialect id which is the IRI that indicates the
     * meta-schema that can be used to validate the schema conforms to the dialect.
     * 
     * @param dialectId     the dialect id of the dialect which IRI that indicates
     *                      the meta-schema that can be used to validate the schema
     *                      conforms to the dialect
     * @param schemaFactory the schema factory
     * @param config        the config
     * @return the dialect
     */
    Dialect getDialect(String dialectId, JsonSchemaFactory schemaFactory, SchemaValidatorsConfig config);
}
