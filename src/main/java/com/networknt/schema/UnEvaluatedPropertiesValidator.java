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
        if (!schemaNode.isBoolean()) {
            return Collections.emptySet();
        }
        boolean unevaluatedProperties = schemaNode.booleanValue();
        if (!unevaluatedProperties) {
            List<String> allPaths = new ArrayList<>();
            processAllPaths(node, at, allPaths);
            List<String> unEvaluatedProperties = getUnEvaluatedProperties(allPaths);
            if (!unEvaluatedProperties.isEmpty()) {
                CollectorContext.getInstance().add(UNEVALUATED_PROPERTIES, unEvaluatedProperties);
                return Collections.singleton(buildValidationMessage(String.join(", ", unEvaluatedProperties)));
            }
        }
        return Collections.emptySet();
    }

    private List<String> getUnEvaluatedProperties(List<String> allPaths) {
        List<String> unevaluatedProperties = new ArrayList<>();
        Object evaluatedProperties = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);
        if (evaluatedProperties != null) {
            List<String> evaluatedPropertiesList = (List<String>) evaluatedProperties;
            allPaths.forEach(path -> {
                if (!evaluatedPropertiesList.contains(path)) {
                    unevaluatedProperties.add(path);
                }
            });
        }
        return unevaluatedProperties;
    }

    public void processAllPaths(JsonNode node, String at, List<String> paths) {
        Iterator<String> nodesIterator = node.fieldNames();
        while (nodesIterator.hasNext()) {
            String fieldName = nodesIterator.next();
            JsonNode jsonNode = node.get(fieldName);
            paths.add(at + "." + fieldName);
            if (jsonNode.isObject()) {
                processAllPaths(jsonNode, at + "." + fieldName, paths);
            }
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
