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

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.ValidationMessage;

/**
 * {@link SchemaLoader} that disallows loading external resources.
 */
public class DisallowSchemaLoader implements SchemaLoader {
    private static final DisallowSchemaLoader INSTANCE = new DisallowSchemaLoader();

    /**
     * Disallows loading schemas from external resources.
     *
     * @return the disallow schema loader
     */
    public static DisallowSchemaLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor.
     */
    private DisallowSchemaLoader() {
    }

    @Override
    public InputStreamSource getSchema(AbsoluteIri absoluteIri) {
        throw new InvalidSchemaException(ValidationMessage.builder()
                .message("Schema from ''{1}'' is not allowed to be loaded.").arguments(absoluteIri).build());
    }
}
