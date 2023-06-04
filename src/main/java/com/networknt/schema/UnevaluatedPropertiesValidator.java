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
import com.networknt.schema.utils.JsonNodeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UnevaluatedPropertiesValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedPropertiesValidator.class);

    private final JsonSchema schema;
    private final boolean disabled;

    public UnevaluatedPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_PROPERTIES, validationContext);

        this.disabled = validationContext.getConfig().isUnevaluatedPropertiesAnalysisDisabled();
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedProperties' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        if (this.disabled) return Collections.emptySet();

        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = CollectorContext.getInstance();

        collectorContext.exitDynamicScope();
        try {
            Set<String> allPaths = allPaths(node, at);

            // Short-circuit since schema is 'true'
            if (super.schemaNode.isBoolean() && super.schemaNode.asBoolean()) {
                collectorContext.getEvaluatedProperties().addAll(allPaths);
                return Collections.emptySet();
            }

            Set<String> unevaluatedPaths = unevaluatedPaths(allPaths);

            // Short-circuit since schema is 'false'
            if (super.schemaNode.isBoolean() && !super.schemaNode.asBoolean() && !unevaluatedPaths.isEmpty()) {
                return reportUnevaluatedPaths(unevaluatedPaths);
            }

            Set<String> failingPaths = new HashSet<>();
            unevaluatedPaths.forEach(path -> {
                String pointer = getPathType().convertToJsonPointer(path);
                JsonNode property = rootNode.at(pointer);
                if (!this.schema.validate(property, rootNode, path).isEmpty()) {
                    failingPaths.add(path);
                }
            });

            if (failingPaths.isEmpty()) {
                collectorContext.getEvaluatedProperties().addAll(allPaths);
            } else {
                return reportUnevaluatedPaths(failingPaths);
            }

            return Collections.emptySet();
        } finally {
            collectorContext.enterDynamicScope();
        }
    }

    private static final Pattern NUMERIC = Pattern.compile("^\\d+$");

    private Set<String> allPaths(JsonNode node, String at) {
        return JsonNodeUtil.allPaths(getPathType(), at, node)
            .stream()
            .filter(this::isProperty)
            .collect(Collectors.toSet());
    }

    private boolean isProperty(String path) {
        String jsonPointer = getPathType().convertToJsonPointer(path);
        String[] segment = jsonPointer.split("/");
        if (0 == segment.length) return false;
        String lastSegment = segment[segment.length - 1];
        return !NUMERIC.matcher(lastSegment).matches();
    }

    private Set<ValidationMessage> reportUnevaluatedPaths(Set<String> unevaluatedPaths) {
        List<String> paths = new ArrayList<>(unevaluatedPaths);
        paths.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.singleton(buildValidationMessage(String.join("\n  ", paths)));
    }

    private static Set<String> unevaluatedPaths(Set<String> allPaths) {
        Set<String> unevaluatedProperties = new HashSet<>(allPaths);
        unevaluatedProperties.removeAll(CollectorContext.getInstance().getEvaluatedProperties());
        return unevaluatedProperties;
    }
}
