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
import com.networknt.schema.walk.DefaultPropertyWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PropertiesValidator extends BaseJsonValidator implements JsonValidator {
    public static final String PROPERTY = "properties";
    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidator.class);
    private Map<String, JsonSchema> schemas;
    private WalkListenerRunner propertyWalkListenerRunner;
    private ValidationContext validationContext;

    public PropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTIES, validationContext);
        this.validationContext = validationContext;
        schemas = new HashMap<String, JsonSchema>();
        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            schemas.put(pname, new JsonSchema(validationContext, schemaPath + "/" + pname, parentSchema.getCurrentUri(), schemaNode.get(pname), parentSchema)
                .initialize());
        }
		propertyWalkListenerRunner = new DefaultPropertyWalkListenerRunner(
				config.getPropertyWalkListeners());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        // get the Validator state object storing validation data
        ValidatorState state = validatorState.get();
        if (state == null) {
            // if one has not been created, instantiate one
            state = new ValidatorState();
            validatorState.set(state);
        }

        for (Map.Entry<String, JsonSchema> entry : schemas.entrySet()) {
            JsonSchema propertySchema = entry.getValue();
            JsonNode propertyNode = node.get(entry.getKey());

            if (propertyNode != null) {
                // check whether this is a complex validator. save the state
                boolean isComplex = state.isComplexValidator();
                // if this is a complex validator, the node has matched, and all it's child elements, if available, are to be validated
                if (state.isComplexValidator()) {
                    state.setMatchedNode(true);
                }
                // reset the complex validator for child element validation, and reset it after the return from the recursive call
                state.setComplexValidator(false);

                //validate the child element(s)
                errors.addAll(propertySchema.validate(propertyNode, rootNode, at + "." + entry.getKey()));

                // reset the complex flag to the original value before the recursive call
                state.setComplexValidator(isComplex);
                // if this was a complex validator, the node has matched and has been validated
                if (state.isComplexValidator()) {
                    state.setMatchedNode(true);
                }
            } else {
                // check whether the node which has not matched was mandatory or not
                if (getParentSchema().hasRequiredValidator()) {
                    Set<ValidationMessage> requiredErrors = getParentSchema().getRequiredValidator().validate(node, rootNode, at);

                    if (!requiredErrors.isEmpty()) {
                        // the node was mandatory, decide which behavior to employ when validator has not matched
                        if (state.isComplexValidator()) {
                            // this was a complex validator (ex oneOf) and the node has not been matched
                            state.setMatchedNode(false);
                            return Collections.unmodifiableSet(new LinkedHashSet<ValidationMessage>());
                        } else {
                            errors.addAll(requiredErrors);
                        }
                    }
                }
            }
        }

        return Collections.unmodifiableSet(errors);
    }
    
	@Override
	public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
		HashSet<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
		if (shouldValidateSchema) {
			validationMessages.addAll(validate(node, rootNode, at));
		} else {
			boolean executeWalk = true;
			for (Map.Entry<String, JsonSchema> entry : schemas.entrySet()) {
				JsonSchema propertySchema = entry.getValue();
				JsonNode propertyNode = (node == null ? null : node.get(entry.getKey()));
				executeWalk = propertyWalkListenerRunner.runPreWalkListeners(ValidatorTypeCode.PROPERTIES.getValue(),
						propertyNode, rootNode, at + "." + entry.getKey(), propertySchema.getSchemaPath(),
						propertySchema.getSchemaNode(), propertySchema.getParentSchema(),
						validationContext.getJsonSchemaFactory());
				if (executeWalk) {
					validationMessages.addAll(propertySchema.walk(propertyNode, rootNode, at + "." + entry.getKey(),
							shouldValidateSchema));
				}
				propertyWalkListenerRunner.runPostWalkListeners(ValidatorTypeCode.PROPERTIES.getValue(), propertyNode,
						rootNode, at + "." + entry.getKey(), propertySchema.getSchemaPath(),
						propertySchema.getSchemaNode(), propertySchema.getParentSchema(),
						validationContext.getJsonSchemaFactory(), validationMessages);
			}
		}
		return validationMessages;
	}

	public Map<String, JsonSchema> getSchemas() {
		return schemas;
	}


}
