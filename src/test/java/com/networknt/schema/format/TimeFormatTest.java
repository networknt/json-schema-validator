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
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.Error;

class TimeFormatTest {

    enum ValidTimeFormatInput {
        Z_OFFSET_LEAP_SECOND("23:59:60Z"),
        POSITIVE_OFFSET_LEAP_SECOND("07:59:60+08:00"),
        NEGATIVE_OFFSET_LEAP_SECOND("15:59:60-08:00"),
        POSITIVE_MAX_OFFSET_LEAP_SECOND("23:58:60+23:59"),
        NEGATIVE_MAX_OFFSET_LEAP_SECOND("00:00:60-23:59"),
        Z_OFFSET_MIN_TIME("00:00:00Z"),
        Z_OFFSET("23:59:59Z"),
        Z_OFFSET_ONE_FRACTION_DIGIT("12:00:00.1Z"),
        Z_OFFSET_NINE_FRACTION_DIGITS("12:00:00.123456789Z"),
        Z_OFFSET_TEN_FRACTION_DIGITS("12:00:00.1234567890Z"),
        POSITIVE_OFFSET_TEN_FRACTION_DIGITS("01:27:01.7768079441+12:29"),
        Z_OFFSET_LEAP_SECOND_TEN_FRACTION_DIGITS("23:59:60.1234567890Z"),
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

        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaData);
        List<Error> messages = schema.validate(inputData, InputFormat.JSON,
                executionContext -> executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertTrue(messages.isEmpty());
    }

    enum InvalidTimeFormatInput {
        NEGATIVE_OFFSET_INVALID_LEAP_SECOND("23:59:60-07:00"),
        Z_OFFSET_EXCEED_LEAP_SECOND("23:59:61Z"),
        Z_OFFSET_EXCEED_TIME("24:00:00Z"),
        POSITIVE_OFFSET_EXCEED_MAX("17:00:00+24:00"),
        NEGATIVE_OFFSET_EXCEED_MAX("17:00:00-24:00"),
        Z_OFFSET_EMPTY_FRACTION("12:00:00.Z"),
        LOWERCASE_Z_OFFSET_EMPTY_FRACTION("12:00:00.z"),
        POSITIVE_OFFSET_EMPTY_FRACTION("12:00:00.+01:00"),
        NEGATIVE_OFFSET_EMPTY_FRACTION("12:00:00.-01:00"),
        Z_OFFSET_NON_DIGIT_FRACTION("12:00:00.1234567890xZ"),
        TRAILING_TEXT_AFTER_OFFSET("12:00:00+01:00junk"),
        OFFSET_WITH_SECONDS("12:00:00+01:00:00"),
        LOWERCASE_Z_WITH_TRAILING_TEXT("12:00:00zjunk"),
        TRAILING_NEWLINE_AFTER_OFFSET("12:00:00+01:00\\n"),
        NO_SECONDS("12:00Z"),
        ONE_DIGIT_HOUR("1:00:00Z"),
        ONE_DIGIT_MINUTE("12:0:00Z"),
        ONE_DIGIT_SECOND("12:00:0Z"),
        HOUR_OVERFLOW_LEAP_SECOND("24:59:60+01:00"),
        MINUTE_OVERFLOW_LEAP_SECOND("23:89:60+00:30"),
        HOUR_AND_OFFSET_OVERFLOW_LEAP_SECOND("45:59:60+22:00");

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

        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaData);
        List<Error> messages = schema.validate(inputData, InputFormat.JSON,
                executionContext -> executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertFalse(messages.isEmpty());
    }
}
