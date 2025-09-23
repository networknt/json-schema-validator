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
import com.networknt.schema.CachedSupplier;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.InvalidSchemaRefException;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

import java.util.function.Supplier;

/**
 * {@link KeywordValidator} that resolves $recursiveRef.
 */
public class RecursiveRefValidator extends BaseKeywordValidator {
    protected final SchemaRef schema;

    public RecursiveRefValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(ValidatorTypeCode.RECURSIVE_REF, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);

        String refValue = schemaNode.asText();
        if (!"#".equals(refValue)) {
            Error error = error()
                    .keyword(ValidatorTypeCode.RECURSIVE_REF.getValue()).messageKey("internal.invalidRecursiveRef")
                    .message("The value of a $recursiveRef must be '#' but is '{0}'").instanceLocation(schemaLocation.getFragment())
                    .instanceNode(this.schemaNode)
                    .evaluationPath(evaluationPath).arguments(refValue).build();
            throw new SchemaException(error);
        }
        this.schema = getRefSchema(parentSchema, schemaContext, refValue, evaluationPath);
    }

    static SchemaRef getRefSchema(Schema parentSchema, SchemaContext schemaContext, String refValue,
            NodePath evaluationPath) {
        return new SchemaRef(getSupplier(() -> getSchema(parentSchema, schemaContext, refValue, evaluationPath), schemaContext.getSchemaRegistryConfig().isCacheRefs()));
    }

    static <T> Supplier<T> getSupplier(Supplier<T> supplier, boolean cache) {
        return cache ? new CachedSupplier<>(supplier) : supplier;
    }

    static Schema getSchema(Schema parentSchema, SchemaContext schemaContext, String refValue,
            NodePath evaluationPath) {
        Schema refSchema = parentSchema.findSchemaResourceRoot(); // Get the document
        Schema current = refSchema;
        Schema check = null;
        String base = null;
        String baseCheck = null;
        if (refSchema != null)
            base = current.getSchemaLocation().getAbsoluteIri() != null ? current.getSchemaLocation().getAbsoluteIri().toString() : "";
            if (current.isRecursiveAnchor()) {
                // Check dynamic scope
                while (current.getEvaluationParentSchema() != null) {
                    current = current.getEvaluationParentSchema();
                    baseCheck = current.getSchemaLocation().getAbsoluteIri() != null ? current.getSchemaLocation().getAbsoluteIri().toString() : "";
                    if (!base.equals(baseCheck)) {
                        base = baseCheck;
                        // Check if it has a dynamic anchor
                        check = current.findSchemaResourceRoot();
                        if (check.isRecursiveAnchor()) {
                            refSchema = check;
                        }
                    }
                }
            }
        if (refSchema != null) {
            refSchema = refSchema.fromRef(parentSchema, evaluationPath);
        }
        return refSchema;
    }
    
    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        
        Schema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            Error error = error().keyword(ValidatorTypeCode.RECURSIVE_REF.getValue())
                    .messageKey("internal.unresolvedRef").message("Reference {0} cannot be resolved")
                    .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                    .arguments(schemaNode.asText()).build();
            throw new InvalidSchemaRefException(error);
        }
         refSchema.validate(executionContext, node, rootNode, instanceLocation);
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        
        // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
        // these schemas will be cached along with config. We have to replace the config for cached $ref references
        // with the latest config. Reset the config.
        Schema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            Error error = error().keyword(ValidatorTypeCode.RECURSIVE_REF.getValue())
                    .messageKey("internal.unresolvedRef").message("Reference {0} cannot be resolved")
                    .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                    .arguments(schemaNode.asText()).build();
            throw new InvalidSchemaRefException(error);
        }
        if (node == null) {
            // Check for circular dependency
            SchemaLocation schemaLocation = refSchema.getSchemaLocation();
            Schema check = refSchema;
            boolean circularDependency = false;
            while (check.getEvaluationParentSchema() != null) {
                check = check.getEvaluationParentSchema();
                if (check.getSchemaLocation().equals(schemaLocation)) {
                    circularDependency = true;
                    break;
                }
            }
            if (circularDependency) {
                return;
            }
        }
        refSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
    }

    public SchemaRef getSchemaRef() {
        return this.schema;
    }

    @Override
    public void preloadSchema() {
        Schema jsonSchema = null;
        try {
            jsonSchema = this.schema.getSchema();
        } catch (SchemaException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SchemaException(e);
        }
        // Check for circular dependency
        // Only one cycle is pre-loaded
        // The rest of the cycles will load at execution time depending on the input
        // data
        SchemaLocation schemaLocation = jsonSchema.getSchemaLocation();
        Schema check = jsonSchema;
        boolean circularDependency = false;
        int depth = 0;
        while (check.getEvaluationParentSchema() != null) {
            depth++;
            check = check.getEvaluationParentSchema();
            if (check.getSchemaLocation().equals(schemaLocation)) {
                circularDependency = true;
                break;
            }
        }
        if (this.schemaContext.getSchemaRegistryConfig().isCacheRefs() && !circularDependency
                && depth < this.schemaContext.getSchemaRegistryConfig().getPreloadSchemaRefMaxNestingDepth()) {
            jsonSchema.initializeValidators();
        }
    }
}
