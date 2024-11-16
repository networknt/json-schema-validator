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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validator for validating the correctness of $id.
 */
public interface JsonSchemaIdValidator {
    /**
     * Validates if the $id value is valid.
     * 
     * @param id                     the $id or id
     * @param rootSchema             true if this is a root schema
     * @param schemaLocation         the schema location
     * @param resolvedSchemaLocation the schema location after resolving with the id
     * @param validationContext      the validation context for instance to get the
     *                               meta schema
     * @return true if valid
     */
    boolean validate(String id, boolean rootSchema, SchemaLocation schemaLocation,
            SchemaLocation resolvedSchemaLocation, ValidationContext validationContext);

    JsonSchemaIdValidator DEFAULT = new DefaultJsonSchemaIdValidator();

    /**
     * Implementation of {@link JsonSchemaIdValidator}.
     * <p>
     * Note that this does not strictly follow the specification.
     * <p>
     * This allows an $id that isn't an absolute-IRI on the root schema, but it must
     * resolve to an absolute-IRI given a base-IRI.
     * <p>
     * This also allows non-empty fragments.
     */
    class DefaultJsonSchemaIdValidator implements JsonSchemaIdValidator {
        @Override
        public boolean validate(String id, boolean rootSchema, SchemaLocation schemaLocation,
                SchemaLocation resolvedSchemaLocation, ValidationContext validationContext) {
            if (hasNoContext(schemaLocation)) {
                // The following are non-standard
                if (isFragment(id) || startsWithSlash(id)) {
                    return true;
                }
            }
            return resolvedSchemaLocation.getAbsoluteIri() != null
                    && isAbsoluteIri(resolvedSchemaLocation.getAbsoluteIri().toString());
        }

        protected boolean startsWithSlash(String id) {
            return id.startsWith("/");
        }

        protected boolean isFragment(String id) {
            return id.startsWith("#");
        }
        
        protected boolean hasNoContext(SchemaLocation schemaLocation) {
            return schemaLocation.getAbsoluteIri() == null || schemaLocation.toString().startsWith("#");
        }

        protected boolean isAbsoluteIri(String iri) {
            if (!iri.contains(":")) {
                return false; // quick check
            }
            try {
                new URI(iri);
            } catch (URISyntaxException e) {
                return false;
            }
            return true;
        }
    }
}
