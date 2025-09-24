/*
 * Copyright (c) 2024 the original author or authors.
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
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.InvalidSchemaRefException;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.ThreadSafeCachingSupplier;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

import java.util.function.Supplier;

/**
 * {@link KeywordValidator} that resolves $dynamicRef.
 */
public class DynamicRefValidator extends BaseKeywordValidator {
    protected final SchemaRef schema;

    public DynamicRefValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.DYNAMIC_REF, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        String refValue = schemaNode.asText();
        this.schema = getRefSchema(parentSchema, schemaContext, refValue, evaluationPath);
    }

    static SchemaRef getRefSchema(Schema parentSchema, SchemaContext schemaContext, String refValue,
            NodePath evaluationPath) {
        String ref = resolve(parentSchema, refValue);
        return new SchemaRef(getSupplier(() -> {
            Schema refSchema = schemaContext.getDynamicAnchors().get(ref);
            if (refSchema == null) { // This is a $dynamicRef without a matching $dynamicAnchor
                // A $dynamicRef without a matching $dynamicAnchor in the same schema resource
                // behaves like a normal $ref to $anchor
                // A $dynamicRef without anchor in fragment behaves identical to $ref
                SchemaRef r = RefValidator.getRefSchema(parentSchema, schemaContext, refValue, evaluationPath);
                if (r != null) {
                    refSchema = r.getSchema();
                }
            } else {
                // Check parents
                Schema base = parentSchema;
                int index = ref.indexOf("#");
                String anchor = ref.substring(index);
                String absoluteIri = ref.substring(0, index);
                while (base.getEvaluationParentSchema() != null) {
                    base = base.getEvaluationParentSchema();
                    String baseAbsoluteIri = base.getSchemaLocation().getAbsoluteIri() != null ? base.getSchemaLocation().getAbsoluteIri().toString() : "";
                    if (!baseAbsoluteIri.equals(absoluteIri)) {
                        absoluteIri = baseAbsoluteIri;
                        String parentRef = SchemaLocation.resolve(base.getSchemaLocation(), anchor);
                        Schema parentRefSchema = schemaContext.getDynamicAnchors().get(parentRef);
                        if (parentRefSchema != null) {
                            refSchema = parentRefSchema;
                        }
                    }
                }
            }
            
            if (refSchema != null) {
                refSchema = refSchema.fromRef(parentSchema, evaluationPath);
            }
            return refSchema;
        }, schemaContext.getSchemaRegistryConfig().isCacheRefs()));
    }

    static <T> Supplier<T> getSupplier(Supplier<T> supplier, boolean cache) {
        return cache ? new ThreadSafeCachingSupplier<>(supplier) : supplier;
    }

    private static String resolve(Schema parentSchema, String refValue) {
        // $ref prevents a sibling $id from changing the base uri
        Schema base = parentSchema;
        if (parentSchema.getId() != null && parentSchema.getParentSchema() != null) {
            base = parentSchema.getParentSchema();
        }
        return SchemaLocation.resolve(base.getSchemaLocation(), refValue);
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        Schema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            Error error = error().keyword(KeywordType.DYNAMIC_REF.getValue())
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
            Error error = error().keyword(KeywordType.DYNAMIC_REF.getValue())
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
