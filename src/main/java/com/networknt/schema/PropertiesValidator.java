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

public class PropertiesValidator extends BaseJsonValidator implements JsonValidator {
    public static final String PROPERTY = "properties";
    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidator.class);
    private Map<String, JsonSchema> schemas;

    public PropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTIES, validationContext);
        schemas = new HashMap<String, JsonSchema>();
        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            schemas.put(pname, new JsonSchema(validationContext, schemaPath + "/" + pname, schemaNode.get(pname), parentSchema));
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        for (Map.Entry<String, JsonSchema> entry : schemas.entrySet()) {
            JsonSchema propertySchema = entry.getValue();
            JsonNode propertyNode = node.get(entry.getKey());

            if (propertyNode != null) {
                errors.addAll(propertySchema.validate(propertyNode, rootNode, at + "." + entry.getKey())); 
                
                // this was a regular validation error; mark it as such
                if(!errors.isEmpty()) {
                	config.setElementValidationError(true);
                }
            } else {
            	// if a node could not be found, treat is as error/continue, depending on the SchemaValidatorsConfig
            	if(config.isMissingNodeAsError()) {
            		if(getParentSchema().hasRequiredValidator())
                		errors.addAll(getParentSchema().getRequiredValidator().validate(node,  rootNode, at));     
            		else 
                		errors.add(buildValidationMessage(at, node.toString()));
            	}
            }
        }

        return Collections.unmodifiableSet(errors);
    }

}
