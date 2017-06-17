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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the core of json constraint implementation. It parses json constraint
 * file and generates JsonValidators. The class is thread safe, once it is
 * constructed, it can be used to validate multiple json data concurrently.
 */
public class JsonSchema extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(JsonSchema.class);
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");
    protected Map<String, JsonValidator> validators;
    private ObjectMapper mapper;

    JsonSchema(ObjectMapper mapper, JsonNode schemaNode) {
        this(mapper, "#", schemaNode, null);
    }

    JsonSchema(ObjectMapper mapper, String schemaPath, JsonNode schemaNode,
               JsonSchema parent) {
        super(schemaPath, schemaNode, parent, null);
        this.init(mapper, schemaNode);
    }

    JsonSchema(ObjectMapper mapper, String schemaPath, JsonNode schemaNode,
               JsonSchema parent, JsonSchema subSchema) {
        super(schemaPath, schemaNode, parent, null, subSchema);
        this.init(mapper, schemaNode);
    }

    public JsonSchema(ObjectMapper mapper, JsonNode schemaNode, JsonSchema subSchema) {
        this(mapper, "#", schemaNode, null, subSchema);
    }

    private void init(ObjectMapper mapper, JsonNode schemaNode) {
        this.mapper = mapper;
        this.validators = new LinkedHashMap<String, JsonValidator>();
        this.read(schemaNode);
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

    @SuppressWarnings("unchecked")
    private void read(JsonNode schemaNode) {
        Iterator<String> pnames = schemaNode.fieldNames();
        while (pnames.hasNext()) {
            String pname = pnames.next();
            JsonNode n = schemaNode.get(pname);

            String shortClassName = pname;
            if (shortClassName.startsWith("$")) {
                // remove "$" from class name for $ref schema
                shortClassName = shortClassName.substring(1);
            }

            try {
                ValidatorTypeCode.fromValue(shortClassName);

                String className = Character.toUpperCase(shortClassName.charAt(0))
                        + shortClassName.substring(1) + "Validator";
                Class<JsonValidator> clazz = (Class<JsonValidator>) Class
                        .forName("com.networknt.schema." + className);
                Constructor<JsonValidator> c = null;
                c = clazz.getConstructor(new Class[]{String.class,
                        JsonNode.class, JsonSchema.class, ObjectMapper.class});
                validators.put(getSchemaPath() + "/" + pname, c.newInstance(
                        getSchemaPath() + "/" + pname, n, this, mapper));
            } catch (IllegalArgumentException e) {
                // ignore unsupported schema node
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof JsonSchemaException) {
                    throw (JsonSchemaException) e.getTargetException();
                } else {
                    logger.info("Could not load validator " + pname);
                }
            } catch (Exception e) {
                logger.info("Could not load validator " + pname);
            }
        }
    }

    public Set<ValidationMessage> validate(JsonNode JsonNode,
                                           JsonNode rootNode, String at) {
        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();
        for (JsonValidator v : validators.values()) {
            errors.addAll(v.validate(JsonNode, rootNode, at));
        }
        return errors;
    }

    @Override
    public String toString() {
        return "\"" + getSchemaPath() + "\" : " + getSchemaNode().toString();
    }

}
