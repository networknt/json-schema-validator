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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class OneOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private List<ShortcutValidator> schemas = new ArrayList<ShortcutValidator>();

    private static class ShortcutValidator {
        private final JsonSchema schema;
        private final Map<String, String> constants;

        ShortcutValidator(JsonNode schemaNode, JsonSchema parentSchema,
                ValidationContext validationContext, JsonSchema schema) {
            JsonNode refNode = schemaNode.get(ValidatorTypeCode.REF.getValue());
            JsonSchema resolvedRefSchema = refNode != null && refNode.isTextual() ? RefValidator.getRefSchema(parentSchema, validationContext,refNode.textValue()) : null;
            this.constants = extractConstants(schemaNode, resolvedRefSchema);
            this.schema = schema;
        }

        private Map<String, String> extractConstants(JsonNode schemaNode, JsonSchema resolvedRefSchema) {
            Map<String, String> refMap = resolvedRefSchema != null ?  extractConstants(resolvedRefSchema.getSchemaNode()) : Collections.<String,String>emptyMap();
            Map<String, String> schemaMap = extractConstants(schemaNode);
            if (refMap.isEmpty() ) {
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
            
            JsonNode propertiesNode  = schemaNode.get("properties");
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
            if (enumNode == null || !enumNode.isArray() ) {
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
            for (Map.Entry<String, String> e: constants.entrySet()) {
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

    }

    public OneOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF, validationContext);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            JsonNode childNode = schemaNode.get(i);
            JsonSchema childSchema = new JsonSchema(validationContext, getValidatorType().getValue(), childNode, parentSchema);
            schemas.add(new ShortcutValidator(childNode, parentSchema, validationContext, childSchema));
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        
        // this validator considers a missing node as an error
        // set it here to true, however re-set it to its original value upon finishing the validation
        boolean missingNodeAsError = config.isMissingNodeAsError();
        config.setMissingNodeAsError(true);
        
        int numberOfValidSchema = 0;
        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        
        for (ShortcutValidator validator : schemas) {
            if (!validator.allConstantsMatch(node)) {
                // take a shortcut: if there is any constant that does not match,
                // we can bail out
                continue;
            }
            JsonSchema schema = validator.schema;
        	    Set<ValidationMessage> schemaErrors = schema.validate(node, rootNode, at);
            if (schemaErrors.isEmpty()) {
                numberOfValidSchema++;
                errors = new LinkedHashSet<ValidationMessage>();
            }
            if(numberOfValidSchema == 0){
            	// one of has matched one of the elements
            	// if it has an error here, it is due to an element validation error
            	// within its child elements
            	if(config.hasElementValidationError()) {
            		errors.clear();
            		errors.addAll(schemaErrors);
            		break;
            	} else {
            		errors.addAll(schemaErrors);
            	}
        	}
        }
        
        if (numberOfValidSchema == 0) {
            for (Iterator<ValidationMessage> it = errors.iterator(); it.hasNext();) {
                ValidationMessage msg = it.next();
                
                if (ValidatorTypeCode.ADDITIONAL_PROPERTIES.getValue().equals(msg.getType())) {
                    it.remove();
                }
            }
            if (errors.isEmpty()) {
                // ensure there is always an error reported if number of valid schemas is 0
                errors.add(buildValidationMessage(at, ""));
            }
        }
        if (numberOfValidSchema > 1) {
            errors = Collections.singleton(buildValidationMessage(at, ""));
        }
        
        // reset the flag for error handling
        config.setMissingNodeAsError(missingNodeAsError);
        
        return Collections.unmodifiableSet(errors);
    }

}
