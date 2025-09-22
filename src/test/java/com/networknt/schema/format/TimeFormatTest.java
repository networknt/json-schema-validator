/*
 * Copyright (c) 2025 the original author or authors.
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

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.Error;

class TimeFormatTest {

    enum ValidTimeFormatInput {
        Z_OFFSET_LEAP_SECOND("23:59:60Z"),
        POSITIVE_OFFSET_LEAP_SECOND("07:59:60+08:00"),
        NEGATIVE_OFFSET_LEAP_SECOND("15:59:60-08:00"),
        Z_OFFSET_MIN_TIME("00:00:00Z"),
        Z_OFFSET("23:59:59Z"),
        POSITIVE_OFFSET_ZERO("17:00:00+00:00"),
        POSITIVE_OFFSET_MAX("17:00:00+23:59"),
        NEGATIVE_OFFSET_ZERO("17:00:00-00:00"),
        NEGATIVE_OFFSET_ONE_DAY("17:00:00-07:00"),
        NEGATIVE_OFFSET_MAX("17:00:00-23:59");

        String format;

        ValidTimeFormatInput(String format) {
            this.format = format;
        }
    }

    @ParameterizedTest
    @EnumSource(ValidTimeFormatInput.class)
    void validTimeShouldPass(ValidTimeFormatInput input) {
        String schemaData = "{\r\n"
                + "  \"format\": \"time\"\r\n"
                + "}";

        String inputData = "\""+input.format+"\"";

        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build();
        Schema schema = SchemaRegistry.getInstance(Version.DRAFT_2020_12).getSchema(schemaData, config);
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertTrue(messages.isEmpty());
    }

    enum InvalidTimeFormatInput {
        NEGATIVE_OFFSET_INVALID_LEAP_SECOND("23:59:60-07:00"),
        Z_OFFSET_EXCEED_LEAP_SECOND("23:59:61Z"),
        Z_OFFSET_EXCEED_TIME("24:00:00Z"),
        POSITIVE_OFFSET_EXCEED_MAX("17:00:00+24:00"),
        NEGATIVE_OFFSET_EXCEED_MAX("17:00:00-24:00");

        String format;
        
        InvalidTimeFormatInput(String format) {
            this.format = format;
        }
    }

    @ParameterizedTest
    @EnumSource(InvalidTimeFormatInput.class)
    void invalidTimeShouldFail(InvalidTimeFormatInput input) {
        String schemaData = "{\r\n"
                + "  \"format\": \"time\"\r\n"
                + "}";

        String inputData = "\""+input.format+"\"";

        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build();
        Schema schema = SchemaRegistry.getInstance(Version.DRAFT_2020_12).getSchema(schemaData, config);
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
    }
}
