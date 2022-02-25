/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeValidator.class);

    private final ValidationContext validationContext;

    private final String formatName;
    private final String DATE = "date";
    private final String DATETIME = "date-time";

    private static final Pattern RFC3339_PATTERN = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2})" // yyyy-MM-dd
                    + "([Tt](\\d{2}):(\\d{2}):(\\d{2})(\\.\\d+)?" // 'T'HH:mm:ss.milliseconds
                    + "(([Zz])|([+-])(\\d{2}):(\\d{2})))?");

    public DateTimeValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, String formatName) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.DATETIME, validationContext);
        this.formatName = formatName;
        this.validationContext = validationContext;
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return errors;
        }
        if (!isLegalDateTime(node.textValue())) {
            errors.add(buildValidationMessage(at, node.textValue(), formatName));
        }
        return Collections.unmodifiableSet(errors);
    }

    private boolean isLegalDateTime(String string) {
        Matcher matcher = RFC3339_PATTERN.matcher(string);
        StringBuilder pattern = new StringBuilder();
        StringBuilder dateTime = new StringBuilder();
        // Validate the format
        if (!matcher.matches()) {
            logger.error("Failed to apply RFC3339 pattern on " + string);
            return false;
        }
        // Validate the date/time content
        String year = matcher.group(1);
        String month = matcher.group(2);
        String day = matcher.group(3);
        dateTime.append(year).append('-').append(month).append('-').append(day);
        pattern.append("yyyy-MM-dd");

        boolean isTimeGiven = matcher.group(4) != null;
        boolean isOffsetZuluTime = matcher.group(10) != null;
        String hour = null;
        String minute = null;
        String second = null;
        String milliseconds = null;
        String timeShiftSign = null;
        String timeShiftHour = null;
        String timeShiftMinute = null;

        if (!isTimeGiven && DATETIME.equals(formatName) || (isTimeGiven && DATE.equals(formatName))) {
            logger.error("The supplied date/time format type does not match the specification, expected: " + formatName);
            return false;
        }

        if (isTimeGiven) {
            hour = matcher.group(5);
            minute = matcher.group(6);
            second = matcher.group(7);
            dateTime.append('T').append(hour).append(':').append(minute).append(':').append(second);
            pattern.append("'T'HH:mm:ss");
            if (matcher.group(8) != null) {
                // Normalize milliseconds to 3-length digit
                milliseconds = matcher.group(8);
                if (milliseconds.length() > 4) {
                    milliseconds = milliseconds.substring(0, 4);
                } else {
                    while (milliseconds.length() < 4) {
                        milliseconds += "0";
                    }
                }
                dateTime.append(milliseconds);
                pattern.append(".SSS");
            }

            if (isOffsetZuluTime) {
                dateTime.append('Z');
                pattern.append("'Z'");
            } else {
                timeShiftSign = matcher.group(11);
                timeShiftHour = matcher.group(12);
                timeShiftMinute = matcher.group(13);
                dateTime.append(timeShiftSign).append(timeShiftHour).append(':').append(timeShiftMinute);
                pattern.append("XXX");
            }
        }
        return validateDateTime(dateTime.toString(), pattern.toString());
    }

    private boolean validateDateTime(String dateTime, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.parse(dateTime, new ParsePosition(0)) != null;
    }
}
