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

public class MinItemsValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MinItemsValidator.class);

    private int min = 0;

    public MinItemsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.MIN_ITEMS, validationContext);
        if (schemaNode.canConvertToExactIntegral()) {
            min = schemaNode.intValue();
        }
        this.validationContext = validationContext;
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        if (node.isArray()) {
            if (node.size() < min) {
                return Collections.singleton(message().instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale()).arguments(min, node.size()).build());
            }
        } else if (this.validationContext.getConfig().isTypeLoose()) {
            if (1 < min) {
                return Collections.singleton(message().instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale()).arguments(min, 1).build());
            }
        }

        return Collections.emptySet();
    }

}
