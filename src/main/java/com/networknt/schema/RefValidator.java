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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.url.URLFactory;

public class RefValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    protected JsonSchema schema;
    
    private static final String REF_CURRENT = "#";

    public RefValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.REF, validationContext);
        String refValue = schemaNode.asText();
        schema = getRefSchema(parentSchema, validationContext, refValue);
        if (schema == null) {
            throw new JsonSchemaException(ValidationMessage.of(ValidatorTypeCode.REF.getValue(), CustomErrorMessageType.of("internal.unresolvedRef", new MessageFormat("{0}: Reference {1} cannot be resolved")), schemaPath, refValue));
        }
    }

    static JsonSchema getRefSchema(JsonSchema parentSchema, ValidationContext validationContext, String refValue) {
        if (!refValue.startsWith(REF_CURRENT)) {
            // This will be the url extracted from the refValue (this may be a relative or absolute Url).
            final String refUrl;
            final int index = refValue.indexOf(REF_CURRENT);
            if (index > 0) {
                refUrl = refValue.substring(0, index);
            } else {
                refUrl = refValue;
            }
            
            // This will determine the correct absolute url for the refUrl. This decision will take into
            // account the current url of the parent schema.
            URL schemaUrl = determineSchemaUrl(parentSchema, refUrl);
            if (schemaUrl == null) {
              return null;
            }
            
            // This should retrieve schemas regardless of the protocol that is in the url.
            parentSchema = validationContext.getJsonSchemaFactory().getSchema(schemaUrl, validationContext.getConfig());
            
            if (index < 0) {
                return parentSchema.findAncestor();
            } else {
                refValue = refValue.substring(index);
            }
        }
        if (refValue.equals(REF_CURRENT)) {
            return parentSchema.findAncestor();
        } else {
            JsonNode node = parentSchema.getRefSchemaNode(refValue);
            if (node != null) {
                return new JsonSchema(validationContext, refValue, parentSchema.getCurrentUrl(), node, parentSchema);
            }
        }
        return null;
    }

    private static URL determineSchemaUrl(JsonSchema parentSchema, String refUrl) {
        URL schemaUrl;
        try {
            // If the refUrl is an absolute url, then this will succeed.
            schemaUrl = URLFactory.toURL(refUrl);
        } catch (MalformedURLException e) {
            try {
                // If the refUrl is a valid relative url in the context of the parent schema's url,
                // then this will succeed.
                schemaUrl = URLFactory.toURL(parentSchema.getCurrentUrl(), refUrl);
            } catch (MalformedURLException e2) {
                // We are unable to resolve the reference at this point.
                schemaUrl = null;
            }
        }
        return schemaUrl;
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
