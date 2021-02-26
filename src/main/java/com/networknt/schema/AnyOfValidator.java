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

public class AnyOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private List<JsonSchema> schemas = new ArrayList<JsonSchema>();

    public AnyOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ANY_OF, validationContext);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), schemaNode.get(i), parentSchema));
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> allErrors = new LinkedHashSet<ValidationMessage>();
        String typeValidatorName = "anyOf/type";

        for (JsonSchema schema : schemas) {
            if (schema.getValidators().containsKey(typeValidatorName)) {
                TypeValidator typeValidator = ((TypeValidator) schema.getValidators().get(typeValidatorName));
                //If schema has type validator and node type doesn't match with schemaType then ignore it
                //For union type, it is must to call TypeValidator
                if (typeValidator.getSchemaType() != JsonType.UNION && !typeValidator.equalsToSchemaType(node)) {
                    allErrors.add(buildValidationMessage(at, typeValidator.getSchemaType().toString()));
                    continue;
                }
            }
            Set<ValidationMessage> errors = schema.validate(node, rootNode, at);
            if (errors.isEmpty()) {
                return errors;
            }
            allErrors.addAll(errors);
        }
        return Collections.unmodifiableSet(allErrors);
    }

}
