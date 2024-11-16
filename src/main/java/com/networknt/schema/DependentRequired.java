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

/**
 * {@link JsonValidator} for dependentRequired.
 */
public class DependentRequired extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DependentRequired.class);
    private final Map<String, List<String>> propertyDependencies = new HashMap<>();

    public DependentRequired(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.DEPENDENT_REQUIRED, validationContext);

        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            JsonNode pvalue = schemaNode.get(pname);
            if (pvalue.isArray()) {
                List<String> dependencies = propertyDependencies.computeIfAbsent(pname, k -> new ArrayList<>());

                for (int i = 0; i < pvalue.size(); i++) {
                    dependencies.add(pvalue.get(i).asText());
                }
            }
        }
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        Set<ValidationMessage> errors = new LinkedHashSet<>();

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            List<String> dependencies = propertyDependencies.get(pname);
            if (dependencies != null && !dependencies.isEmpty()) {
                for (String field : dependencies) {
                    if (node.get(field) == null) {
                        errors.add(message().instanceNode(node).property(pname).instanceLocation(instanceLocation)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .failFast(executionContext.isFailFast()).arguments(field, pname)
                                .build());
                    }
                }
            }
        }

        return Collections.unmodifiableSet(errors);
    }

}
