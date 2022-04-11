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
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.urn.URNFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.MessageFormat;
import java.util.*;

public class RefValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    protected JsonSchemaRef schema;

    private JsonSchema parentSchema;

    private static final String REF_CURRENT = "#";

    public RefValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.REF, validationContext);
        String refValue = schemaNode.asText();
        this.parentSchema = parentSchema;
        schema = getRefSchema(parentSchema, validationContext, refValue);
        if (schema == null) {
            throw new JsonSchemaException(
                    ValidationMessage.of(ValidatorTypeCode.REF.getValue(),
                                                               CustomErrorMessageType.of("internal.unresolvedRef", new MessageFormat("{0}: Reference {1} cannot be resolved")), schemaPath, schemaPath, refValue));
        }
    }

    static JsonSchemaRef getRefSchema(JsonSchema parentSchema, ValidationContext validationContext, String refValue) {
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

            // This should retrieve schemas regardless of the protocol that is in the uri.
            parentSchema = validationContext.getJsonSchemaFactory().getSchema(schemaUri, validationContext.getConfig());

            if (index < 0) {
                return new JsonSchemaRef(parentSchema.findAncestor());
            } else {
                refValue = refValue.substring(index);
            }
        }
        if (refValue.equals(REF_CURRENT)) {
            return new JsonSchemaRef(parentSchema.findAncestor());
        } else {
            JsonNode node = parentSchema.getRefSchemaNode(refValue);
            if (node != null) {
                JsonSchemaRef ref = validationContext.getReferenceParsingInProgress(refValueOriginal);
                if (ref == null) {
                    final JsonSchema schema = new JsonSchema(validationContext, refValue, parentSchema.getCurrentUri(), node, parentSchema);
                    ref = new JsonSchemaRef(schema);
                    validationContext.setReferenceParsingInProgress(refValueOriginal, ref);
                }
                return ref;
            }
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

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {

        Set<ValidationMessage> errors = new HashSet<>();

        // As ref will contain a schema take a backup of evaluatedProperties.
        Object backupEvaluatedProperties = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);

        // Make the evaluatedProperties list empty.
        CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, new ArrayList<>());

        try {
            debug(logger, node, rootNode, at);
            // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
            // these schemas will be cached along with config. We have to replace the config for cached $ref references
            // with the latest config. Reset the config.
            schema.getSchema().getValidationContext().setConfig(parentSchema.getValidationContext().getConfig());
            if (schema != null) {
                errors =  schema.validate(node, rootNode, at);
            } else {
                errors = Collections.emptySet();
            }
        } finally {
            if (errors.isEmpty()) {
                List<String> backupEvaluatedPropertiesList = (backupEvaluatedProperties == null ? new ArrayList<>() : (List<String>) backupEvaluatedProperties);
                backupEvaluatedPropertiesList.addAll((List<String>) CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES));
                CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedPropertiesList);
            } else {
                CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedProperties);
            }
        }
        return errors;
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {

        Set<ValidationMessage> errors = new HashSet<>();

        // As ref will contain a schema take a backup of evaluatedProperties.
        Object backupEvaluatedProperties = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);

        // Make the evaluatedProperties list empty.
        CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, new ArrayList<>());
        try {
            debug(logger, node, rootNode, at);
            // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
            // these schemas will be cached along with config. We have to replace the config for cached $ref references
            // with the latest config. Reset the config.
            schema.getSchema().getValidationContext().setConfig(parentSchema.getValidationContext().getConfig());
            if (schema != null) {
                errors = schema.walk(node, rootNode, at, shouldValidateSchema);
            }
            return errors;
        } finally {
            if (shouldValidateSchema) {
                if (errors.isEmpty()) {
                    List<String> backupEvaluatedPropertiesList = (backupEvaluatedProperties == null ? new ArrayList<>() : (List<String>) backupEvaluatedProperties);
                    backupEvaluatedPropertiesList.addAll((List<String>) CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES));
                    CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedPropertiesList);
                } else {
                    CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedProperties);
                }
            }
        }
    }

	public JsonSchemaRef getSchemaRef() {
		return schema;
	}


    @Override
    public void preloadJsonSchema() {
        schema.getSchema().initializeValidators();
    }
}
