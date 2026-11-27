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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

/**
 * Tests for the json-pointer and relative-json-pointer formats.
 * <p>
 * RFC 6901 section 3: a reference token may contain any character except '/' and '~', which is escaped as '~0' or
 * '~1'. A '#' is an ordinary character inside a token; only the URI fragment representation (section 6) starts with
 * '#', and that is not a JSON Pointer string.
 */
class JsonPointerFormatTest {

    private static boolean valid(String format, String value) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"type\": \"string\", \"format\": \"" + format + "\"}");
        List<Error> errors = schema.validate("\"" + value + "\"", InputFormat.JSON,
                executionContext -> executionContext.executionConfig(config -> config.formatAssertionsEnabled(true)));
        return errors.isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "/", "/foo", "/a#b", "/#", "/foo#bar/baz", "/~0#~1", "/c%d/e^f/g|h" })
    void jsonPointerAccepts(String value) {
        assertEquals(true, valid("json-pointer", value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = { "#", "#/", "#/a", "a#b", "/a~2#", "/~#" })
    void jsonPointerRejects(String value) {
        assertEquals(false, valid("json-pointer", value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "0#", "1/foo", "0/a#b", "2/#", "10/foo#bar" })
    void relativeJsonPointerAccepts(String value) {
        assertEquals(true, valid("relative-json-pointer", value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = { "#/a", "/a#b", "01/a", "0/a~2#", "0##" })
    void relativeJsonPointerRejects(String value) {
        assertEquals(false, valid("relative-json-pointer", value), value);
    }
}
