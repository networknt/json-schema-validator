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
import com.networknt.schema.url.URLFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public abstract class BaseJsonValidator implements JsonValidator {
    private String schemaPath;
    private JsonNode schemaNode;
    private JsonSchema parentSchema;
    private boolean suppressSubSchemaRetrieval;
    private ValidatorTypeCode validatorType;
    private ErrorMessageType errorMessageType;

    
    public BaseJsonValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                             ValidatorTypeCode validatorType, ValidationContext validationContext) {
    	this(schemaPath, schemaNode, parentSchema, validatorType, false );
    }

    public BaseJsonValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                             ValidatorTypeCode validatorType, boolean suppressSubSchemaRetrieval) {
        this.errorMessageType = validatorType;
        this.schemaPath = schemaPath;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.validatorType = validatorType;
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
    }

    protected String getSchemaPath() {
        return schemaPath;
    }

    public JsonNode getSchemaNode() {
        return schemaNode;
    }

    protected JsonSchema getParentSchema() {
        return parentSchema;
    }
    
    protected JsonSchema fetchSubSchemaNode(ValidationContext validationContext) {
        return suppressSubSchemaRetrieval ? null : obtainSubSchemaNode(schemaNode, validationContext);
    }

    
    private static JsonSchema obtainSubSchemaNode(JsonNode schemaNode, ValidationContext validationContext){
        JsonNode node = schemaNode.get("id");
        if(node == null) return null;
    	if(node.equals(schemaNode.get("$schema"))) return null;

        try {
            String text = node.textValue();
            if (text == null) {
                return null;
            }
            else {
                URL url = URLFactory.toURL(node.textValue());
                return validationContext.getJsonSchemaFactory().getSchema(url);
            }
        } catch (MalformedURLException e) {
            return null;
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
        return ValidationMessage.of(getValidatorType().getValue(), errorMessageType, at, arguments);
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
}
