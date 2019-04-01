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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(PatternValidator.class);

    private String pattern;
    private Pattern p;

    public PatternValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN, validationContext);
        pattern = "";
        if (schemaNode != null && schemaNode.isTextual()) {
            pattern = schemaNode.textValue();
            p = Pattern.compile(pattern);
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }

        if (p != null) {
            try {
                Matcher m = p.matcher(node.asText());
                if (!m.find()) {
                    return Collections.singleton(buildValidationMessage(at, pattern));
                }
            } catch (PatternSyntaxException pse) {
                logger.error("Failed to apply pattern on " + at + ": Invalid syntax [" + pattern + "]", pse);
            }
        }

        return Collections.emptySet();
    }

}
