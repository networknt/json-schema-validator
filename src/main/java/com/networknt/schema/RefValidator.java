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
import com.networknt.schema.uri.URNURIFactory;
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
    private static final String URN_SCHEME = URNURIFactory.SCHEME;

    public RefValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.REF, validationContext);
        String refValue = schemaNode.asText();
        this.parentSchema = parentSchema;
        this.schema = getRefSchema(parentSchema, validationContext, refValue, evaluationPath);
        if (this.schema == null) {
            ValidationMessage validationMessage = ValidationMessage.builder().type(ValidatorTypeCode.REF.getValue())
                    .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                    .instanceLocation(schemaLocation.getFragment()).evaluationPath(schemaLocation.getFragment()).arguments(refValue).build();
            throw new JsonSchemaException(validationMessage);
        }
    }

    static JsonSchemaRef getRefSchema(JsonSchema parentSchema, ValidationContext validationContext, String refValue,
            JsonNodePath evaluationPath) {
        // The evaluationPath is used to derive the keywordLocation
        final String refValueOriginal = refValue;

        JsonSchema parent = parentSchema;
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
            URI schemaUri = determineSchemaUri(validationContext.getURIFactory(), parent, refUri);
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
            } else if (URN_SCHEME.equals(schemaUri.getScheme())) {
                // Try to resolve URN schema as a JsonSchemaRef to some sub-schema of the parent
                JsonSchemaRef ref = getJsonSchemaRef(parent, validationContext, schemaUri.toString(), refValueOriginal, evaluationPath);
                if (ref != null) {
                    return ref;
                }
            }

            // This should retrieve schemas regardless of the protocol that is in the uri.
            parent = validationContext.getJsonSchemaFactory().getSchema(schemaUri, validationContext.getConfig());

            if (index < 0) {
                return new JsonSchemaRef(parent.findAncestor());
            }
            refValue = refValue.substring(index);
        }
        if (refValue.equals(REF_CURRENT)) {
            return new JsonSchemaRef(parent.findAncestor());
        }
        return getJsonSchemaRef(parent, validationContext, refValue, refValueOriginal, evaluationPath);
    }

    private static JsonSchemaRef getJsonSchemaRef(JsonSchema parent,
                                                  ValidationContext validationContext,
                                                  String refValue,
                                                  String refValueOriginal,
                                                  JsonNodePath evaluationPath) {
        JsonNode node = parent.getRefSchemaNode(refValue);
        if (node != null) {
            JsonSchemaRef ref = validationContext.getReferenceParsingInProgress(refValueOriginal);
            if (ref == null) {
                SchemaLocation path = null;
                if (refValue.startsWith(REF_CURRENT)) {
                    // relative
                    path = parent.schemaLocation;
                    JsonNodePath fragment = new JsonNodePath(PathType.JSON_POINTER);
                    String[] parts = refValue.split("/");
                    for (int x = 1; x < parts.length; x++) {
                        fragment = fragment.append(parts[x]);
                    }
                    path = new SchemaLocation(parent.schemaLocation.getAbsoluteIri(), fragment);
                } else {
                    // absolute
                    path = SchemaLocation.of(refValue); 
                }
                final JsonSchema schema = validationContext.newSchema(path, evaluationPath, node, parent);
                ref = new JsonSchemaRef(schema);
                validationContext.setReferenceParsingInProgress(refValueOriginal, ref);
            }
            return ref;
        }
        return null;
    }

    private static URI determineSchemaUri(final URIFactory uriFactory, final JsonSchema parentSchema, final String refUri) {
        URI schemaUri;
        final URI currentUri = parentSchema.getCurrentUri();
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
            this.schema.getSchema().getValidationContext().setConfig(this.parentSchema.getValidationContext().getConfig());
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
            this.schema.getSchema().getValidationContext().setConfig(this.parentSchema.getValidationContext().getConfig());
            if (this.schema != null) {
                errors = this.schema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
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
        this.schema.getSchema().initializeValidators();
    }
}
