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
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.walk.DefaultPropertyWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PropertiesValidator extends BaseJsonValidator {
    public static final String PROPERTY = "properties";
    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidator.class);
    private final Map<String, JsonSchema> schemas = new LinkedHashMap<>();

    public PropertiesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTIES, validationContext);
        this.validationContext = validationContext;
        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            this.schemas.put(pname, validationContext.newSchema(schemaLocation.append(pname),
                    evaluationPath.append(pname), schemaNode.get(pname), parentSchema));
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);
        CollectorContext collectorContext = executionContext.getCollectorContext();

        WalkListenerRunner propertyWalkListenerRunner = new DefaultPropertyWalkListenerRunner(this.validationContext.getConfig().getPropertyWalkListeners());

        Set<ValidationMessage> errors = null;

        // get the Validator state object storing validation data
        ValidatorState state = executionContext.getValidatorState();

        Set<ValidationMessage> requiredErrors = null; 

        for (Map.Entry<String, JsonSchema> entry : this.schemas.entrySet()) {
            JsonSchema propertySchema = entry.getValue();
            JsonNode propertyNode = node.get(entry.getKey());
            if (propertyNode != null) {
                JsonNodePath path = instanceLocation.append(entry.getKey());
                if (executionContext.getExecutionConfig().getAnnotationAllowedPredicate().test(getKeyword())) {
                    collectorContext.getEvaluatedProperties().add(path); // TODO: This should happen after validation
                }
                // check whether this is a complex validator. save the state
                boolean isComplex = state.isComplexValidator();
               // if this is a complex validator, the node has matched, and all it's child elements, if available, are to be validated
                if (state.isComplexValidator()) {
                    state.setMatchedNode(true);
                }
                 // reset the complex validator for child element validation, and reset it after the return from the recursive call
                state.setComplexValidator(false);
                
                if (!state.isWalkEnabled()) {
                    //validate the child element(s)
                    Set<ValidationMessage> result = propertySchema.validate(executionContext, propertyNode, rootNode, path);
                    if (!result.isEmpty()) {
                        if (errors == null) {
                            errors = new LinkedHashSet<>();
                        }
                        errors.addAll(result);
                    }
                } else {
                    // check if walker is enabled. If it is enabled it is upto the walker implementation to decide about the validation.
                    if (errors == null) {
                        errors = new LinkedHashSet<>();
                    }
                    walkSchema(executionContext, entry, node, rootNode, instanceLocation, state.isValidationEnabled(), errors, propertyWalkListenerRunner);
                }

                // reset the complex flag to the original value before the recursive call
                state.setComplexValidator(isComplex);
                // if this was a complex validator, the node has matched and has been validated
                if (state.isComplexValidator()) {
                    state.setMatchedNode(true);
                }
            } else {
                // check whether the node which has not matched was mandatory or not
                if (getParentSchema().hasRequiredValidator()) {

                    // The required validator runs for all properties in the node and not just the
                    // current propertyNode
                    if (requiredErrors == null) {
                        requiredErrors = getParentSchema().getRequiredValidator().validate(executionContext, node, rootNode, instanceLocation);

                        if (!requiredErrors.isEmpty()) {
                             // the node was mandatory, decide which behavior to employ when validator has not matched
                            if (state.isComplexValidator()) {
                                // this was a complex validator (ex oneOf) and the node has not been matched
                                state.setMatchedNode(false);
                                return Collections.emptySet();
                            }
                            if (errors == null) {
                                errors = new LinkedHashSet<>();
                            }
                            errors.addAll(requiredErrors);
                        }
                    }
                }
            }
        }
        return errors == null || errors.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        HashSet<ValidationMessage> validationMessages = new LinkedHashSet<>();
        if (this.applyDefaultsStrategy.shouldApplyPropertyDefaults() && null != node && node.getNodeType() == JsonNodeType.OBJECT) {
            applyPropertyDefaults((ObjectNode) node);
        }
        if (shouldValidateSchema) {
            validationMessages.addAll(validate(executionContext, node, rootNode, instanceLocation));
        } else {
            WalkListenerRunner propertyWalkListenerRunner = new DefaultPropertyWalkListenerRunner(this.validationContext.getConfig().getPropertyWalkListeners());
            for (Map.Entry<String, JsonSchema> entry : this.schemas.entrySet()) {
                walkSchema(executionContext, entry, node, rootNode, instanceLocation, shouldValidateSchema, validationMessages, propertyWalkListenerRunner);
            }
        }
        return validationMessages;
    }

    private void applyPropertyDefaults(ObjectNode node) {
        for (Map.Entry<String, JsonSchema> entry : this.schemas.entrySet()) {
            JsonNode propertyNode = node.get(entry.getKey());

            JsonNode defaultNode = getDefaultNode(entry);
            if (defaultNode == null) {
                continue;
            }
            boolean applyDefault = propertyNode == null
                    || (propertyNode.isNull() && this.applyDefaultsStrategy.shouldApplyPropertyDefaultsIfNull());
            if (applyDefault) {
                node.set(entry.getKey(), defaultNode);
            }
        }
    }

    private static JsonNode getDefaultNode(final Map.Entry<String, JsonSchema> entry) {
        JsonSchema propertySchema = entry.getValue();
        return propertySchema.getSchemaNode().get("default");
    }

    private void walkSchema(ExecutionContext executionContext, Map.Entry<String, JsonSchema> entry, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema,
            Set<ValidationMessage> validationMessages, WalkListenerRunner propertyWalkListenerRunner) {
        JsonSchema propertySchema = entry.getValue();
        JsonNode propertyNode = (node == null ? null : node.get(entry.getKey()));
        JsonNodePath path = instanceLocation.append(entry.getKey());
        boolean executeWalk = propertyWalkListenerRunner.runPreWalkListeners(executionContext,
                ValidatorTypeCode.PROPERTIES.getValue(), propertyNode, rootNode, path,
                propertySchema.getEvaluationPath(), propertySchema.getSchemaLocation(), propertySchema.getSchemaNode(),
                propertySchema.getParentSchema(), this.validationContext, this.validationContext.getJsonSchemaFactory());
        if (executeWalk) {
            validationMessages.addAll(
                    propertySchema.walk(executionContext, propertyNode, rootNode, path, shouldValidateSchema));
        }
        propertyWalkListenerRunner.runPostWalkListeners(executionContext, ValidatorTypeCode.PROPERTIES.getValue(), propertyNode,
                rootNode, path, propertySchema.getEvaluationPath(),
                propertySchema.getSchemaLocation(), propertySchema.getSchemaNode(), propertySchema.getParentSchema(), this.validationContext, this.validationContext.getJsonSchemaFactory(), validationMessages);

    }

    public Map<String, JsonSchema> getSchemas() {
        return this.schemas;
    }


    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas.values());
    }
}
