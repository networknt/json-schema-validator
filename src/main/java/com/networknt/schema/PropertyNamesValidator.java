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
    public PropertyNamesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTYNAMES, validationContext);
        innerSchema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        Set<ValidationMessage> errors = null;
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            final String pname = it.next();
            final TextNode pnameText = TextNode.valueOf(pname);
            final Set<ValidationMessage> schemaErrors = innerSchema.validate(executionContext, pnameText, node, instanceLocation.append(pname));
            for (final ValidationMessage schemaError : schemaErrors) {
                final String path = schemaError.getInstanceLocation().toString();
                String msg = schemaError.getMessage();
                if (msg.startsWith(path)) {
                    msg = msg.substring(path.length()).replaceFirst("^:\\s*", "");
                }
                if (errors == null) {
                    errors = new LinkedHashSet<>();
                }
                errors.add(
                        message().property(pname).instanceNode(node).instanceLocation(instanceLocation)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .failFast(executionContext.isFailFast()).arguments(pname, msg).build());
            }
        }
        return errors == null || errors.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }


    @Override
    public void preloadJsonSchema() {
        innerSchema.initializeValidators();
    }
}
