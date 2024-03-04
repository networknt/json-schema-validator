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

/**
 * Schema Identifier used in $schema.
 */
public class SchemaId {
    /**
     * Draft 4.
     */
    public static final String V4 = "http://json-schema.org/draft-04/schema#";

    /**
     * Draft 6.
     */
    public static final String V6 = "http://json-schema.org/draft-06/schema#";

    /**
     * Draft 7.
     */
    public static final String V7 = "http://json-schema.org/draft-07/schema#";

    /**
     * Draft 2019-09.
     */
    public static final String V201909 = "https://json-schema.org/draft/2019-09/schema";

    /**
     * Draft 2020-12.
     */
    public static final String V202012 = "https://json-schema.org/draft/2020-12/schema";

    /**
     * OpenAPI 3.0.
     */
    public static final String OPENAPI_3_0 = "https://spec.openapis.org/oas/3.0/dialect";

    /**
     * OpenAPI 3.1
     */
    public static final String OPENAPI_3_1 = "https://spec.openapis.org/oas/3.1/dialect/base";
}
