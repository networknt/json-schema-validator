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
import java.util.stream.Collectors;

public class UnevaluatedPropertiesValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedPropertiesValidator.class);

    private final JsonSchema schema;
    private final boolean disabled;

    public UnevaluatedPropertiesValidator(JsonNodePath schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_PROPERTIES, validationContext);

        this.disabled = validationContext.getConfig().isUnevaluatedPropertiesAnalysisDisabled();
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedProperties' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        if (this.disabled || !node.isObject()) return Collections.emptySet();

        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = executionContext.getCollectorContext();

        collectorContext.exitDynamicScope();
        try {
            Set<JsonNodePath> allPaths = allPaths(node, at);

            // Short-circuit since schema is 'true'
            if (super.schemaNode.isBoolean() && super.schemaNode.asBoolean()) {
                collectorContext.getEvaluatedProperties().addAll(allPaths);
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
                collectorContext.getEvaluatedProperties().addAll(allPaths);
            } else {
                return reportUnevaluatedPaths(failingPaths, executionContext);
            }

            return Collections.emptySet();
        } finally {
            collectorContext.enterDynamicScope();
        }
    }

    private Set<JsonNodePath> allPaths(JsonNode node, JsonNodePath at) {
        Set<JsonNodePath> collector = new LinkedHashSet<>();
        node.fields().forEachRemaining(entry -> {
            collector.add(at.resolve(entry.getKey()));
        });
        return collector;
    }

    private Set<ValidationMessage> reportUnevaluatedPaths(Set<JsonNodePath> unevaluatedPaths, ExecutionContext executionContext) {
        return unevaluatedPaths.stream()
                .map(path -> buildValidationMessage(null, path, executionContext.getExecutionConfig().getLocale()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<JsonNodePath> unevaluatedPaths(CollectorContext collectorContext, Set<JsonNodePath> allPaths) {
        Set<JsonNodePath> unevaluatedProperties = new LinkedHashSet<>(allPaths);
        unevaluatedProperties.removeAll(collectorContext.getEvaluatedProperties());
        return unevaluatedProperties;
    }
}
