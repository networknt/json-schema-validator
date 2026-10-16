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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

class DateTimeFormatTest {
    private static final Schema SCHEMA = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
            .getSchema("{\"type\":\"string\",\"format\":\"date-time\"}");

    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00Zjunk",
            "2020-01-01T12:00:00zjunk",
            "2020-01-01T12:00:00ZjunkZ",
            "2020-01-01T12:00:00+01:00junk",
            "2020-01-01T12:00:00+01x00",
            "2020-01-01T12:00:00+0100",
            "2020-01-01T12:00:00+01:0",
            "2016-12-31T23:59:60Zjunk"
    })
    void rejectsTrailingTextAfterOffset(String value) {
        assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00Z",
            "2020-01-01t12:00:00Z",
            "2020-01-01t12:00:00z",
            "2020-01-01T12:00:00.123456789+01:00",
            "2020-01-01t12:00:00-05:30",
            "2016-12-31t23:59:60Z",
            "2020-01-01T12:00:00z",
            "2020-01-01T12:00:00+01:00",
            "2016-12-31T23:59:60Z",
            "2017-01-01T00:59:60+01:00"
    })
    void acceptsValidOffsetsAndLeapSecond(String value) {
        assertTrue(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "2020-01-01",
            "2020-01-01 12:00:00Z",
            "2020-01-01 12:00:00+01:00",
            "2020-01-01 12:00:00.123456789Z",
            "2016-12-31 23:59:60Z",
            "2020-01-01_12:00:00Z",
            "2020-01-01  12:00:00Z"
    })
    void rejectsNonTSeparators(String value) {
        assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    @Test
    void formatAnnotationDoesNotRejectSpaceSeparator() {
        assertTrue(SCHEMA.validate("\"2020-01-01 12:00:00Z\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(false))).isEmpty());
    }
}
