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
            "2020-01-01T12:00:00z",
            "2020-01-01T12:00:00+01:00",
            "2016-12-31T23:59:60Z",
            "2017-01-01T00:59:60+01:00"
    })
    void acceptsValidOffsetsAndLeapSecond(String value) {
        assertTrue(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
    }

    /**
     * OffsetDateTime.parse(..) accepts these, Ethlo doesn't. The two paths have to agree.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00Z",
            "2020-01-01T12:00:00+01",
            "2020-01-01T12:00:00.Z"
    })
    void bothPathsRejectValuesThatAreNotRfc3339(String value) {
        // Ethlo is on the test classpath, so the schema takes the Ethlo path
        assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00.+01:00",
            "2020-01-01T12:00:00+01:00:00",
            "2020-01-01 12:00:00Z",
            "2020-01-01T12:00:00",
            "2020-01-01T",
            "2020-01-01",
            "2020-1-01T12:00:00Z",
            "+2020-01-01T12:00:00Z",
            "2020-02-30T12:00:00Z",
            "2020-01-01T24:00:00Z",
            "2020-01-01T12:59:60Z",
            "2020-01-01T12:00:00+24:00",
            "2020-01-01T12:00:00+0100",
            "2020-01-01T12:00:00+01:0",
            "2020-01-01T12:00:00Zjunk"
    })
    void fallbackRejectsValuesThatAreNotRfc3339(String value) {
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    /**
     * Valid RFC 3339 date-times that OffsetDateTime.parse(..) rejects.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00Z",
            "2020-01-01t12:00:00z",
            "2020-01-01T12:00:00.5+01:00",
            "2016-12-31T23:59:60Z",
            "2017-01-01T00:59:60+01:00",
            "2020-01-01T12:00:00.1234567890Z",
            "2020-01-01T12:00:00+23:59",
            "2020-01-01T12:00:00-00:00"
    })
    void fallbackAcceptsRfc3339DateTimes(String value) {
        assertTrue(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }
}
