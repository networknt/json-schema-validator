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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

public class RefValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    protected JsonSchemaRef schema;

    private static final String REF_CURRENT = "#";

    public RefValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.REF, validationContext);
        String refValue = schemaNode.asText();
        schema = getRefSchema(parentSchema, validationContext, refValue);
        if (schema == null) {
            throw new JsonSchemaException(ValidationMessage.of(ValidatorTypeCode.REF.getValue(), CustomErrorMessageType.of("internal.unresolvedRef", new MessageFormat("{0}: Reference {1} cannot be resolved")), schemaPath, refValue));
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
                return null;
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
                    ref = new JsonSchemaRef(validationContext, refValue);
                    validationContext.setReferenceParsingInProgress(refValueOriginal, ref);
                    JsonSchema ret = new JsonSchema(validationContext, refValue, parentSchema.getCurrentUri(), node, parentSchema)
                        .initialize();
                    ref.set(ret);
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

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (schema != null) {
            return schema.validate(node, rootNode, at);
        } else {
            return Collections.emptySet();
        }
    }

}
