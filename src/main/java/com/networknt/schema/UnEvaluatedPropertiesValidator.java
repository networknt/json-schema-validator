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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UnEvaluatedPropertiesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnEvaluatedPropertiesValidator.class);
    public static final String EVALUATED_PROPERTIES = "com.networknt.schema.UnEvaluatedPropertiesValidator.EvaluatedProperties";
    public static final String UNEVALUATED_PROPERTIES = "com.networknt.schema.UnEvaluatedPropertiesValidator.UnevaluatedProperties";
    private JsonNode schemaNode = null;

    public UnEvaluatedPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_PROPERTIES, validationContext);
        this.schemaNode = schemaNode;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {

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
            List<String> unEvaluatedProperties = getUnEvaluatedProperties(allPaths);

            // If unevaluatedProperties is not empty add error.
            if (!unEvaluatedProperties.isEmpty()) {
                CollectorContext.getInstance().add(UNEVALUATED_PROPERTIES, unEvaluatedProperties);
                return Collections.singleton(buildValidationMessage(String.join(", ", unEvaluatedProperties)));
            }
        } else {
            // Add all properties as evaluated.
            CollectorContext.getInstance().add(EVALUATED_PROPERTIES, allPaths);
        }
        return Collections.emptySet();
    }

    private List<String> getUnEvaluatedProperties(List<String> allPaths) {
        List<String> unevaluatedPropertiesList = new ArrayList<>();
        Object evaluatedPropertiesObj = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);
        if (evaluatedPropertiesObj != null) {
            List<String> evaluatedPropertiesList = (List<String>) evaluatedPropertiesObj;
            allPaths.forEach(path -> {
                if (!evaluatedPropertiesList.contains(path)) {
                    unevaluatedPropertiesList.add(path);
                }
            });
        } else {
            unevaluatedPropertiesList.addAll(allPaths);
        }
        return unevaluatedPropertiesList;
    }

    public void processAllPaths(JsonNode node, String at, List<String> paths) {
        Iterator<String> nodesIterator = node.fieldNames();
        while (nodesIterator.hasNext()) {
            String fieldName = nodesIterator.next();
            JsonNode jsonNode = node.get(fieldName);
            if (jsonNode.isObject()) {
                processAllPaths(jsonNode, at + "." + fieldName, paths);
            }
            paths.add(at + "." + fieldName);
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