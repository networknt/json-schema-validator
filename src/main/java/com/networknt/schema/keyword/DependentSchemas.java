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
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

import java.util.*;

/**
 * {@link KeywordValidator} for dependentSchemas.
 */
public class DependentSchemas extends BaseKeywordValidator {
    private final Map<String, Schema> schemaDependencies = new HashMap<>();

    public DependentSchemas(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {

        super(ValidatorTypeCode.DEPENDENT_SCHEMAS, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);

        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            JsonNode pvalue = schemaNode.get(pname);
            if (pvalue.isObject() || pvalue.isBoolean()) {
                this.schemaDependencies.put(pname, schemaContext.newSchema(schemaLocation.append(pname),
                        evaluationPath.append(pname), pvalue, parentSchema));
            }
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean walk) {
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            Schema schema = this.schemaDependencies.get(pname);
            if (schema != null) {
                if(!walk) {
                    schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    schema.walk(executionContext, node, rootNode, instanceLocation, true);   
                }
            }
        }
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemaDependencies.values());
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            validate(executionContext, node, rootNode, instanceLocation, true);
            return;
        }
        for (Schema schema : this.schemaDependencies.values()) {
            schema.walk(executionContext, node, rootNode, instanceLocation, false);
        }
    }

}
