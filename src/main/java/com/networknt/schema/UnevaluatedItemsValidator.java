/*
 * Copyright (c) 2023 Network New Technologies Inc.
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

public class UnevaluatedItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedItemsValidator.class);

    private final JsonSchema schema;
    private final boolean disabled;

    public UnevaluatedItemsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_ITEMS, validationContext);

        this.disabled = validationContext.getConfig().isUnevaluatedItemsAnalysisDisabled();
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedItems' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, String at) {
        if (this.disabled || !node.isArray()) return Collections.emptySet();

        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = executionContext.getCollectorContext();

        collectorContext.exitDynamicScope();
        try {
            Set<String> allPaths = allPaths(node, at);

            // Short-circuit since schema is 'true'
            if (super.schemaNode.isBoolean() && super.schemaNode.asBoolean()) {
                collectorContext.getEvaluatedItems().addAll(allPaths);
                return Collections.emptySet();
            }

            Set<String> unevaluatedPaths = unevaluatedPaths(collectorContext, allPaths);

            // Short-circuit since schema is 'false'
            if (super.schemaNode.isBoolean() && !super.schemaNode.asBoolean() && !unevaluatedPaths.isEmpty()) {
                return reportUnevaluatedPaths(unevaluatedPaths, executionContext);
            }

            Set<String> failingPaths = new HashSet<>();
            unevaluatedPaths.forEach(path -> {
                String pointer = getPathType().convertToJsonPointer(path);
                JsonNode property = rootNode.at(pointer);
                if (!this.schema.validate(executionContext, property, rootNode, path).isEmpty()) {
                    failingPaths.add(path);
                }
            });

            if (failingPaths.isEmpty()) {
                collectorContext.getEvaluatedItems().addAll(allPaths);
            } else {
                return reportUnevaluatedPaths(failingPaths, executionContext);
            }

            return Collections.emptySet();
        } finally {
            collectorContext.enterDynamicScope();
        }
    }

    private Set<String> allPaths(JsonNode node, String at) {
        PathType pathType = getPathType();
        Set<String> collector = new LinkedHashSet<>();
        int size = node.size();
        for (int i = 0; i < size; ++i) {
            String path = pathType.append(at, i);
            collector.add(path);
        }
        return collector;
    }

    private Set<ValidationMessage> reportUnevaluatedPaths(Set<String> unevaluatedPaths, ExecutionContext executionContext) {
        List<String> paths = new ArrayList<>(unevaluatedPaths);
        paths.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.singleton(buildValidationMessage(String.join("\n  ", paths), executionContext.getExecutionConfig().getLocale()));
    }

    private static Set<String> unevaluatedPaths(CollectorContext collectorContext, Set<String> allPaths) {
        Set<String> unevaluatedProperties = new HashSet<>(allPaths);
        unevaluatedProperties.removeAll(collectorContext.getEvaluatedItems());
        return unevaluatedProperties;
    }

}
