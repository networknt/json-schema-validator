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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class AdditionalPropertiesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalPropertiesValidator.class);

    private final boolean allowAdditionalProperties;
    private final JsonSchema additionalPropertiesSchema;
    private final Set<String> allowedProperties;
    private final List<Pattern> patternProperties = new ArrayList<Pattern>();

    public AdditionalPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
            ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ADDITIONAL_PROPERTIES, validationContext);
        if (schemaNode.isBoolean()) {
            allowAdditionalProperties = schemaNode.booleanValue();
            additionalPropertiesSchema = null;
        } else if (schemaNode.isObject()) {
            allowAdditionalProperties = true;
            additionalPropertiesSchema = new JsonSchema(validationContext, getValidatorType().getValue(), schemaNode, parentSchema);
        } else {
            allowAdditionalProperties = false;
            additionalPropertiesSchema = null;
        }

        allowedProperties = new HashSet<String>();
        JsonNode propertiesNode = parentSchema.getSchemaNode().get(PropertiesValidator.PROPERTY);
        if (propertiesNode != null) {
            for (Iterator<String> it = propertiesNode.fieldNames(); it.hasNext(); ) {
                allowedProperties.add(it.next());
            }
        }

        JsonNode patternPropertiesNode = parentSchema.getSchemaNode().get(PatternPropertiesValidator.PROPERTY);
        if (patternPropertiesNode != null) {
            for (Iterator<String> it = patternPropertiesNode.fieldNames(); it.hasNext(); ) {
                patternProperties.add(Pattern.compile(it.next()));
            }
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        if (logger.isDebugEnabled()) debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        if (!node.isObject()) {
            // ignore no object
            return errors;
        }

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            // skip the context items
            if (pname.startsWith("#")) {
                continue;
            }
            boolean handledByPatternProperties = false;
            for (Pattern pattern : patternProperties) {
                Matcher m = pattern.matcher(pname);
                if (m.find()) {
                    handledByPatternProperties = true;
                    break;
                }
            }

            if (!allowedProperties.contains(pname) && !handledByPatternProperties) {
                if (!allowAdditionalProperties) {
                    errors.add(buildValidationMessage(at, pname));
                } else {
                    if (additionalPropertiesSchema != null) {
                        errors.addAll(additionalPropertiesSchema.validate(node.get(pname), rootNode, at + "." + pname));
                    }
                }
            }
        }
        return Collections.unmodifiableSet(errors);
    }

}
