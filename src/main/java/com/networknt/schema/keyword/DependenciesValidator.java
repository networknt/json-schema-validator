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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

import java.util.*;

/**
 * {@link KeywordValidator} for dependencies.
 */
public class DependenciesValidator extends BaseKeywordValidator implements KeywordValidator {
    private final Map<String, List<String>> propertyDeps = new HashMap<>();
    private final Map<String, Schema> schemaDeps = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param schemaLocation    the schema location
     * @param evaluationPath    the evaluation path
     * @param schemaNode        the schema node
     * @param parentSchema      the parent schema
     * @param schemaContext the schema context
     */
    public DependenciesValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {

        super(KeywordType.DEPENDENCIES, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);

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
                schemaDeps.put(pname, schemaContext.newSchema(schemaLocation.append(pname),
                        evaluationPath.append(pname), pvalue, parentSchema));
            }
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            List<String> deps = propertyDeps.get(pname);
            if (deps != null && !deps.isEmpty()) {
                for (String field : deps) {
                    if (node.get(field) == null) {
                        executionContext.addError(error().instanceNode(node).property(pname).instanceLocation(instanceLocation)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .arguments(propertyDeps.toString()).build());
                    }
                }
            }
            Schema schema = schemaDeps.get(pname);
            if (schema != null) {
                schema.validate(executionContext, node, rootNode, instanceLocation);
            }
        }
    }

    @Override
    public void preloadSchema() {
        preloadSchemas(schemaDeps.values());
    }
}
