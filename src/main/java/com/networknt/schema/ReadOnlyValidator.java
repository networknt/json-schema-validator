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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ReadOnlyValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ReadOnlyValidator.class);

    private final boolean readOnly;

    public ReadOnlyValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.READ_ONLY, validationContext);

        this.readOnly = validationContext.getConfig().isReadOnly();
        logger.debug("Loaded ReadOnlyValidator for property {} as {}", parentSchema, "read mode");
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        Set<ValidationMessage> errors= new HashSet<>();
        if (this.readOnly) {
        	errors.add(buildValidationMessage(at));
        } 
        return errors;
    }

}