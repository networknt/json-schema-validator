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

import java.util.*;

public class IfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(IfValidator.class);

    private static final ArrayList<String> KEYWORDS = new ArrayList<String>(Arrays.asList("if", "then", "else"));

    private JsonSchema ifSchema;
    private JsonSchema thenSchema;
    private JsonSchema elseSchema;

    public IfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.IF_THEN_ELSE, validationContext);

        for (final String keyword : KEYWORDS) {
            final JsonNode node = schemaNode.get(keyword);
            if (keyword.equals("if")) {
                ifSchema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), node, parentSchema)
                    .initialize();
            } else if (keyword.equals("then") && node != null) {
                thenSchema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), node, parentSchema)
                    .initialize();
            } else if (keyword.equals("else") && node != null) {
                elseSchema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), node, parentSchema)
                    .initialize();
            }
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        Set<ValidationMessage> ifErrors = ifSchema.validate(node, rootNode, at);
        if (ifErrors.isEmpty() && thenSchema != null) {
            errors.addAll(thenSchema.validate(node, rootNode, at));
        } else if (!ifErrors.isEmpty() && elseSchema != null) {
            errors.addAll(elseSchema.validate(node, rootNode, at));
        }

        return Collections.unmodifiableSet(errors);
    }

}
