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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.url.URLFactory;

/**
 * This is the core of json constraint implementation. It parses json constraint
 * file and generates JsonValidators. The class is thread safe, once it is
 * constructed, it can be used to validate multiple json data concurrently.
 */
public class JsonSchema extends BaseJsonValidator {
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");
    protected final Map<String, JsonValidator> validators;
    private final ValidationContext validationContext;
    
    /**
     * This is the current url of this schema. This url could refer to the url of this schema's file
     * or it could potentially be a url that has been altered by an id. An 'id' is able to completely overwrite
     * the current url or add onto it. This is necessary so that '$ref's are able to be relative to a
     * combination of the current schema file's url and 'id' urls visible to this schema.
     * 
     * This can be null. If it is null, then the creation of relative urls will fail. However, an absolute
     * 'id' would still be able to specify an absolute url.
     */
    private final URL currentUrl;
    
    private JsonValidator requiredValidator = null;

    public JsonSchema(ValidationContext validationContext, URL baseUrl, JsonNode schemaNode) {
        this(validationContext, "#", baseUrl, schemaNode, null);
    }

    public JsonSchema(ValidationContext validationContext, String schemaPath, URL currentUrl, JsonNode schemaNode,
               JsonSchema parent) {
        this(validationContext,  schemaPath, currentUrl, schemaNode, parent, false);
    }

    public JsonSchema(ValidationContext validationContext, URL baseUrl, JsonNode schemaNode, boolean suppressSubSchemaRetrieval) {
        this(validationContext, "#", baseUrl, schemaNode, null, suppressSubSchemaRetrieval);
    }

    private JsonSchema(ValidationContext validationContext,  String schemaPath, URL currentUrl, JsonNode schemaNode,
               JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        super(schemaPath, schemaNode, parent, null, suppressSubSchemaRetrieval);
        this.validationContext = validationContext;
        this.config = validationContext.getConfig();
        this.currentUrl = this.combineCurrentUrlWithIds(currentUrl, schemaNode);
        this.validators = Collections.unmodifiableMap(this.read(schemaNode));
    }
    
    private URL combineCurrentUrlWithIds(URL currentUrl, JsonNode schemaNode) {
      final JsonNode idNode = schemaNode.get("id");
      if (idNode == null) {
        return currentUrl;
      } else {
        try
        {
          return URLFactory.toURL(currentUrl, idNode.asText());
        }
        catch (MalformedURLException e)
        {
          throw new IllegalArgumentException(String.format("Invalid 'id' in schema: %s", schemaNode), e);
        }
      }
    }
    
    public URL getCurrentUrl()
    {
      return this.currentUrl;
    }

    /**
     * Find the schema node for $ref attribute.
     *
     * @param ref String
     * @return JsonNode
     */
    public JsonNode getRefSchemaNode(String ref) {
        JsonSchema schema = findAncestor();
        JsonNode node = schema.getSchemaNode();

        if (ref.startsWith("#/")) {
            // handle local ref
            String[] keys = ref.substring(2).split("/");
            for (String key : keys) {
                try {
                    key = URLDecoder.decode(key, "utf-8");
                } catch (UnsupportedEncodingException e) {
                }
                Matcher matcher = intPattern.matcher(key);
                if (matcher.matches()) {
                    node = node.get(Integer.parseInt(key));
                } else {
                    node = node.get(key);
                }
                if (node == null){
                    JsonSchema subSchema = schema.fetchSubSchemaNode(validationContext);
                    if (subSchema != null) {
                        node = subSchema.getRefSchemaNode(ref);
                    }
                }
                if (node == null){
                    break;
                }
            }
        }
        return node;
    }

    public JsonSchema findAncestor() {
        JsonSchema ancestor = this;
        if (this.getParentSchema() != null) {
            ancestor = this.getParentSchema().findAncestor();
        }
        return ancestor;
    }

    private Map<String, JsonValidator> read(JsonNode schemaNode) {
        Map<String, JsonValidator> validators = new HashMap<String, JsonValidator>();
        Iterator<String> pnames = schemaNode.fieldNames();
        while (pnames.hasNext()) {
            String pname = pnames.next();
            JsonNode n = schemaNode.get(pname);

            JsonValidator validator = validationContext.newValidator(getSchemaPath(), pname, n, this);
            if (validator != null) {
                validators.put(getSchemaPath() + "/" + pname, validator);
                
                if(pname.equals("required"))
                	requiredValidator = validator;
            }

        }
        return validators;
    }

    public Set<ValidationMessage> validate(JsonNode jsonNode, JsonNode rootNode, String at) {
        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        for (JsonValidator v : validators.values()) {
            errors.addAll(v.validate(jsonNode, rootNode, at));
        }
        return errors;
    }

    @Override
    public String toString() {
        return "\"" + getSchemaPath() + "\" : " + getSchemaNode().toString();
    }

    public boolean hasRequiredValidator() {
    	return requiredValidator != null ? true : false;
    }
    
	public JsonValidator getRequiredValidator() {
		return requiredValidator;
	}
}
