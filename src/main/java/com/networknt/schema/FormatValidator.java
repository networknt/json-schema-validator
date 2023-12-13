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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

public class FormatValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(FormatValidator.class);

    private final Format format;

    public FormatValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, Format format, ValidatorTypeCode type) {
        super(schemaPath, schemaNode, parentSchema, type, validationContext);
        this.format = format;
        this.validationContext = validationContext;
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }

        Set<ValidationMessage> errors = new LinkedHashSet<>();
        if (format != null) {
            if(format.getName().equals("ipv6")) {
                if(!node.textValue().trim().equals(node.textValue())) {
                    // leading and trailing spaces
                    errors.add(buildValidationMessage(null, at,
                            executionContext.getExecutionConfig().getLocale(), format.getName(), format.getErrorMessageDescription()));
                } else if(node.textValue().contains("%")) {
                    // zone id is not part of the ipv6
                    errors.add(buildValidationMessage(null, at,
                            executionContext.getExecutionConfig().getLocale(), format.getName(), format.getErrorMessageDescription()));
                }
            }
            try {
                if (!format.matches(executionContext, node.textValue())) {
                    errors.add(buildValidationMessage(null, at,
                            executionContext.getExecutionConfig().getLocale(), format.getName(), format.getErrorMessageDescription()));
                }
            } catch (PatternSyntaxException pse) {
                // String is considered valid if pattern is invalid
                logger.error("Failed to apply pattern on {}: Invalid RE syntax [{}]", at, format.getName(), pse);
            }
        }

        return Collections.unmodifiableSet(errors);
    }

}
