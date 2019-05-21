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
import java.util.regex.PatternSyntaxException;

public class DateTimeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeValidator.class);

    private final String DATE = "date";
    private final String DATE_TIME = "date-time";

    private final String formatName;
    private final Format format;

    public DateTimeValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, String formatName, Format format) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.DATETIME, validationContext);
        this.formatName = formatName;
        this.format = format;
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != JsonType.STRING) {
            return errors;
        }

        if (formatName != null) {
            if (formatName.equals(DATE) && !isLegalDate(node.textValue()) || (formatName.equals(DATE_TIME) && !isLegalDateTime(node.textValue()))) {
                errors.add(buildValidationMessage(at, node.textValue(), formatName));
            }
        }

        return Collections.unmodifiableSet(errors);
    }

    private boolean isLegalDate(String string) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        return sdf.parse(string, new ParsePosition(0)) != null;
    }

    private boolean isLegalTime(String string) {
        String time = string.split("\\.")[0];
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        sdf.setLenient(false);
        return sdf.parse(time, new ParsePosition(0)) != null;
    }

    private boolean isLegalDateTime(String string) {
        // Check the format
        try {
            if (!format.matches(string)) {
                return false;
            }
        } catch (PatternSyntaxException pse) {
            // String is considered valid if pattern is invalid
            logger.error("Failed to apply pattern: Invalid RE syntax [" + format.getName() + "]", pse);
        }
        // Check the contents
        String[] dateTime = string.split("\\s|T|t", 2);
        String date = dateTime[0];
        String time = dateTime[1];
        return isLegalDate(date) && isLegalTime(time);
    }
}
