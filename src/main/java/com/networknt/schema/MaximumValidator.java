/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MaximumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MaximumValidator.class);
    private static final String PROPERTY_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";

    private double maximum;
    private boolean excludeEqual = false;

    public MaximumValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MAXIMUM);
        if (schemaNode.isNumber()) {
            maximum = schemaNode.doubleValue();
        } else {
            throw new JsonSchemaException("maximum value is not a number");
        }

        JsonNode exclusiveMaximumNode = getParentSchema().getSchemaNode().get(PROPERTY_EXCLUSIVE_MAXIMUM);
        if (exclusiveMaximumNode != null && exclusiveMaximumNode.isBoolean()) {
            excludeEqual = exclusiveMaximumNode.booleanValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (!node.isNumber()) {
            // maximum only applies to numbers
            return errors;
        }

        double value = node.doubleValue();
        if (greaterThan(value, maximum) || (excludeEqual && equals(value, maximum))) {
            errors.add(buildValidationMessage(at, "" + maximum));
        }
        return errors;
    }

}
