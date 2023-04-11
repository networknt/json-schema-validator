/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema.format;

import com.networknt.schema.AbstractFormat;

import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import static java.time.temporal.ChronoField.*;
import java.time.temporal.TemporalAccessor;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

/**
 * Validates that a value conforms to the time specification in RFC 3339.
 */
public class TimeFormat extends AbstractFormat {
    // In 2023, time-zone offsets around the world extend from -12:00 to +14:00.
    // However, RFC 3339 accepts -23:59 to +23:59.
    private static final long MAX_OFFSET_MIN =  24 * 60 - 1;
    private static final long MIN_OFFSET_MIN = -MAX_OFFSET_MIN;

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(ISO_LOCAL_TIME)
        .appendOffset("+HH:MM", "Z")
        .parseLenient()
        .toFormatter();

    public TimeFormat() {
        super("time", "^(?:(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])(?:\\.\\d+)?(?:Z|[+-](?:(?:0[0-9]|2[0-3]):[0-5][0-9]))$");
    }

    @Override
    public boolean matches(String value) {
        try {
            if (null == value) return true;

            int pos = value.indexOf('Z');
            if (-1 != pos && pos != value.length() - 1) return false;

            TemporalAccessor accessor = formatter.parseUnresolved(value, new ParsePosition(0));
            if (null == accessor) return false;

            long offset = accessor.getLong(OFFSET_SECONDS) / 60;
            if (MAX_OFFSET_MIN < offset || MIN_OFFSET_MIN > offset) return false;

            long hr = accessor.getLong(HOUR_OF_DAY) - offset / 60;
            long min = accessor.getLong(MINUTE_OF_HOUR) - offset % 60;
            long sec = accessor.getLong(SECOND_OF_MINUTE);

            if (min < 0) {
                --hr;
                min += 60;
            }
            if (hr < 0) {
                hr += 24;
            }

            return (sec <= 59 && min <= 59 && hr <= 23)
                || (sec == 60 && min == 59 && hr == 23);

        } catch (DateTimeException e) {
            return false;
        }
    }

}
