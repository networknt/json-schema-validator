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

import java.util.*;

/**
 * {@link JsonValidator} for required.
 */
public class RequiredValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private final List<String> fieldNames;

    public RequiredValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.REQUIRED, validationContext);
        if (schemaNode.isArray()) {
            this.fieldNames = new ArrayList<>(schemaNode.size());
            for (JsonNode fieldNme : schemaNode) {
                fieldNames.add(fieldNme.asText());
            }
        } else {
            this.fieldNames = Collections.emptyList();
        }
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (!node.isObject()) {
            return Collections.emptySet();
        }

        Set<ValidationMessage> errors = null;

        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);

            if (propertyNode == null) {
                if (errors == null) {
                    errors = new LinkedHashSet<>();
                }
                /**
                 * Note that for the required validation the instanceLocation does not contain the missing property
                 * <p>
                 * @see <a href="https://json-schema.org/draft/2020-12/draft-bhutton-json-schema-01#name-basic">Basic</a>
                 */
                errors.add(message().instanceNode(node).property(fieldName).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .failFast(executionContext.isFailFast()).arguments(fieldName).build());
            }
        }

        return errors == null ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

}
