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
    private static final String REMARK = "Remaining validation messages report why candidate schemas didn't match";
    private static final String DISCRIMINATOR_REMARK = "and the discriminator-selected candidate schema didn't pass validation";

    private final List<JsonSchema> schemas = new ArrayList<JsonSchema>();
    private final ValidationContext validationContext;
    private final ValidationContext.DiscriminatorContext discriminatorContext;

    public AnyOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ANY_OF, validationContext);
        this.validationContext = validationContext;
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchema(validationContext,
                                       getValidatorType().getValue(),
                                       parentSchema.getCurrentUri(),
                                       schemaNode.get(i),
                                       parentSchema));
        }

        if (config.isOpenAPI3StyleDiscriminators()) {
            this.discriminatorContext = new ValidationContext.DiscriminatorContext();
        } else {
            this.discriminatorContext = null;
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (config.isOpenAPI3StyleDiscriminators()) {
            validationContext.enterDiscriminatorContext(this.discriminatorContext, at);
        }

        Set<ValidationMessage> allErrors = new LinkedHashSet<ValidationMessage>();
        String typeValidatorName = "anyOf/type";

        try {
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
                if (errors.isEmpty() && !config.isOpenAPI3StyleDiscriminators()) {
                    return errors;
                } else if (config.isOpenAPI3StyleDiscriminators()) {
                    if (discriminatorContext.isDiscriminatorMatchFound()) {
                        if (!errors.isEmpty()) {
                            errors.add(buildValidationMessage(at, DISCRIMINATOR_REMARK));
                        }
                        return errors;
                    }
                }
                allErrors.addAll(errors);
//                allErrors.add(renderMissingMatchValidationMessage(at));
            }
        } finally {
            if (config.isOpenAPI3StyleDiscriminators()) {
                validationContext.leaveDiscriminatorContextImmediately(at);
            }
        }
        return Collections.unmodifiableSet(allErrors);
    }

    private ValidationMessage renderMissingMatchValidationMessage(final String at) {
        if (config.isOpenAPI3StyleDiscriminators()) {
            return super.buildValidationMessage(at, "and no match could be found (respecting discriminators). " + REMARK);
        }
        return super.buildValidationMessage(at, "and no match could be found. " + REMARK);
    }
}
