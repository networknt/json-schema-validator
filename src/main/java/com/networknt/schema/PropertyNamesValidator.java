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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class PropertyNamesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(PropertyNamesValidator.class);
    private final JsonSchema innerSchema;
    public PropertyNamesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTYNAMES, validationContext);
        innerSchema = new JsonSchema(validationContext, schemaPath, parentSchema.getCurrentUri(), schemaNode, parentSchema);
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            final String pname = it.next();
            final TextNode pnameText = TextNode.valueOf(pname);
            final Set<ValidationMessage> schemaErrors = innerSchema.validate(pnameText, node, atPath(at, pname));
            for (final ValidationMessage schemaError : schemaErrors) {
                final String path = schemaError.getPath();
                String msg = schemaError.getMessage();
                if (msg.startsWith(path))
                    msg = msg.substring(path.length()).replaceFirst("^:\\s*", "");

                errors.add(buildValidationMessage(schemaError.getPath(), msg));
            }
        }
        return Collections.unmodifiableSet(errors);
    }


    @Override
    public void preloadJsonSchema() {
        innerSchema.initializeValidators();
    }
}
