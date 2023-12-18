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
import com.networknt.schema.format.DateTimeValidator;
import com.networknt.schema.format.DurationFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class FormatKeyword implements Keyword {
    private static final String DATE_TIME = "date-time";
    private static final String DURATION = "duration";

    private final ValidatorTypeCode type;
    private final Map<String, Format> formats;

    public FormatKeyword(ValidatorTypeCode type, Map<String, Format> formats) {
        this.type = type;
        this.formats = formats;
    }

    Collection<Format> getFormats() {
        return Collections.unmodifiableCollection(this.formats.values());
    }

    @Override
    public JsonValidator newValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        Format format = null;
        if (schemaNode != null && schemaNode.isTextual()) {
            String formatName = schemaNode.textValue();
            format = this.formats.get(formatName);
            if (format != null) {
                return new FormatValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext, format, type);
            }

            switch (formatName) {
                case DURATION:
                    format = new DurationFormat(validationContext.getConfig().isStrict(DURATION));
                    break;

                case DATE_TIME: {
                    ValidatorTypeCode typeCode = ValidatorTypeCode.DATETIME;
                    // Set custom error message
                    typeCode.setCustomMessage(this.type.getCustomMessage());
                    return new DateTimeValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext, typeCode);
                }
            }
        }

        return new FormatValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext, format, this.type);
    }

    @Override
    public String getValue() {
        return this.type.getValue();
    }

    @Override
    public void setCustomMessage(Map<String, String> message) {
        this.type.setCustomMessage(message);
    }
}
