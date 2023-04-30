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

    private final JsonSchema schema;

    public UnEvaluatedPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_PROPERTIES, validationContext);

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = new JsonSchema(validationContext, schemaPath, parentSchema.getCurrentUri(), schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedProperties' MUST be a valid JSON Schema.");
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = CollectorContext.getInstance();

        Set<String> allPaths = allPaths(node, at);
        Set<String> unevaluatedPaths = unevaluatedPaths(allPaths);

        Set<String> failingPaths = new HashSet<>();
        unevaluatedPaths.forEach(path -> {
            String pointer = getPathType().convertToJsonPointer(path);
            JsonNode property = rootNode.at(pointer);
            if (!schema.validate(property, rootNode, path).isEmpty()) {
                failingPaths.add(path);
            }
        });

        if (failingPaths.isEmpty()) {
            collectorContext.getEvaluatedProperties().addAll(allPaths);
        } else {
            // TODO: Why add this to the context if it is never referenced?
            collectorContext.add(UNEVALUATED_PROPERTIES, unevaluatedPaths);
            List<String> paths = new ArrayList<>(failingPaths);
            paths.sort(String.CASE_INSENSITIVE_ORDER);
            return Collections.singleton(buildValidationMessage(String.join(", ", paths)));
        }

        return Collections.emptySet();
    }

    private Set<String> unevaluatedPaths(Set<String> allPaths) {
        Set<String> unevaluatedProperties = new HashSet<>(allPaths);
        unevaluatedProperties.removeAll(CollectorContext.getInstance().getEvaluatedProperties());
        return unevaluatedProperties;
    }

    private Set<String> allPaths(JsonNode node, String at) {
        Set<String> results = new HashSet<>();
        processAllPaths(node, at, results);
        return results;
    }

    private void processAllPaths(JsonNode node, String at, Set<String> paths) {
        Iterator<String> nodesIterator = node.fieldNames();
        while (nodesIterator.hasNext()) {
            String fieldName = nodesIterator.next();
            String path = atPath(at, fieldName);
            paths.add(path);

            JsonNode jsonNode = node.get(fieldName);
            if (jsonNode.isObject()) {
                processAllPaths(jsonNode, path, paths);
            }
        }
    }

}
