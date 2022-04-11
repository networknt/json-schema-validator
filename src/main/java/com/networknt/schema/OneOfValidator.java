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
import java.util.stream.Collectors;

public class OneOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(OneOfValidator.class);

    private final List<ShortcutValidator> schemas = new ArrayList<ShortcutValidator>();

    private static class ShortcutValidator {
        private final JsonSchema schema;
        private final Map<String, String> constants;

        ShortcutValidator(JsonNode schemaNode, JsonSchema parentSchema,
                          ValidationContext validationContext, JsonSchema schema) {
            JsonNode refNode = schemaNode.get(ValidatorTypeCode.REF.getValue());
            JsonSchema resolvedRefSchema = refNode != null && refNode.isTextual() ? RefValidator.getRefSchema(parentSchema, validationContext, refNode.textValue()).getSchema() : null;
            this.constants = extractConstants(schemaNode, resolvedRefSchema);
            this.schema = schema;
        }

        private Map<String, String> extractConstants(JsonNode schemaNode, JsonSchema resolvedRefSchema) {
            Map<String, String> refMap = resolvedRefSchema != null ? extractConstants(resolvedRefSchema.getSchemaNode()) : Collections.<String, String>emptyMap();
            Map<String, String> schemaMap = extractConstants(schemaNode);
            if (refMap.isEmpty()) {
                return schemaMap;
            }
            if (schemaMap.isEmpty()) {
                return refMap;
            }
            Map<String, String> joined = new HashMap<String, String>();
            joined.putAll(schemaMap);
            joined.putAll(refMap);
            return joined;
        }

        private Map<String, String> extractConstants(JsonNode schemaNode) {
            Map<String, String> result = new HashMap<String, String>();
            if (!schemaNode.isObject()) {
                return result;
            }

            JsonNode propertiesNode = schemaNode.get("properties");
            if (propertiesNode == null || !propertiesNode.isObject()) {
                return result;
            }
            Iterator<String> fit = propertiesNode.fieldNames();
            while (fit.hasNext()) {
                String fieldName = fit.next();
                JsonNode jsonNode = propertiesNode.get(fieldName);
                String constantFieldValue = getConstantFieldValue(jsonNode);
                if (constantFieldValue != null && !constantFieldValue.isEmpty()) {
                    result.put(fieldName, constantFieldValue);
                }
            }
            return result;
        }

        private String getConstantFieldValue(JsonNode jsonNode) {
            if (jsonNode == null || !jsonNode.isObject() || !jsonNode.has("enum")) {
                return null;
            }
            JsonNode enumNode = jsonNode.get("enum");
            if (enumNode == null || !enumNode.isArray()) {
                return null;
            }
            if (enumNode.size() != 1) {
                return null;
            }
            JsonNode valueNode = enumNode.get(0);
            if (valueNode == null || !valueNode.isTextual()) {
                return null;
            }
            return valueNode.textValue();
        }

        public boolean allConstantsMatch(JsonNode node) {
            for (Map.Entry<String, String> e : constants.entrySet()) {
                JsonNode valueNode = node.get(e.getKey());
                if (valueNode != null && valueNode.isTextual()) {
                    boolean match = e.getValue().equals(valueNode.textValue());
                    if (!match) {
                        return false;
                    }
                }
            }
            return true;
        }

        private JsonSchema getSchema() {
            return schema;
        }

    }

    public OneOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF, validationContext);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            JsonNode childNode = schemaNode.get(i);
            JsonSchema childSchema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), childNode, parentSchema);
            schemas.add(new ShortcutValidator(childNode, parentSchema, validationContext, childSchema));
        }
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        // As oneOf might contain multiple schemas take a backup of evaluatedProperties.
        Object backupEvaluatedProperties = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);

        // Make the evaluatedProperties list empty.
        CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, new ArrayList<>());

        try {
            debug(logger, node, rootNode, at);

            ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);

            // this is a complex validator, we set the flag to true
            state.setComplexValidator(true);

            int numberOfValidSchema = 0;
            Set<ValidationMessage> childErrors = new LinkedHashSet<ValidationMessage>();
            // validate that only a single element has been received in the oneOf node
            // validation should not continue, as it contradicts the oneOf requirement of only one
