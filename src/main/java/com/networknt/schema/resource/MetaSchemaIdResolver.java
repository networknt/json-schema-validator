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

/**
 * Maps the JSON Schema meta schema to the class path location.
 */
public class MetaSchemaIdResolver implements SchemaIdResolver {
    private static final char ANCHOR = '#';
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String HTTP_JSON_SCHEMA_ORG_PREFIX = "http://json-schema.org/";
    private static final String HTTPS_JSON_SCHEMA_ORG_PREFIX = "https://json-schema.org/";

    @Override
    public AbsoluteIri resolve(AbsoluteIri absoluteIRI) {
        String absoluteIRIString = absoluteIRI != null ? absoluteIRI.toString() : null;
        if (absoluteIRIString != null) {
            if (absoluteIRIString.startsWith(HTTPS_JSON_SCHEMA_ORG_PREFIX)) {
                return AbsoluteIri.of(CLASSPATH_PREFIX + absoluteIRIString.substring(24));
            } else if (absoluteIRIString.startsWith(HTTP_JSON_SCHEMA_ORG_PREFIX)) {
                int endIndex = absoluteIRIString.length();
                if (absoluteIRIString.charAt(endIndex - 1) == ANCHOR) {
                    endIndex = endIndex - 1;
                }
                return AbsoluteIri.of(CLASSPATH_PREFIX + absoluteIRIString.substring(23, endIndex));
            }
        }
        return null;
    }
}
