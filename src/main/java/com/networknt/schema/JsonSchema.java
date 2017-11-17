/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is the core of json constraint implementation. It parses json constraint
 * file and generates JsonValidators. The class is thread safe, once it is
 * constructed, it can be used to validate multiple json data concurrently.
 */
public class JsonSchema extends BaseJsonValidator {
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");
    protected final Map<String, JsonValidator> validators;
    private final ValidationContext validationContext;

    JsonSchema(ValidationContext validationContext,  JsonNode schemaNode) {
        this(validationContext,  "#", schemaNode, null);
    }

    JsonSchema(ValidationContext validationContext,  String schemaPath, JsonNode schemaNode,
               JsonSchema parent) {
        this(validationContext,  schemaPath, schemaNode, parent, obainSubSchemaNode(schemaNode, validationContext));
    }

    JsonSchema(ValidationContext validatorFactory,  String schemaPath, JsonNode schemaNode,
               JsonSchema parent, JsonSchema subSchema) {
        super(schemaPath, schemaNode, parent, null, subSchema);
        this.validationContext = validatorFactory;
        this.validators = Collections.unmodifiableMap(this.read(schemaNode));
    }

    public JsonSchema(ValidationContext validationContext,  JsonNode schemaNode, JsonSchema subSchema) {
        this(validationContext, "#", schemaNode, null, subSchema);
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
                if (node == null && schema.hasSubSchema()){
                    node = schema.getSubSchema().getRefSchemaNode(ref);
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

            Optional<JsonValidator> validator = validationContext.newValidator(getSchemaPath(), pname, n, this);
            if (validator.isPresent()) {
                validators.put(getSchemaPath() + "/" + pname, validator.get());
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

}
