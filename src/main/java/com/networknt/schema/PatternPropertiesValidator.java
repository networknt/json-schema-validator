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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPropertiesValidator extends BaseJsonValidator implements JsonValidator {
    public static final String PROPERTY = "patternProperties";
    private static final Logger logger = LoggerFactory.getLogger(PatternPropertiesValidator.class);
    private Map<Pattern, JsonSchema> schemas = new IdentityHashMap<Pattern, JsonSchema>();

    public PatternPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
            ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN_PROPERTIES, validationContext);
        if (!schemaNode.isObject()) {
            throw new JsonSchemaException("patternProperties must be an object node");
        }
        Iterator<String> names = schemaNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            schemas.put(Pattern.compile(name), new JsonSchema(validationContext, name, schemaNode.get(name), parentSchema));
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        if (!node.isObject()) {
            return errors;
        }

        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            JsonNode n = node.get(name);
            for (Map.Entry<Pattern, JsonSchema> entry : schemas.entrySet()) {
                Matcher m = entry.getKey().matcher(name);
                if (m.find()) {
                    errors.addAll(entry.getValue().validate(n, rootNode, at + "." + name));
                }
            }
        }
        return Collections.unmodifiableSet(errors);
    }

}
