/*
 * Copyright (c) 2020 Network New Technologies Inc.
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
import java.util.regex.Pattern;

public class PropertyNamesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(PropertyNamesValidator.class);
    private Map<String, JsonSchema> schemas;
    private boolean schemaValue = false;
    public PropertyNamesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTYNAMES, validationContext);
        if(schemaNode.isBoolean()) {
            schemaValue = schemaNode.booleanValue();
        } else {
            schemas = new HashMap<String, JsonSchema>();
            for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
                String pname = it.next();
                schemas.put(pname, new JsonSchema(validationContext, schemaPath + "/" + pname, parentSchema.getCurrentUri(), schemaNode.get(pname), parentSchema)
                    .initialize());
            }
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        if(schemas != null) {
            for (Map.Entry<String, JsonSchema> entry : schemas.entrySet()) {
                JsonNode propertyNode = node.get(entry.getKey());
                // check propertyNames
                if (!node.isObject()) {
                    continue;
                }
                for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                    String pname = it.next();
                    int maxLength = entry.getValue().getSchemaNode().intValue();
                    if("maxLength".equals(entry.getKey()) && pname.length() > maxLength) {
                        errors.add(buildValidationMessage(at + "." + pname, "maxLength " + maxLength));
                    }
                    int minLength = entry.getValue().getSchemaNode().intValue();
                    if("minLength".equals(entry.getKey()) && pname.length() < minLength) {
                        errors.add(buildValidationMessage(at + "." + pname, "minLength " + minLength));
                    }
                    String pattern = entry.getValue().getSchemaNode().textValue();
                    if("pattern".equals(entry.getKey()) && !Pattern.matches(pattern,pname)) {
                        errors.add(buildValidationMessage(at + "." + pname, "pattern " + pattern));
                    }
                }
            }
        } else {
            if(!schemaValue && node.isObject() && node.size() != 0) {
                errors.add(buildValidationMessage(at + "." + node, "false"));
            }
        }
        return Collections.unmodifiableSet(errors);
    }
}
