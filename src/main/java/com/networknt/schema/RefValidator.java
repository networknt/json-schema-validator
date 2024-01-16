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
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.urn.URNFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

public class RefValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    protected JsonSchemaRef schema;

    private JsonSchema parentSchema;

    private static final String REF_CURRENT = "#";

    public RefValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.REF, validationContext);
        String refValue = schemaNode.asText();
        this.parentSchema = parentSchema;
        this.schema = getRefSchema(parentSchema, validationContext, refValue, evaluationPath);
        if (this.schema == null) {
            ValidationMessage validationMessage = ValidationMessage.builder().type(ValidatorTypeCode.REF.getValue())
                    .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                    .instanceLocation(schemaLocation.getFragment()).evaluationPath(schemaLocation.getFragment())
                    .arguments(refValue).build();
            throw new JsonSchemaException(validationMessage);
        }
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
            URI schemaUri = determineSchemaUri(validationContext.getURIFactory(), parentSchema, refUri);
            if (schemaUri == null) {
                // the URNFactory is optional
                if (validationContext.getURNFactory() == null) {
                    return null;
                }
                // If the uri dose't determinate try to determinate with urn factory
                schemaUri = determineSchemaUrn(validationContext.getURNFactory(), refUri);
                if (schemaUri == null) {
                    return null;
                }
            }

            URI schemaUriFinal = schemaUri;
            // This should retrieve schemas regardless of the protocol that is in the uri.
            return new JsonSchemaRef(new CachedSupplier<>(() -> {
                JsonSchema schemaResource = validationContext.getSchemaResources().get(schemaUriFinal.toString());
                if (schemaResource == null) {
                    schemaResource = validationContext.getJsonSchemaFactory().getSchema(schemaUriFinal, validationContext.getConfig()); 
                }
                if (index < 0) {
                    return schemaResource.fromRef(parentSchema, evaluationPath);
                } else {
                    String newRefValue = refValue.substring(index);
                    schemaResource = getJsonSchema(schemaResource, validationContext, newRefValue, refValueOriginal,
                            evaluationPath);
                    if (schemaResource == null) {
                        throw new JsonSchemaException("Failed to resolve ref");
                    }
                    return schemaResource.fromRef(parentSchema, evaluationPath);
                }
            }));
            
        } else if (SchemaLocation.Fragment.isAnchorFragment(refValue)) {
            // $ref prevents a sibling $id from changing the base uri
            JsonSchema base = parentSchema;
            if (parentSchema.getId() != null && parentSchema.parentSchema != null) {
                base = parentSchema.parentSchema;
            }
            if (base.getCurrentUri() != null) {
                String absoluteIri = SchemaLocation.resolve(base.getSchemaLocation(), refValue);
                JsonSchema schemaResource = validationContext.getSchemaResources().get(absoluteIri);
                if (schemaResource != null) {
                    // Schema resource needs to update the parent and evaluation path
                    return new JsonSchemaRef(
                            new CachedSupplier<>(() -> schemaResource.fromRef(parentSchema, evaluationPath)));
                }
            }
        }
        if (refValue.equals(REF_CURRENT)) {
            return new JsonSchemaRef(new CachedSupplier<>(
                    () -> parentSchema.findSchemaResourceRoot().fromRef(parentSchema, evaluationPath)));
        }
        return new JsonSchemaRef(new CachedSupplier<>(
                () -> getJsonSchema(parentSchema, validationContext, refValue, refValueOriginal, evaluationPath)));
    }

    private static JsonSchema getJsonSchema(JsonSchema parent,
                                                  ValidationContext validationContext,
                                                  String refValue,
                                                  String refValueOriginal,
                                                  JsonNodePath evaluationPath) {
        JsonNode node = parent.getRefSchemaNode(refValue);
        if (node != null) {
            return validationContext.getSchemaReferences().computeIfAbsent(refValueOriginal, key -> {
                return getJsonSchema(node, parent, validationContext, refValue, evaluationPath);
            });
        }
        return null;
    }
    
    private static JsonSchema getJsonSchema(JsonNode node, JsonSchema parent,
                                                  ValidationContext validationContext,
                                                  String refValue,
                                                  JsonNodePath evaluationPath) {
        if (node != null) {
            SchemaLocation path = null;
            JsonSchema currentParent = parent;
            URI currentUri = parent.getCurrentUri();
            if (refValue.startsWith(REF_CURRENT)) {
                // relative to document
                path = new SchemaLocation(parent.schemaLocation.getAbsoluteIri(),
                        new JsonNodePath(PathType.JSON_POINTER));
                // Attempt to get subschema node
                String[] refParts = refValue.split("/");
                if (refParts.length > 3) {
                    String[] subschemaParts = Arrays.copyOf(refParts, refParts.length - 2);
                    JsonNode subschemaNode = parent.getRefSchemaNode(String.join("/", subschemaParts));
                    String id = validationContext.resolveSchemaId(subschemaNode);
                    if (id != null) {
                        if (id.contains(":")) {
                            // absolute
                            currentUri = URI.create(id);
                            path = SchemaLocation.of(id);
                        } else {
                            // relative
                            String absoluteUri = path.getAbsoluteIri().resolve(id).toString();
                            currentUri = URI.create(absoluteUri);
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
            return validationContext.newSchema(path, evaluationPath, currentUri, node, currentParent);
        }
        throw new JsonSchemaException("Cannot find ref "+refValue);
    }

    private static URI determineSchemaUri(final URIFactory uriFactory, final JsonSchema parentSchema, final String refUri) {
        URI schemaUri;
        // $ref prevents a sibling $id from changing the base uri
        JsonSchema parent = parentSchema.getParentSchema(); // just the parentSchema is the sibling $id with this $ref
        final URI currentUri = parent != null ? parent.getCurrentUri() : parentSchema.getCurrentUri();
        try {
            if (currentUri == null) {
                schemaUri = uriFactory.create(refUri);
            } else {
                schemaUri = uriFactory.create(currentUri, refUri);
            }
        } catch (IllegalArgumentException e) {
            schemaUri = null;
        }
        return schemaUri;
    }

    private static URI determineSchemaUrn(final URNFactory urnFactory, final String refUri) {
        URI schemaUrn;
        try {
            schemaUrn = urnFactory.create(refUri);
        } catch (IllegalArgumentException e) {
            schemaUrn = null;
        }
        return schemaUrn;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        CollectorContext collectorContext = executionContext.getCollectorContext();

        Set<ValidationMessage> errors = new HashSet<>();

        Scope parentScope = collectorContext.enterDynamicScope();
        try {
            debug(logger, node, rootNode, instanceLocation);
            // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
            // these schemas will be cached along with config. We have to replace the config for cached $ref references
            // with the latest config. Reset the config.
            JsonSchema refSchema = this.schema.getSchema();
            if (refSchema == null) {
                throw new JsonSchemaException("Unable to resolve ref");
            }
            refSchema.getValidationContext().setConfig(this.parentSchema.getValidationContext().getConfig());
            if (this.schema != null) {
                errors =  this.schema.validate(executionContext, node, rootNode, instanceLocation);
            } else {
                errors = Collections.emptySet();
            }
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

        Set<ValidationMessage> errors = new HashSet<>();

        Scope parentScope = collectorContext.enterDynamicScope();
        try {
            debug(logger, node, rootNode, instanceLocation);
            // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
            // these schemas will be cached along with config. We have to replace the config for cached $ref references
            // with the latest config. Reset the config.
            JsonSchema refSchema = this.schema.getSchema();
            if (refSchema == null) {
                throw new JsonSchemaException("Unable to resolve ref");
            }
            refSchema.getValidationContext().setConfig(this.parentSchema.getValidationContext().getConfig());
            if (refSchema != null) {
                errors = refSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            }
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
