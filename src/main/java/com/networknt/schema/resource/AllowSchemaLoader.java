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
package com.networknt.schema.resource;

import java.util.function.Predicate;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.ValidationMessage;

/**
 * {@link SchemaLoader} that allows loading external resources.
 */
public class AllowSchemaLoader implements SchemaLoader {
    private final Predicate<AbsoluteIri> allowed;

    /**
     * Constructor.
     * 
     * @param allowed the predicate to determine which external resource is allowed
     *                to be loaded
     */
    public AllowSchemaLoader(Predicate<AbsoluteIri> allowed) {
        this.allowed = allowed;
    }

    @Override
    public InputStreamSource getSchema(AbsoluteIri absoluteIri) {
        if (this.allowed.test(absoluteIri)) {
            // Allow to delegate to the next schema loader
            return null;
        }
        throw new InvalidSchemaException(ValidationMessage.builder()
                .message("Schema from ''{1}'' is not allowed to be loaded.").arguments(absoluteIri).build());
    }
}
