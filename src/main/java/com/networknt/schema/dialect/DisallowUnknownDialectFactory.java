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

import com.networknt.schema.Error;
import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;

/**
 * A {@link DialectRegistry} that does not meta-schemas that aren't
 * explicitly configured in the {@link JsonSchemaFactory}.
 */
public class DisallowUnknownDialectFactory implements DialectRegistry {
    @Override
    public Dialect getDialect(String dialectId, JsonSchemaFactory schemaFactory, SchemaValidatorsConfig config) {
        throw new InvalidSchemaException(Error.builder()
                .message("Unknown dialect ''{0}''. Only dialects that are explicitly configured can be used.")
                .arguments(dialectId).build());
    }

    private static class Holder {
        private static final DisallowUnknownDialectFactory INSTANCE = new DisallowUnknownDialectFactory();
    }

    /**
     * Gets the instance of {@link DisallowUnknownDialectFactory}.
     * 
     * @return the json meta schema factory
     */
    public static DisallowUnknownDialectFactory getInstance() {
        return Holder.INSTANCE;
    }
}
