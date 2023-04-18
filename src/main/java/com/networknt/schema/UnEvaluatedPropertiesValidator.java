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

public class UnEvaluatedPropertiesValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnEvaluatedPropertiesValidator.class);
    private static final String UNEVALUATED_PROPERTIES = "com.networknt.schema.UnEvaluatedPropertiesValidator.UnevaluatedProperties";
    private JsonNode schemaNode = null;

    public UnEvaluatedPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_PROPERTIES, validationContext);
        this.schemaNode = schemaNode;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        // Check if unevaluatedProperties is a boolean value.
        if (!schemaNode.isBoolean()) {
            return Collections.emptySet();
        }

        // Continue checking unevaluatedProperties.
        boolean unevaluatedProperties = schemaNode.booleanValue();

        // Process all paths in node.
        List<String> allPaths = new ArrayList<>();
        processAllPaths(node, at, allPaths);

        // Check for errors only if unevaluatedProperties is false.
        if (!unevaluatedProperties) {

            // Process UnEvaluated Properties.
            Set<String> unEvaluatedProperties = getUnEvaluatedProperties(allPaths);

            // If unevaluatedProperties is not empty add error.
            if (!unEvaluatedProperties.isEmpty()) {
                CollectorContext.getInstance().add(UNEVALUATED_PROPERTIES, unEvaluatedProperties);
                return Collections.singleton(buildValidationMessage(String.join(", ", unEvaluatedProperties)));
            }
        } else {
            // Add all properties as evaluated.
            CollectorContext.getInstance().getEvaluatedProperties().addAll(allPaths);
        }
        return Collections.emptySet();
    }

    private Set<String> getUnEvaluatedProperties(Collection<String> allPaths) {
        Set<String> unevaluatedProperties = new LinkedHashSet<>(allPaths);
        unevaluatedProperties.removeAll(CollectorContext.getInstance().getEvaluatedProperties());
        return unevaluatedProperties;
    }

    public void processAllPaths(JsonNode node, String at, List<String> paths) {
        Iterator<String> nodesIterator = node.fieldNames();
        while (nodesIterator.hasNext()) {
            String fieldName = nodesIterator.next();
            JsonNode jsonNode = node.get(fieldName);
            if (jsonNode.isObject()) {
                processAllPaths(jsonNode, atPath(at, fieldName), paths);
            }
            paths.add(atPath(at, fieldName));
        }
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(node, rootNode, at);
        }
        return Collections.emptySet();
    }
}