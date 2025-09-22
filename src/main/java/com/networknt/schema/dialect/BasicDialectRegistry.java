/*
 * Copyright (c) 2025 the original author or authors.
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

import java.util.function.Function;

import com.networknt.schema.Error;
import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaValidatorsConfig;

public class BasicDialectRegistry implements DialectRegistry {
    private Function<String, Dialect> dialects;

    public BasicDialectRegistry(Function<String, Dialect> dialects) {
        this.dialects = dialects;
    }

    public BasicDialectRegistry(Dialect dialect) {
        this.dialects = dialectId -> dialect.getIri().equals(dialectId) ? dialect : null;
    }

    @Override
    public Dialect getDialect(String dialectId, SchemaRegistry schemaRegistry, SchemaValidatorsConfig config) {
        Dialect dialect = dialects.apply(dialectId);
        if (dialect != null) {
            return dialect;
        }
        throw new InvalidSchemaException(Error.builder()
                .message("Unknown dialect ''{0}''. Only dialects that are explicitly configured can be used.")
                .arguments(dialectId).build());
    }
}
