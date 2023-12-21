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
import java.util.stream.Collectors;

public class UnevaluatedItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedItemsValidator.class);

    private final JsonSchema schema;

    public UnevaluatedItemsValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_ITEMS, validationContext);

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedItems' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        if (!executionContext.getExecutionConfig().getAnnotationAllowedPredicate().test(getKeyword()) || !node.isArray()) return Collections.emptySet();

        debug(logger, node, rootNode, instanceLocation);
        CollectorContext collectorContext = executionContext.getCollectorContext();

        collectorContext.exitDynamicScope();
        try {
            Set<JsonNodePath> allPaths = allPaths(node, instanceLocation);

            // Short-circuit since schema is 'true'
            if (super.schemaNode.isBoolean() && super.schemaNode.asBoolean()) {
                collectorContext.getEvaluatedItems().addAll(allPaths);
                return Collections.emptySet();
            }

            Set<JsonNodePath> unevaluatedPaths = unevaluatedPaths(collectorContext, allPaths);

            // Short-circuit since schema is 'false'
            if (super.schemaNode.isBoolean() && !super.schemaNode.asBoolean() && !unevaluatedPaths.isEmpty()) {
                return reportUnevaluatedPaths(unevaluatedPaths, executionContext);
            }

            Set<JsonNodePath> failingPaths = new LinkedHashSet<>();
            unevaluatedPaths.forEach(path -> {
                String pointer = path.getPathType().convertToJsonPointer(path.toString());
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

    private Set<JsonNodePath> allPaths(JsonNode node, JsonNodePath instanceLocation) {
        Set<JsonNodePath> collector = new LinkedHashSet<>();
        int size = node.size();
        for (int i = 0; i < size; ++i) {
            JsonNodePath path = instanceLocation.resolve(i);
            collector.add(path);
        }
        return collector;
    }

    private Set<ValidationMessage> reportUnevaluatedPaths(Set<JsonNodePath> unevaluatedPaths, ExecutionContext executionContext) {
        return unevaluatedPaths
                .stream().map(path -> message().instanceLocation(path)
                        .locale(executionContext.getExecutionConfig().getLocale()).build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<JsonNodePath> unevaluatedPaths(CollectorContext collectorContext, Set<JsonNodePath> allPaths) {
        Set<JsonNodePath> unevaluatedProperties = new HashSet<>(allPaths);
        unevaluatedProperties.removeAll(collectorContext.getEvaluatedItems());
        return unevaluatedProperties;
    }

}
