/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OneOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private List<JsonSchema> schemas = new ArrayList<JsonSchema>();

    public OneOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF, validationContext);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchema(validationContext, getValidatorType().getValue(), schemaNode.get(i), parentSchema));
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        int numberOfValidSchema = 0;
        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        
        for (JsonSchema schema : schemas) {
        	Set<ValidationMessage> schemaErrors = schema.validate(node, rootNode, at);
            if (schemaErrors.isEmpty()) {
                numberOfValidSchema++;
                errors = new LinkedHashSet<ValidationMessage>();
            }
            if(numberOfValidSchema == 0){
        		errors.addAll(schemaErrors);
        	}
            if (numberOfValidSchema > 1) {
                break;
            }
        }
        
        if (numberOfValidSchema == 0) {
            for (Iterator<ValidationMessage> it = errors.iterator(); it.hasNext();) {
                ValidationMessage msg = it.next();
                
                if (ValidatorTypeCode.ADDITIONAL_PROPERTIES.getValue().equals(msg.getType())) {
                    it.remove();
                }
            }
        }
        if (numberOfValidSchema > 1) {
            errors = Collections.singleton(buildValidationMessage(at, ""));
        }
        
        return Collections.unmodifiableSet(errors);
    }

}
