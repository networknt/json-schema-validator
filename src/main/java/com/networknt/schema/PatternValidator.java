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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;
import org.joni.exception.SyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class PatternValidator implements JsonValidator {

    private final JsonValidator delegate;

    public PatternValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
         if (validationContext.getConfig() != null && validationContext.getConfig().isEcma262Validator()) {
             delegate = new PatternValidatorEcma262(schemaPath, schemaNode, parentSchema, validationContext);
         } else {
             delegate = new PatternValidatorJava(schemaPath, schemaNode, parentSchema, validationContext);
         }
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode rootNode) {
        return delegate.validate(rootNode);
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        return delegate.validate(node, rootNode, at);
    }
    
	@Override
	public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
		Set<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
		if (shouldValidateSchema) {
			validationMessages.addAll(validate(node, rootNode, at));
		}
		validationMessages.addAll(delegate.walk(node, rootNode, at, shouldValidateSchema));
		return validationMessages;
	}

    private static class PatternValidatorJava extends BaseJsonValidator implements JsonValidator {
        private static final Logger logger = LoggerFactory.getLogger(PatternValidator.class);

        private String pattern;
        private Pattern compiledPattern;

        public PatternValidatorJava(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

            super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN, validationContext);
            pattern = "";
            if (schemaNode != null && schemaNode.isTextual()) {
                pattern = schemaNode.textValue();
                try {
                    compiledPattern = Pattern.compile(pattern);
                } catch (PatternSyntaxException pse) {
                    logger.error("Failed to compile pattern : Invalid syntax [" + pattern + "]", pse);
                    throw pse;
                }
            }

            parseErrorCode(getValidatorType().getErrorCodeKey());
        }

        private boolean matches(String value) {
            return compiledPattern == null || compiledPattern.matcher(value).find();
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

    private static class PatternValidatorEcma262 extends BaseJsonValidator implements JsonValidator {
        private static final Logger logger = LoggerFactory.getLogger(PatternValidator.class);

        private String pattern;
        private Regex compiledRegex;

        public PatternValidatorEcma262(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

            super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN, validationContext);
            pattern = "";
            if (schemaNode != null && schemaNode.isTextual()) {
                pattern = schemaNode.textValue();
                try {
                    compileRegexPattern(pattern, validationContext.getConfig() != null && validationContext.getConfig().isEcma262Validator());
                } catch (SyntaxException se) {
                    logger.error("Failed to compile pattern : Invalid syntax [" + pattern + "]", se);
                    throw se;
                }
            }

            parseErrorCode(getValidatorType().getErrorCodeKey());
        }

        private void compileRegexPattern(String regex, boolean useEcma262Validator) {
            byte[] regexBytes = regex.getBytes();
            this.compiledRegex = new Regex(regexBytes, 0, regexBytes.length, Option.NONE, UTF8Encoding.INSTANCE, Syntax.ECMAScript);
        }

        private boolean matches(String value) {
            if (compiledRegex == null) {
                return true;
            }

            byte[] bytes = value.getBytes();
            return compiledRegex.matcher(bytes).search(0, bytes.length, Option.NONE) >= 0;
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

}
