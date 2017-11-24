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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReadOnlyValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private List<String> fieldNames = new ArrayList<String>();

    public ReadOnlyValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.READ_ONLY, validationContext);
        if (schemaNode.isArray()) {
            int size = schemaNode.size();
            for (int i = 0; i < size; i++) {
                fieldNames.add(schemaNode.get(i).asText());
            }
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);
            String datapath = "";
            if (at.equals("$")) {
                datapath = datapath + "#original." + fieldName;
            } else {
                datapath = datapath + "#original." + at.substring(2) + "." + fieldName;
            }
            JsonNode originalNode = getNode(datapath, rootNode);

            boolean theSame = propertyNode != null && originalNode != null && propertyNode.equals(originalNode);
            if (!theSame) {
                errors.add(buildValidationMessage(at));
            }
        }

        return Collections.unmodifiableSet(errors);
    }

    private JsonNode getNode(String datapath, JsonNode data) {
        String path = datapath;
        if (path.startsWith("$.")) {
            path = path.substring(2);
        }

        String[] parts = path.split("\\.");
        JsonNode result = null;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("[")) {
                int idx1 = parts[i].indexOf("[");
                int idx2 = parts[i].indexOf("]");
                String key = parts[i].substring(0, idx1).trim();
                int idx = Integer.parseInt(parts[i].substring(idx1 + 1, idx2).trim());
                result = data.get(key).get(idx);
            } else {
                result = data.get(parts[i]);
            }
            if (result == null) {
                break;
            }
            data = result;
        }
        return result;
    }

}
