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

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link JsonValidator} that resolves $ref.
 */
public class RefValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    protected final JsonSchemaRef schema;

    private static final String REF_CURRENT = "#";

    public RefValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.REF, validationContext);
        String refValue = schemaNode.asText();
        this.schema = getRefSchema(parentSchema, validationContext, refValue, evaluationPath);
    }

    static JsonSchemaRef getRefSchema(JsonSchema parentSchema, ValidationContext validationContext, String refValue,
            JsonNodePath evaluationPath) {
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
            return new JsonSchemaRef(getSupplier(() -> {
                JsonSchema schemaResource = validationContext.getSchemaResources().get(schemaUriFinal);
                if (schemaResource == null) {
                    schemaResource = validationContext.getJsonSchemaFactory().loadSchema(schemaLocation, validationContext.getConfig()); 
                    if (schemaResource != null) {
                        copySchemaResources(validationContext, schemaResource);
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
                    JsonSchema findSchemaResource = validationContext.getSchemaResources().get(find);
                    if (findSchemaResource == null) {
                        findSchemaResource = validationContext.getDynamicAnchors().get(find); 
                    }
                    if (findSchemaResource != null) {
                        schemaResource = findSchemaResource;   
                    } else {
                        schemaResource = getJsonSchema(schemaResource, validationContext, newRefValue, refValueOriginal,
                                evaluationPath);
                    }
                    if (schemaResource == null) {
                        return null;
                    }
                    return schemaResource.fromRef(parentSchema, evaluationPath);
                }
            }, validationContext.getConfig().isCacheRefs()));
            
        } else if (SchemaLocation.Fragment.isAnchorFragment(refValue)) {
            String absoluteIri = resolve(parentSchema, refValue);
            // Schema resource needs to update the parent and evaluation path
            return new JsonSchemaRef(getSupplier(() -> {
                JsonSchema schemaResource = validationContext.getSchemaResources().get(absoluteIri);
                if (schemaResource == null) {
                    schemaResource = validationContext.getDynamicAnchors().get(absoluteIri);
                }
                if (schemaResource == null) {
                    schemaResource = getJsonSchema(parentSchema, validationContext, refValue, refValueOriginal, evaluationPath);
                }
                if (schemaResource == null) {
                    return null;
                }
                return schemaResource.fromRef(parentSchema, evaluationPath);
            }, validationContext.getConfig().isCacheRefs()));
        }
        if (refValue.equals(REF_CURRENT)) {
            return new JsonSchemaRef(
                    getSupplier(() -> parentSchema.findSchemaResourceRoot().fromRef(parentSchema, evaluationPath),
                            validationContext.getConfig().isCacheRefs()));
        }
        return new JsonSchemaRef(getSupplier(
                () -> getJsonSchema(parentSchema, validationContext, refValue, refValueOriginal, evaluationPath)
                        .fromRef(parentSchema, evaluationPath),
                validationContext.getConfig().isCacheRefs()));
    }

    static <T> Supplier<T> getSupplier(Supplier<T> supplier, boolean cache) {
        return cache ? new CachedSupplier<>(supplier) : supplier;
    }

    private static void copySchemaResources(ValidationContext validationContext, JsonSchema schemaResource) {
        if (!schemaResource.getValidationContext().getSchemaResources().isEmpty()) {
            validationContext.getSchemaResources()
                    .putAll(schemaResource.getValidationContext().getSchemaResources());
        }
        if (!schemaResource.getValidationContext().getSchemaReferences().isEmpty()) {
            validationContext.getSchemaReferences()
                    .putAll(schemaResource.getValidationContext().getSchemaReferences());
        }
        if (!schemaResource.getValidationContext().getDynamicAnchors().isEmpty()) {
            validationContext.getDynamicAnchors()
                    .putAll(schemaResource.getValidationContext().getDynamicAnchors());
        }
    }
    
    private static String resolve(JsonSchema parentSchema, String refValue) {
        // $ref prevents a sibling $id from changing the base uri
        JsonSchema base = parentSchema;
        if (parentSchema.getId() != null && parentSchema.parentSchema != null) {
            base = parentSchema.parentSchema;
        }
        return SchemaLocation.resolve(base.getSchemaLocation(), refValue);
    }

    private static JsonSchema getJsonSchema(JsonSchema parent,
                                                  ValidationContext validationContext,
                                                  String refValue,
                                                  String refValueOriginal,
                                                  JsonNodePath evaluationPath) {
        // This should be processing json pointer fragments only
        JsonNodePath fragment = SchemaLocation.Fragment.of(refValue);
        String schemaReference = resolve(parent, refValueOriginal);
        // ConcurrentHashMap computeIfAbsent does not allow calls that result in a
        // recursive update to the map.
        // The getSubSchema potentially recurses to call back to getJsonSchema again
        JsonSchema result = validationContext.getSchemaReferences().get(schemaReference);
        if (result == null) {
            synchronized (validationContext.getJsonSchemaFactory()) { // acquire lock on shared factory object to prevent deadlock
                result = validationContext.getSchemaReferences().get(schemaReference);
                if (result == null) {
                    result = parent.getSubSchema(fragment);
                    if (result != null) {
                        validationContext.getSchemaReferences().put(schemaReference, result);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);
        JsonSchema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            ValidationMessage validationMessage = message().type(ValidatorTypeCode.REF.getValue())
                    .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                    .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                    .arguments(schemaNode.asText()).build();
            throw new InvalidSchemaRefException(validationMessage);
        }
        return refSchema.validate(executionContext, node, rootNode, instanceLocation);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        debug(logger, executionContext, node, rootNode, instanceLocation);
        // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
        // these schemas will be cached along with config. We have to replace the config for cached $ref references
        // with the latest config. Reset the config.
        JsonSchema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            ValidationMessage validationMessage = message().type(ValidatorTypeCode.REF.getValue())
                    .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                    .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                    .arguments(schemaNode.asText()).build();
            throw new InvalidSchemaRefException(validationMessage);
        }
        if (node == null) {
            // Check for circular dependency
            SchemaLocation schemaLocation = refSchema.getSchemaLocation();
            JsonSchema check = refSchema;
            boolean circularDependency = false;
            while (check.getEvaluationParentSchema() != null) {
                check = check.getEvaluationParentSchema();
                if (check.getSchemaLocation().equals(schemaLocation)) {
                    circularDependency = true;
                    break;
                }
            }
            if (circularDependency) {
                return Collections.emptySet();
            }
        }
        return refSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
    }

	public JsonSchemaRef getSchemaRef() {
		return this.schema;
	}

    @Override
    public void preloadJsonSchema() {
        JsonSchema jsonSchema = null;
        try {
            jsonSchema = this.schema.getSchema();
        } catch (JsonSchemaException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new JsonSchemaException(e);
        }
        // Check for circular dependency
        // Only one cycle is pre-loaded
        // The rest of the cycles will load at execution time depending on the input
        // data
        SchemaLocation schemaLocation = jsonSchema.getSchemaLocation();
        JsonSchema check = jsonSchema;
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
        if (this.validationContext.getConfig().isCacheRefs() && !circularDependency
                && depth < this.validationContext.getConfig().getPreloadJsonSchemaRefMaxNestingDepth()) {
            jsonSchema.initializeValidators();
        }
    }
}
