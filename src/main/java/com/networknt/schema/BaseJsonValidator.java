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

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class BaseJsonValidator implements JsonValidator {
    protected String schemaPath;
    protected JsonNode schemaNode;
    protected JsonSchema parentSchema;
    private boolean suppressSubSchemaRetrieval;
    private ValidatorTypeCode validatorType;
    private ErrorMessageType errorMessageType;
    /**
     * SchemaValidatorsConfig can only get and set in validationContext
     */
    protected SchemaValidatorsConfig config;
    protected final boolean failFast;

    /**
     * ThreadLocal to allow to pass state in recursive validator calls
     */
    protected final static ThreadLocal<ValidatorState> validatorState = new ThreadLocal<ValidatorState>();

    public BaseJsonValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                             ValidatorTypeCode validatorType, ValidationContext validationContext) {
        this(schemaPath, schemaNode, parentSchema, validatorType, false,
                validationContext.getConfig() != null && validationContext.getConfig().isFailFast());
        this.config = validationContext.getConfig() == null ? new SchemaValidatorsConfig() : validationContext.getConfig();
    }

    public BaseJsonValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                             ValidatorTypeCode validatorType, boolean suppressSubSchemaRetrieval, boolean failFast) {

        this.errorMessageType = validatorType;
        this.schemaPath = schemaPath;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.validatorType = validatorType;
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
        this.failFast = failFast;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public JsonNode getSchemaNode() {
        return schemaNode;
    }

    public JsonSchema getParentSchema() {
        return parentSchema;
    }

    protected JsonSchema fetchSubSchemaNode(ValidationContext validationContext) {
        return suppressSubSchemaRetrieval ? null : obtainSubSchemaNode(schemaNode, validationContext);
    }


    private static JsonSchema obtainSubSchemaNode(final JsonNode schemaNode, final ValidationContext validationContext) {
        final JsonNode node = schemaNode.get("id");
        if (node == null) return null;
        if (node.equals(schemaNode.get("$schema"))) return null;

        final String text = node.textValue();
        if (text == null) {
            return null;
        } else {
            final URI uri;
            try {
                uri = validationContext.getURIFactory().create(node.textValue());
            } catch (IllegalArgumentException e) {
                return null;
            }
            return validationContext.getJsonSchemaFactory().getSchema(uri, validationContext.getConfig());
        }
    }

    public Set<ValidationMessage> validate(JsonNode node) {
        return validate(node, node, AT_ROOT);
    }

    protected boolean equals(double n1, double n2) {
        return Math.abs(n1 - n2) < 1e-12;
    }

    protected boolean greaterThan(double n1, double n2) {
        return n1 - n2 > 1e-12;
    }

    protected boolean lessThan(double n1, double n2) {
        return n1 - n2 < -1e-12;
    }

    protected void parseErrorCode(String errorCodeKey) {
        JsonNode errorCodeNode = getParentSchema().getSchemaNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            String errorCodeText = errorCodeNode.asText();
            if (StringUtils.isNotBlank(errorCodeText)) {
                errorMessageType = CustomErrorMessageType.of(errorCodeText);
            }
        }
    }


    protected ValidationMessage buildValidationMessage(String at, String... arguments) {
        final ValidationMessage message = ValidationMessage.of(getValidatorType().getValue(), errorMessageType, at, arguments);
        if (failFast) {
            throw new JsonSchemaException(message);
        }
        return message;
    }

    protected void debug(Logger logger, JsonNode node, JsonNode rootNode, String at) {
        if (logger.isDebugEnabled()) {
            logger.debug("validate( " + node + ", " + rootNode + ", " + at + ")");
        }
    }

    protected ValidatorTypeCode getValidatorType() {
        return validatorType;
    }

    protected String getNodeFieldType() {
        JsonNode typeField = this.getParentSchema().getSchemaNode().get("type");
        if (typeField != null) {
            return typeField.asText();
        }
        return null;
    }
    
	/**
	 * This is default implementation of walk method. Its job is to call the
	 * validate method if shouldValidateSchema is enabled.
	 */
	@Override
	public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
		Set<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
		if (shouldValidateSchema) {
			validationMessages = validate(node, rootNode, at);
		}
		return validationMessages;
	}

}
