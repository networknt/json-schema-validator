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

import com.ethlo.time.ITU;
import com.ethlo.time.LeapSecondException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DateTimeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeValidator.class);

    private final ValidationContext validationContext;

    private final String formatName;
    private final String DATE = "date";
    private final String DATETIME = "date-time";

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
        if(formatName.equals(DATE)) {
            return tryParse(() -> LocalDate.parse(string));
        } else if(formatName.equals(DATETIME)) {
            return tryParse(() -> {
                try {
                    ITU.parseDateTime(string);
                } catch (LeapSecondException ex) {
                    if(!ex.isVerifiedValidLeapYearMonth()) {
                        throw ex;
                    }
                }
            });
        } else {
            throw new IllegalStateException("Unknown format: " + formatName);
        }
    }

    private boolean tryParse(Runnable parser) {
        try {
            parser.run();
            return true;
        } catch (Exception ex) {
            logger.error("Invalid " + formatName + ": " + ex.getMessage());
            return false;
        }
    }
}
