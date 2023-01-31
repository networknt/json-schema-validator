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
import com.networknt.schema.format.DurationValidator;
import com.networknt.schema.format.EmailValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class FormatKeyword implements Keyword {
    private static final String DATE = "date";
    private static final String DATE_TIME = "date-time";
    private static final String UUID = "uuid";
    private static final String EMAIL = "email";
    private static final String DURATION = "duration";

    private final ValidatorTypeCode type;
    private final Map<String, Format> formats;

    public FormatKeyword(ValidatorTypeCode type, Map<String, Format> formats) {
        this.type = type;
        this.formats = formats;
    }

    Collection<Format> getFormats() {
        return Collections.unmodifiableCollection(formats.values());
    }

    @Override
    public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
            throws Exception {
        Format format = null;
        if (schemaNode != null && schemaNode.isTextual()) {
            String formatName = schemaNode.textValue();
            format = formats.get(formatName);
            // if you set custom format, override default Email/DateTime/UUID Validator
            if (format != null) {
                return new FormatValidator(schemaPath, schemaNode, parentSchema, validationContext, format, type);
            }
            // Validate date and time separately
            if (formatName.equals(DATE) || formatName.equals(DATE_TIME)) {
                ValidatorTypeCode typeCode = ValidatorTypeCode.DATETIME;
                // Set custom error message
                typeCode.setCustomMessage(type.getCustomMessage());
                return new DateTimeValidator(schemaPath, schemaNode, parentSchema, validationContext, formatName, typeCode);
            } else if (formatName.equals(UUID)) {
                ValidatorTypeCode typeCode = ValidatorTypeCode.UUID;
                // Set custom error message
                typeCode.setCustomMessage(type.getCustomMessage());
                return new UUIDValidator(schemaPath, schemaNode, parentSchema, validationContext, formatName, typeCode);
            } else if (formatName.equals(EMAIL)) {
                return new EmailValidator(schemaPath, schemaNode, parentSchema, validationContext, formatName, type);
            }
            else if (formatName.equals(DURATION)) {
                return new DurationValidator(schemaPath, schemaNode, parentSchema, validationContext, formatName, type);
            }
        }
        return new FormatValidator(schemaPath, schemaNode, parentSchema, validationContext, format, type);
    }

    @Override
    public String getValue() {
        return type.getValue();
    }

    @Override
    public void setCustomMessage(String message) {
        type.setCustomMessage(message);
    }
}
