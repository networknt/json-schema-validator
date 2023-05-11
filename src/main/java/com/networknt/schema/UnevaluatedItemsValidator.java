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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class UnevaluatedItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedItemsValidator.class);

    private final JsonSchema schema;

    public UnevaluatedItemsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_ITEMS, validationContext);

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = new JsonSchema(validationContext, schemaPath, parentSchema.getCurrentUri(), schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedItems' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = CollectorContext.getInstance();

        Set<String> allPaths = allPaths(node, at);
        Set<String> unevaluatedPaths = unevaluatedPaths(allPaths);

        Set<String> failingPaths = new HashSet<>();
        unevaluatedPaths.forEach(path -> {
            String pointer = getPathType().convertToJsonPointer(path);
            JsonNode property = rootNode.at(pointer);
            if (!this.schema.validate(property, rootNode, path).isEmpty()) {
                failingPaths.add(path);
            }
        });

        if (failingPaths.isEmpty()) {
            collectorContext.getEvaluatedItems().addAll(allPaths);
        } else {
            List<String> paths = new ArrayList<>(failingPaths);
            paths.sort(String.CASE_INSENSITIVE_ORDER);
            return Collections.singleton(buildValidationMessage(String.join(", ", paths)));
        }

        return Collections.emptySet();
    }

    private Set<String> allPaths(JsonNode node, String at) {
        Set<String> results = new HashSet<>();
        for (int i = 0; i < node.size(); ++i) {
            results.add(atPath(at, i));
        }
        return results;
    }

    private static Set<String> unevaluatedPaths(Set<String> allPaths) {
        Set<String> unevaluatedProperties = new HashSet<>(allPaths);
        unevaluatedProperties.removeAll(CollectorContext.getInstance().getEvaluatedItems());
        return unevaluatedProperties;
    }

}
