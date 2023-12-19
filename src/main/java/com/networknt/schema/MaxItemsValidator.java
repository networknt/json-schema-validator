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
import java.util.Set;

public class MaxItemsValidator extends BaseJsonValidator implements JsonValidator {

    private static final Logger logger = LoggerFactory.getLogger(MaxItemsValidator.class);


    private int max = 0;

    public MaxItemsValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.MAX_ITEMS, validationContext);
        if (schemaNode.canConvertToExactIntegral()) {
            max = schemaNode.intValue();
        }
        this.validationContext = validationContext;
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        if (node.isArray()) {
            if (node.size() > max) {
                return Collections.singleton(message().instanceLocation(instanceLocation).locale(executionContext.getExecutionConfig().getLocale()).arguments(max).build());
            }
        } else if (this.validationContext.getConfig().isTypeLoose()) {
            if (1 > max) {
                return Collections.singleton(message().instanceLocation(instanceLocation).locale(executionContext.getExecutionConfig().getLocale()).arguments(max).build());
            }
        }

        return Collections.emptySet();
    }

}
