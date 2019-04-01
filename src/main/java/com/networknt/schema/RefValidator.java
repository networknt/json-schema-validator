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

import java.io.InputStream;
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
    
    private static final String REF_DOMAIN = "/";
    private static final String REF_CURRENT = "#";
    private static final String REF_RELATIVE = "../";

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
            // handle remote ref
            String schemaUrl = refValue;
        	    int index = refValue.indexOf(REF_CURRENT);
            if (index > 0) {
                schemaUrl = schemaUrl.substring(0, index);
            }
            if(isRelativePath(schemaUrl)){
                schemaUrl = obtainAbsolutePath(parentSchema, schemaUrl);
            }
            
            try {
                URL url = URLFactory.toURL(schemaUrl);
                parentSchema = validationContext.getJsonSchemaFactory().getSchema(url);
            } catch (MalformedURLException e) {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaUrl);
                parentSchema = validationContext.getJsonSchemaFactory().getSchema(is);
            }
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
                return new JsonSchema(validationContext, refValue, node, parentSchema);
            }
        }
        return null;
    }
    
    private static boolean isRelativePath(String schemaUrl) {
    	return !schemaUrl.startsWith("http");
    }
    
    private static String obtainAbsolutePath(JsonSchema parentSchema, String schemaUrl) {
    	String baseSchemaUrl = parentSchema.findAncestor().getSchemaNode().get("id").textValue();
		int index = baseSchemaUrl.lastIndexOf("/");
		baseSchemaUrl = baseSchemaUrl.substring(0, index);
		
		String schemaRef = schemaUrl;
		
		if(schemaRef.startsWith(REF_DOMAIN)){
			// from domain add ref
			try {
				URL url = URLFactory.toURL(baseSchemaUrl);
				baseSchemaUrl = url.getProtocol()+"//"+url.getHost();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}else if(schemaRef.startsWith(REF_RELATIVE)){
			// relative from schema
			while(schemaRef.startsWith(REF_RELATIVE)){
				index = baseSchemaUrl.lastIndexOf("/");
				baseSchemaUrl = baseSchemaUrl.substring(0, index);
				schemaRef = schemaRef.replaceFirst(REF_RELATIVE, "");
			}
		}
		schemaRef = baseSchemaUrl +"/"+ schemaRef;
		return schemaRef;
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
