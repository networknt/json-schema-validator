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
 * {@link KeywordValidator} that resolves $ref.
 */
public class RefValidator extends BaseKeywordValidator {
    protected final SchemaRef schema;

    private static final String REF_CURRENT = "#";

    public RefValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(Keywords.REF, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        String refValue = schemaNode.asText();
        this.schema = getRefSchema(parentSchema, schemaContext, refValue, evaluationPath);
    }

    static SchemaRef getRefSchema(Schema parentSchema, SchemaContext schemaContext, String refValue,
            NodePath evaluationPath) {
        // The evaluationPath is used to derive the keywordLocation
        final String refValueOriginal = refValue;

        if (!refValue.startsWith(REF_CURRENT)) {
            // This will be the uri extracted from the refValue (this may be a relative or absolute uri).
            final String refUri;
            final int index = refValue.indexOf(REF_CURRENT);
            if (index > 0) {
                refUri = refValue.substring(0, index);
            } else {
                refUri = refValue;
            }

            // This will determine the correct absolute uri for the refUri. This decision will take into
            // account the current uri of the parent schema.
            String schemaUriFinal = resolve(parentSchema, refUri);
            SchemaLocation schemaLocation = SchemaLocation.of(schemaUriFinal);
            // This should retrieve schemas regardless of the protocol that is in the uri.
            return new SchemaRef(getSupplier(() -> {
                Schema schemaResource = schemaContext.getSchemaResources().get(schemaUriFinal);
                if (schemaResource == null) {
                    schemaResource = schemaContext.getSchemaRegistry().loadSchema(schemaLocation); 
                    if (schemaResource != null) {
                        copySchemaResources(schemaContext, schemaResource);
                    }
                }
                if (index < 0) {
                    if (schemaResource == null) {
                        return null;
                    }
                    return schemaResource.fromRef(parentSchema, evaluationPath);
                } else {
                    String newRefValue = refValue.substring(index);
                    String find = schemaLocation.getAbsoluteIri() + newRefValue;
                    Schema findSchemaResource = schemaContext.getSchemaResources().get(find);
                    if (findSchemaResource == null) {
                        findSchemaResource = schemaContext.getDynamicAnchors().get(find); 
                    }
                    if (findSchemaResource != null) {
                        schemaResource = findSchemaResource;   
                    } else {
                        schemaResource = getSchema(schemaResource, schemaContext, newRefValue, refValueOriginal,
                                evaluationPath);
                    }
                    if (schemaResource == null) {
                        return null;
                    }
                    return schemaResource.fromRef(parentSchema, evaluationPath);
                }
            }, schemaContext.getSchemaRegistryConfig().isCacheRefs()));
            
        } else if (SchemaLocation.Fragment.isAnchorFragment(refValue)) {
            String absoluteIri = resolve(parentSchema, refValue);
            // Schema resource needs to update the parent and evaluation path
            return new SchemaRef(getSupplier(() -> {
                Schema schemaResource = schemaContext.getSchemaResources().get(absoluteIri);
                if (schemaResource == null) {
                    schemaResource = schemaContext.getDynamicAnchors().get(absoluteIri);
                }
                if (schemaResource == null) {
                    schemaResource = getSchema(parentSchema, schemaContext, refValue, refValueOriginal, evaluationPath);
                }
                if (schemaResource == null) {
                    return null;
                }
                return schemaResource.fromRef(parentSchema, evaluationPath);
            }, schemaContext.getSchemaRegistryConfig().isCacheRefs()));
        }
        if (refValue.equals(REF_CURRENT)) {
            return new SchemaRef(
                    getSupplier(() -> parentSchema.findSchemaResourceRoot().fromRef(parentSchema, evaluationPath),
                            schemaContext.getSchemaRegistryConfig().isCacheRefs()));
        }
        return new SchemaRef(getSupplier(
                () -> getSchema(parentSchema, schemaContext, refValue, refValueOriginal, evaluationPath)
                        .fromRef(parentSchema, evaluationPath),
                schemaContext.getSchemaRegistryConfig().isCacheRefs()));
    }

    static <T> Supplier<T> getSupplier(Supplier<T> supplier, boolean cache) {
        return cache ? new CachedSupplier<>(supplier) : supplier;
    }

    private static void copySchemaResources(SchemaContext schemaContext, Schema schemaResource) {
        if (!schemaResource.getSchemaContext().getSchemaResources().isEmpty()) {
            schemaContext.getSchemaResources()
                    .putAll(schemaResource.getSchemaContext().getSchemaResources());
        }
        if (!schemaResource.getSchemaContext().getSchemaReferences().isEmpty()) {
            schemaContext.getSchemaReferences()
                    .putAll(schemaResource.getSchemaContext().getSchemaReferences());
        }
        if (!schemaResource.getSchemaContext().getDynamicAnchors().isEmpty()) {
            schemaContext.getDynamicAnchors()
                    .putAll(schemaResource.getSchemaContext().getDynamicAnchors());
        }
    }
    
    private static String resolve(Schema parentSchema, String refValue) {
        // $ref prevents a sibling $id from changing the base uri
        Schema base = parentSchema;
        if (parentSchema.getId() != null && parentSchema.getParentSchema() != null) {
            base = parentSchema.getParentSchema();
        }
        return SchemaLocation.resolve(base.getSchemaLocation(), refValue);
    }

    private static Schema getSchema(Schema parent,
                                                  SchemaContext schemaContext,
                                                  String refValue,
                                                  String refValueOriginal,
                                                  NodePath evaluationPath) {
        // This should be processing json pointer fragments only
        NodePath fragment = SchemaLocation.Fragment.of(refValue);
        String schemaReference = resolve(parent, refValueOriginal);
        // ConcurrentHashMap computeIfAbsent does not allow calls that result in a
        // recursive update to the map.
        // The getSubSchema potentially recurses to call back to getJsonSchema again
        Schema result = schemaContext.getSchemaReferences().get(schemaReference);
        if (result == null) {
            synchronized (schemaContext.getSchemaRegistry()) { // acquire lock on shared factory object to prevent deadlock
                result = schemaContext.getSchemaReferences().get(schemaReference);
                if (result == null) {
                    result = parent.getSubSchema(fragment);
                    if (result != null) {
                        schemaContext.getSchemaReferences().put(schemaReference, result);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        
        Schema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            Error error = error().keyword(Keywords.REF.getValue())
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
            Error error = error().keyword(Keywords.REF.getValue())
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
