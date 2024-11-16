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
 * {@link JsonValidator} for dependencies.
 */
public class DependenciesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DependenciesValidator.class);
    private final Map<String, List<String>> propertyDeps = new HashMap<>();
    private final Map<String, JsonSchema> schemaDeps = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param schemaLocation    the schema location
     * @param evaluationPath    the evaluation path
     * @param schemaNode        the schema node
     * @param parentSchema      the parent schema
     * @param validationContext the validation context
     */
    public DependenciesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.DEPENDENCIES, validationContext);

        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            JsonNode pvalue = schemaNode.get(pname);
            if (pvalue.isArray()) {
                List<String> depsProps = propertyDeps.get(pname);
                if (depsProps == null) {
                    depsProps = new ArrayList<>();
                    propertyDeps.put(pname, depsProps);
                }
                for (int i = 0; i < pvalue.size(); i++) {
                    depsProps.add(pvalue.get(i).asText());
                }
            } else if (pvalue.isObject() || pvalue.isBoolean()) {
                schemaDeps.put(pname, validationContext.newSchema(schemaLocation.append(pname),
                        evaluationPath.append(pname), pvalue, parentSchema));
            }
        }
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        Set<ValidationMessage> errors = null;

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            List<String> deps = propertyDeps.get(pname);
            if (deps != null && !deps.isEmpty()) {
                for (String field : deps) {
                    if (node.get(field) == null) {
                        if (errors == null) {
                            errors = new LinkedHashSet<>();
                        }
                        errors.add(message().instanceNode(node).property(pname).instanceLocation(instanceLocation)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .failFast(executionContext.isFailFast())
                                .arguments(propertyDeps.toString()).build());
                    }
                }
            }
            JsonSchema schema = schemaDeps.get(pname);
            if (schema != null) {
                Set<ValidationMessage> schemaDepsErrors = schema.validate(executionContext, node, rootNode, instanceLocation);
                if (!schemaDepsErrors.isEmpty()) {
                    if (errors == null) {
                        errors = new LinkedHashSet<>();
                    }
                    errors.addAll(schemaDepsErrors);
                }
            }
        }
        return errors == null || errors.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(schemaDeps.values());
    }
}
