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

/**
 * {@link JsonValidator} for maxLength.
 */
public class MaxLengthValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MaxLengthValidator.class);

    private final int maxLength;

    public MaxLengthValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.MAX_LENGTH, validationContext);
        if (schemaNode != null && schemaNode.canConvertToExactIntegral()) {
            this.maxLength = schemaNode.intValue();
        } else {
            this.maxLength = Integer.MAX_VALUE;
        }
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            // ignore no-string typs
            return Collections.emptySet();
        }
        if (node.textValue().codePointCount(0, node.textValue().length()) > this.maxLength) {
            return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast()).arguments(this.maxLength).build());
        }
        return Collections.emptySet();
    }

}
