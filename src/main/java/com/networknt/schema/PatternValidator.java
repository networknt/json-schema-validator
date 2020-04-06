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
import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;
import org.joni.exception.SyntaxException;
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
    private Pattern compiledPattern;
    private Regex compiledRegex;

    public PatternValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN, validationContext);
        pattern = "";
        if (schemaNode != null && schemaNode.isTextual()) {
            pattern = schemaNode.textValue();
            try {
                compileRegexPattern(pattern, validationContext.getConfig() != null && validationContext.getConfig().isEcma262Validator());
            } catch (PatternSyntaxException pse) {
                logger.error("Failed to compile pattern : Invalid syntax [" + pattern + "]", pse);
                throw pse;
            } catch (SyntaxException se) {
                logger.error("Failed to compile pattern : Invalid syntax [" + pattern + "]", se);
                throw se;
            }
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    private void compileRegexPattern(String regex, boolean useEcma262Validator) {
        if (useEcma262Validator) {
            byte[] regexBytes = regex.getBytes();
            this.compiledRegex = new Regex(regexBytes, 0, regexBytes.length, Option.NONE, UTF8Encoding.INSTANCE, Syntax.ECMAScript);
        } else {
            compiledPattern = Pattern.compile(pattern);
        }
    }

    private boolean matches(String value) {
        if (compiledRegex == null && compiledPattern == null) {
            return true;
        }

        if (compiledPattern == null) {
            byte[] bytes = value.getBytes();
            return compiledRegex.matcher(bytes).search(0, bytes.length, Option.NONE) >= 0;
        } else {
            return compiledPattern.matcher(value).find();
        }

    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }

        try {
            if (!matches(node.asText())) {
                return Collections.singleton(buildValidationMessage(at, pattern));
            }
        } catch (PatternSyntaxException pse) {
            logger.error("Failed to apply pattern on " + at + ": Invalid syntax [" + pattern + "]", pse);
        }

        return Collections.emptySet();
    }
}
