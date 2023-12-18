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

public class DependentSchemas extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DependentSchemas.class);
    private final Map<String, JsonSchema> schemaDependencies = new HashMap<>();

    public DependentSchemas(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {

        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.DEPENDENT_SCHEMAS, validationContext);

        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            JsonNode pvalue = schemaNode.get(pname);
            if (pvalue.isObject() || pvalue.isBoolean()) {
                this.schemaDependencies.put(pname, validationContext.newSchema(schemaLocation.resolve(pname),
                        evaluationPath.resolve(pname), pvalue, parentSchema));
            }
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<>();

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            JsonSchema schema = this.schemaDependencies.get(pname);
            if (schema != null) {
                errors.addAll(schema.validate(executionContext, node, rootNode, at));
            }
        }

        return Collections.unmodifiableSet(errors);
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemaDependencies.values());
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(executionContext, node, rootNode, at);
        }
        for (JsonSchema schema : this.schemaDependencies.values()) {
            schema.walk(executionContext, node, rootNode, at, false);
        }
        return Collections.emptySet();
    }

}
