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

public class ContainsValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ContainsValidator.class);

    private JsonSchema schema;

    public ContainsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.CONTAINS, validationContext);
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            schema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), schemaNode, parentSchema);
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);


        if (!node.isArray()) {
            // ignores non-arrays
            return Collections.emptySet();
        }

        if (node.isEmpty()) {
            // Array was empty
            return buildErrorMessageSet(at);
        } else if (node.isArray()) {
            int i = 0;
            for (JsonNode n : node) {
                if (schema.validate(n, rootNode, at + "[" + i + "]").isEmpty()) {
                    //Short circuit on first success
                    return Collections.emptySet();
                }
                i++;
            }
            // None of the elements in the array satisfies the schema
            return buildErrorMessageSet(at);
        }

        return Collections.emptySet();
    }

    private Set<ValidationMessage> buildErrorMessageSet(String at) {
        return Collections.singleton(buildValidationMessage(at, schema.getSchemaNode().toString()));
    }

}
