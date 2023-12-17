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

package com.networknt.schema.format;

import com.ethlo.time.ITU;
import com.ethlo.time.LeapSecondException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.BaseJsonValidator;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonType;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class DateTimeValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeValidator.class);
    private static final String DATETIME = "date-time";

    public DateTimeValidator(JsonNodePath schemaPath, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, ValidatorTypeCode type) {
        super(schemaPath, evaluationPath, schemaNode, parentSchema, type, validationContext);

        this.validationContext = validationContext;
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }
        if (!isLegalDateTime(node.textValue())) {
            return Collections.singleton(buildValidationMessage(null, at, executionContext.getExecutionConfig().getLocale(),
                    node.textValue(), DATETIME));
        }
        return Collections.emptySet();
    }

    private static boolean isLegalDateTime(String string) {
        try {
            try {
                ITU.parseDateTime(string);
            } catch (LeapSecondException ex) {
                if (!ex.isVerifiedValidLeapYearMonth()) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            logger.debug("Invalid {}: {}", DATETIME, ex.getMessage());
            return false;
        }
    }
}
