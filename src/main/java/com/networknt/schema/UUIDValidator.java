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

public class UUIDValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UUIDValidator.class);

    private final String formatName;


    private static final String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    public UUIDValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, String formatName) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UUID, validationContext);
        this.formatName = formatName;
        parseErrorCode(getValidatorType().getErrorCodeKey());
        this.validationContext = validationContext;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return errors;
        }
        if (!isUUID(node.textValue())) {
            errors.add(buildValidationMessage(at, node.textValue(), formatName));
        }
        return Collections.unmodifiableSet(errors);
    }

    public boolean isUUID(String s) {
        return s.matches(regex);
    }
}
