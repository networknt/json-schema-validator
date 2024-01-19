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
import com.networknt.schema.CollectorContext.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RefValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    protected JsonSchemaRef schema;

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
            return new JsonSchemaRef(new CachedSupplier<>(() -> {
                JsonSchema schemaResource = validationContext.getSchemaResources().get(schemaUriFinal);
                if (schemaResource == null) {
                    schemaResource = validationContext.getJsonSchemaFactory().getSchema(schemaLocation, validationContext.getConfig()); 
                    if (schemaResource != null) {
                        if (!schemaResource.getValidationContext().getSchemaResources().isEmpty()) {
                            validationContext.getSchemaResources()
                                    .putAll(schemaResource.getValidationContext().getSchemaResources());
                        }
                        if (!schemaResource.getValidationContext().getSchemaReferences().isEmpty()) {
                            validationContext.getSchemaReferences()
                                    .putAll(schemaResource.getValidationContext().getSchemaReferences());
                        }
                    }
                }
                if (index < 0) {
                    if (schemaResource == null) {
                        return null;
                    }
                    return schemaResource.fromRef(parentSchema, evaluationPath);
                } else {
                    String newRefValue = refValue.substring(index);
                    schemaResource = getJsonSchema(schemaResource, validationContext, newRefValue, refValueOriginal,
                            evaluationPath);
                    if (schemaResource == null) {
                        return null;
                    }
                    return schemaResource.fromRef(parentSchema, evaluationPath);
                }
            }));
            
        } else if (SchemaLocation.Fragment.isAnchorFragment(refValue)) {
            String absoluteIri = resolve(parentSchema, refValue);
            // Schema resource needs to update the parent and evaluation path
            return new JsonSchemaRef(new CachedSupplier<>(() -> {
                JsonSchema schemaResource = validationContext.getSchemaResources().get(absoluteIri);
                if (schemaResource == null) {
                    schemaResource = getJsonSchema(parentSchema, validationContext, refValue, refValueOriginal, evaluationPath);
                }
                if (schemaResource == null) {
                    return null;
                }
                return schemaResource.fromRef(parentSchema, evaluationPath);
            }));
        }
        if (refValue.equals(REF_CURRENT)) {
            return new JsonSchemaRef(new CachedSupplier<>(
                    () -> parentSchema.findSchemaResourceRoot().fromRef(parentSchema, evaluationPath)));
        }
        return new JsonSchemaRef(new CachedSupplier<>(
                () -> getJsonSchema(parentSchema, validationContext, refValue, refValueOriginal, evaluationPath)));
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
        JsonNode node = parent.getRefSchemaNode(refValue);
        if (node != null) {
            return validationContext.getSchemaReferences().computeIfAbsent(refValueOriginal, key -> {
                return getJsonSchema(node, parent, refValue, evaluationPath);
            });
        }
        return null;
    }
    
    private static JsonSchema getJsonSchema(JsonNode node, JsonSchema parent,
                                                  String refValue,
                                                  JsonNodePath evaluationPath) {
        if (node != null) {
            SchemaLocation path = null;
            JsonSchema currentParent = parent;
            if (refValue.startsWith(REF_CURRENT)) {
                // relative to document
                path = new SchemaLocation(parent.schemaLocation.getAbsoluteIri(),
                        new JsonNodePath(PathType.JSON_POINTER));
                // Attempt to get subschema node
                String[] refParts = refValue.split("/");
                if (refParts.length > 3) {
                    String[] subschemaParts = Arrays.copyOf(refParts, refParts.length - 2);
                    JsonNode subschemaNode = parent.getRefSchemaNode(String.join("/", subschemaParts));
                    String id = parent.getValidationContext().resolveSchemaId(subschemaNode);
                    if (id != null) {
                        if (id.contains(":")) {
                            // absolute
                            path = SchemaLocation.of(id);
                        } else {
                            // relative
                            String absoluteUri = path.getAbsoluteIri().resolve(id).toString();
                            path = SchemaLocation.of(absoluteUri);
                        }
                    }
                }
                String[] parts = refValue.split("/");
                for (int x = 1; x < parts.length; x++) {
                    path = path.append(parts[x]);
                }
            } else if(refValue.contains(":")) {
                // absolute
                path = SchemaLocation.of(refValue); 
            } else {
                // relative to lexical root
                String id = parent.findSchemaResourceRoot().getId();
                path = SchemaLocation.of(id);
                String[] parts = refValue.split("/");
                for (int x = 1; x < parts.length; x++) {
                    path = path.append(parts[x]);
                }
            }
            return parent.getValidationContext().newSchema(path, evaluationPath, node, currentParent);
        }
        throw null;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        CollectorContext collectorContext = executionContext.getCollectorContext();

        Set<ValidationMessage> errors = Collections.emptySet();

        Scope parentScope = collectorContext.enterDynamicScope();
        try {
            debug(logger, node, rootNode, instanceLocation);
            JsonSchema refSchema = this.schema.getSchema();
            if (refSchema == null) {
                ValidationMessage validationMessage = ValidationMessage.builder().type(ValidatorTypeCode.REF.getValue())
                        .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                        .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                        .arguments(schemaNode.asText()).build();
                throw new JsonSchemaException(validationMessage);
            }
            errors = refSchema.validate(executionContext, node, rootNode, instanceLocation);
        } finally {
            Scope scope = collectorContext.exitDynamicScope();
            if (errors.isEmpty()) {
                parentScope.mergeWith(scope);
            }
        }
        return errors;
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        CollectorContext collectorContext = executionContext.getCollectorContext();

        Set<ValidationMessage> errors = Collections.emptySet();

        Scope parentScope = collectorContext.enterDynamicScope();
        try {
            debug(logger, node, rootNode, instanceLocation);
            // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
            // these schemas will be cached along with config. We have to replace the config for cached $ref references
            // with the latest config. Reset the config.
            JsonSchema refSchema = this.schema.getSchema();
            if (refSchema == null) {
                ValidationMessage validationMessage = ValidationMessage.builder().type(ValidatorTypeCode.REF.getValue())
                        .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                        .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                        .arguments(schemaNode.asText()).build();
                throw new JsonSchemaException(validationMessage);
            }
            errors = refSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            return errors;
        } finally {
            Scope scope = collectorContext.exitDynamicScope();
            if (shouldValidateSchema) {
                if (errors.isEmpty()) {
                    parentScope.mergeWith(scope);
                }
            }
        }
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
        jsonSchema.initializeValidators();
    }
}