//        if(node.isObject() && node.size()>1) {
//        	errors = Collections.singleton(buildValidationMessage(at, ""));
//        	return Collections.unmodifiableSet(errors);
//        }

            for (ShortcutValidator validator : schemas) {
                Set<ValidationMessage> schemaErrors = null;
                // Reset state in case the previous validator did not match
                state.setMatchedNode(true);

                //This prevents from collecting all the  error messages in proper format.
           /* if (!validator.allConstantsMatch(node)) {
                // take a shortcut: if there is any constant that does not match,
                // we can bail out of the validation
                continue;
            }*/

                // get the current validator
                JsonSchema schema = validator.schema;
                if (!state.isWalkEnabled()) {
                    schemaErrors = schema.validate(node, rootNode, at);
                } else {
                    schemaErrors = schema.walk(node, rootNode, at, state.isValidationEnabled());
                }

                // check if any validation errors have occurred
                if (schemaErrors.isEmpty()) {
                    // check whether there are no errors HOWEVER we have validated the exact validator
                    if (!state.hasMatchedNode())
                        continue;

                    numberOfValidSchema++;
                }

                // If the number of valid schema is greater than one, just reset the evaluated properties and break.
                if (numberOfValidSchema > 1) {
                    CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, new ArrayList<>());
                    break;
                }

                childErrors.addAll(schemaErrors);
            }
            Set<ValidationMessage> childNotRequiredErrors = childErrors.stream().filter(error -> !ValidatorTypeCode.REQUIRED.getValue().equals(error.getType())).collect(Collectors.toSet());

            // ensure there is always an "OneOf" error reported if number of valid schemas is not equal to 1.
            if (numberOfValidSchema > 1) {
                final ValidationMessage message = getMultiSchemasValidErrorMsg(at);
                if (failFast) {
                    throw new JsonSchemaException(message);
                }
                errors.add(message);
            }

            // ensure there is always an "OneOf" error reported if number of valid schemas is not equal to 1.
            else if (numberOfValidSchema < 1) {
                if (!childNotRequiredErrors.isEmpty()) {
                    childErrors = childNotRequiredErrors;
                }
                if (!childErrors.isEmpty()) {
                    if (childErrors.size() > 1) {
                        Set<ValidationMessage> notAdditionalPropertiesOnly = new LinkedHashSet<>(childErrors.stream()
                                .filter((ValidationMessage validationMessage) -> !ValidatorTypeCode.ADDITIONAL_PROPERTIES.getValue().equals(validationMessage.getType()))
                                .sorted((vm1, vm2) -> compareValidationMessages(vm1, vm2))
                                .collect(Collectors.toList()));
                        if (notAdditionalPropertiesOnly.size() > 0) {
                            childErrors = notAdditionalPropertiesOnly;
                        }
                    }
                    errors.addAll(childErrors);
                }
                if (failFast) {
                    throw new JsonSchemaException(errors.toString());
                }
            }

            // Make sure to signal parent handlers we matched
            if (errors.isEmpty())
                state.setMatchedNode(true);

            // reset the ValidatorState object in the ThreadLocal
            resetValidatorState();

            return Collections.unmodifiableSet(errors);
        } finally {
            if (errors.isEmpty()) {
                List<String> backupEvaluatedPropertiesList = (backupEvaluatedProperties == null ? new ArrayList<>() : (List<String>) backupEvaluatedProperties);
                backupEvaluatedPropertiesList.addAll((List<String>) CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES));
                CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedPropertiesList);
            } else {
                CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedProperties);
            }
        }
    }

    /**
     * Sort <code>ValidationMessage</code> by its type
     * @return
     */
    private static int compareValidationMessages(ValidationMessage vm1, ValidationMessage vm2) {
        // ValidationMessage's type has smaller index in the list below has high priority
        final List<String> typeCodes = Arrays.asList(
                ValidatorTypeCode.TYPE.getValue(),
                ValidatorTypeCode.DATETIME.getValue(),
                ValidatorTypeCode.UUID.getValue(),
                ValidatorTypeCode.ID.getValue(),
                ValidatorTypeCode.EXCLUSIVE_MAXIMUM.getValue(),
                ValidatorTypeCode.EXCLUSIVE_MINIMUM.getValue(),
                ValidatorTypeCode.TRUE.getValue(),
                ValidatorTypeCode.FALSE.getValue(),
                ValidatorTypeCode.CONST.getValue(),
                ValidatorTypeCode.CONTAINS.getValue(),
                ValidatorTypeCode.PROPERTYNAMES.getValue()
        );

        final int index1 = typeCodes.indexOf(vm1.getType());
        final int index2 = typeCodes.indexOf(vm2.getType());

        if (index1 >= 0) {
            if (index2 >= 0) {
                return Integer.compare(index1, index2);
            } else {
                return -1;
            }
        } else {
            if (index2 >= 0) {
                return 1;
            } else {
                return vm1.getCode().compareTo(vm2.getCode());
            }
        }
    }

    private void resetValidatorState() {
        ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);
        state.setComplexValidator(false);
        state.setMatchedNode(true);
    }

    public List<JsonSchema> getChildSchemas() {
        List<JsonSchema> childJsonSchemas = new ArrayList<JsonSchema>();
        for (ShortcutValidator shortcutValidator: schemas ) {
            childJsonSchemas.add(shortcutValidator.getSchema());
        }
        return childJsonSchemas;
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        HashSet<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
        if (shouldValidateSchema) {
            validationMessages.addAll(validate(node, rootNode, at));
        } else {
            for (ShortcutValidator validator : schemas) {
                validator.schema.walk(node, rootNode, at , shouldValidateSchema);
            }
        }
        return validationMessages;
    }

    private ValidationMessage getMultiSchemasValidErrorMsg(String at){
        String msg="";
        for(ShortcutValidator schema: schemas){
            String schemaValue = schema.getSchema().getSchemaNode().toString();
            msg = msg.concat(schemaValue);
        }

        return ValidationMessage.of(getValidatorType().getValue(), ValidatorTypeCode.ONE_OF, at, schemaPath, msg);
    }

    @Override
    public void preloadJsonSchema() {
        for (final ShortcutValidator scValidator: schemas) {
            scValidator.getSchema().initializeValidators();
        }
    }
}
