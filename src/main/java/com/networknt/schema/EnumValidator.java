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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;

public class EnumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(EnumValidator.class);

    private final Set<JsonNode> nodes;
    private final String error;

    public EnumValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
            super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ENUM, validationContext);

        if (schemaNode != null && schemaNode.isArray()) {
            nodes = new HashSet<JsonNode>();
            StringBuilder sb = new StringBuilder();

            sb.append('[');
            String separator = "";

            for (JsonNode n : schemaNode) {
                nodes.add(n);

                sb.append(separator);
                sb.append(n.asText());
                separator = ", ";
            }

            sb.append(']');

            error = sb.toString();
        } else {
            nodes = Collections.emptySet();
            error = "[none]";
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        if (!nodes.contains(node) && !(config.isTypeLoose() && isTypeLooseContainsInEnum(node))) {
            errors.add(buildValidationMessage(at, error));
        }

        return Collections.unmodifiableSet(errors);
    }

    /**
     * Check whether enum contains the value of the JsonNode if the typeLoose is enabled.
     * @param node JsonNode to check
     */
    private boolean isTypeLooseContainsInEnum(JsonNode node) {
        if (TypeFactory.getValueNodeType(node) == JsonType.STRING) {
            String nodeText = node.textValue();
            for (JsonNode n : nodes) {
                String value = n.asText();
                if (value != null && value.equals(nodeText)) {
                    return true;
                }
            }
        }
        return false;
    }

}
