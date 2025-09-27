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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.Specification;

/**
 * Default {@link DialectRegistry}.
 */
public class DefaultDialectRegistry extends BasicDialectRegistry {
    private final ConcurrentMap<String, Dialect> loadedDialects = new ConcurrentHashMap<>();

    public DefaultDialectRegistry() {
        super();
    }

    public DefaultDialectRegistry(Function<String, Dialect> dialects) {
        super(dialects);
    }

    public DefaultDialectRegistry(Dialect dialect) {
        super(dialect);
    }

    public DefaultDialectRegistry(Collection<Dialect> dialects) {
        super(dialects);
    }

    @Override
    public Dialect getDialect(String dialectId, SchemaRegistry schemaFactory) {
        if (this.dialects != null) {
            Dialect dialect = dialects.apply(dialectId);
            if (dialect != null) {
                return dialect;
            }
        }
        // Is it a well-known dialect?
        Dialect dialect = Specification.getDialect(dialectId);
        if (dialect != null) {
            return dialect;
        }
        return loadedDialects.computeIfAbsent(dialectId, id -> loadDialect(id, schemaFactory));
    }

    private static class Holder {
        private static final DefaultDialectRegistry INSTANCE = new DefaultDialectRegistry();
    }

    public static DefaultDialectRegistry getInstance() {
        return Holder.INSTANCE;
    }
}
