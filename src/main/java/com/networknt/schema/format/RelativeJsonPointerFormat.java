/*
 * Copyright (c) 2026 the original author or authors.
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
package com.networknt.schema.format;

import java.util.regex.Pattern;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SpecificationVersion;

/**
 * Format for relative-json-pointer.
 * <p>
 * A non-negative integer, followed by {@code #} or a JSON Pointer. Draft 2020-12 references
 * draft-bhutton-relative-json-pointer-00, which also allows an index manipulation after the integer,
 * {@code ("+" / "-") positive-integer}, as in {@code 0+1/foo} or {@code 1-1#}. The earlier drafts reference
 * draft-handrews-relative-json-pointer, which has no index manipulation, so it is accepted only when the dialect's
 * specification version is 2020-12 or later.
 */
public class RelativeJsonPointerFormat implements Format {
    private static final String NON_NEGATIVE_INTEGER = "(?:0|[1-9]\\d*+)";
    private static final String INDEX_MANIPULATION = "(?:[+-][1-9]\\d*+)?";
    private static final String END = "(?:#|" + Formats.JSON_POINTER + ")";

    private static final Pattern PATTERN = Pattern.compile("^" + NON_NEGATIVE_INTEGER + END + "$");
    private static final Pattern PATTERN_INDEX_MANIPULATION = Pattern
            .compile("^" + NON_NEGATIVE_INTEGER + INDEX_MANIPULATION + END + "$");

    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        return PATTERN.matcher(value).matches();
    }

    @Override
    public boolean matches(ExecutionContext executionContext, SchemaContext schemaContext, String value) {
        SpecificationVersion specification = schemaContext != null && schemaContext.getDialect() != null
                ? schemaContext.getDialect().getSpecificationVersion()
                : null;
        if (specification != null && specification.getOrder() >= SpecificationVersion.DRAFT_2020_12.getOrder()) {
            return PATTERN_INDEX_MANIPULATION.matcher(value).matches();
        }
        return matches(executionContext, value);
    }

    @Override
    public String getName() {
        return "relative-json-pointer";
    }

    @Override
    public String getMessageKey() {
        return "format.relative-json-pointer";
    }
}
