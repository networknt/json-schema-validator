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

import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;

import com.networknt.schema.ExecutionContext;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.temporal.ChronoField.*;

/**
 * Format for time.
 * <p>
 * Validates that a value conforms to the time specification in RFC 3339.
 */
public class TimeFormat implements Format {
    // In 2023, time-zone offsets around the world extend from -12:00 to +14:00.
    // However, RFC 3339 accepts -23:59 to +23:59.
    private static final long MAX_OFFSET_MIN = 24 * 60 - 1;
    private static final long MIN_OFFSET_MIN = -MAX_OFFSET_MIN;

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_TIME)
            .appendOffset("+HH:MM", "Z")
            .parseLenient()
            .toFormatter();

    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        try {
            if (null == value) return true;

            int pos = value.indexOf('Z');
            if (-1 != pos && pos != value.length() - 1) return false;

            TemporalAccessor accessor = formatter.parseUnresolved(value, new ParsePosition(0));
            if (null == accessor) return false;

            long offset = accessor.getLong(OFFSET_SECONDS) / 60;
            if (MAX_OFFSET_MIN < offset || MIN_OFFSET_MIN > offset) return false;

            long hr = accessor.getLong(HOUR_OF_DAY);
            long min = accessor.getLong(MINUTE_OF_HOUR);
            long sec = accessor.getLong(SECOND_OF_MINUTE);

            boolean isStandardTimeRange = (sec <= 59 && min <= 59 && hr <= 23);
            if (isStandardTimeRange) {
                return true;
            }
            // Leap second check normalize to UTC to check if 23:59:60Z
            hr = hr - offset / 60;
            min = min - offset % 60;

            if (min < 0) {
                --hr;
                min += 60;
            }
            if (hr < 0) {
                hr += 24;
            }
            return isSpecialCaseLeapSecond(sec, min, hr);

        } catch (DateTimeException e) {
            return false;
        }
    }

    /**
     * Determines if it is a valid leap second.
     *
     * See https://datatracker.ietf.org/doc/html/rfc3339#appendix-D
     *
     * @param sec second
     * @param min minute
     * @param hr  hour
     * @return true if it is a valid leap second
     */
    private boolean isSpecialCaseLeapSecond(long sec, long min, long hr) {
        return (sec == 60 && min == 59 && hr == 23);
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public String getMessageKey() {
        return "format.time";
    }
}
