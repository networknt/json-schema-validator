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
import com.networknt.schema.regex.RegularExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PatternPropertiesValidator extends BaseJsonValidator {
    public static final String PROPERTY = "patternProperties";
    private static final Logger logger = LoggerFactory.getLogger(PatternPropertiesValidator.class);
    private final Map<RegularExpression, JsonSchema> schemas = new IdentityHashMap<>();

    public PatternPropertiesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema,
                                      ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN_PROPERTIES, validationContext);
        if (!schemaNode.isObject()) {
            throw new JsonSchemaException("patternProperties must be an object node");
        }
        Iterator<String> names = schemaNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            RegularExpression pattern = RegularExpression.compile(name, validationContext);
            schemas.put(pattern, validationContext.newSchema(schemaLocation.append(name), evaluationPath.append(name),
                    schemaNode.get(name), parentSchema));
        }
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        if (!node.isObject()) {
            return Collections.emptySet();
        }
        Set<ValidationMessage> errors = null;
        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            JsonNode n = node.get(name);
            for (Map.Entry<RegularExpression, JsonSchema> entry : schemas.entrySet()) {
                if (entry.getKey().matches(name)) {
                    JsonNodePath path = instanceLocation.append(name);
                    Set<ValidationMessage> results = entry.getValue().validate(executionContext, n, rootNode, path);
                    if (results.isEmpty()) {
                        if (executionContext.getExecutionConfig().getAnnotationAllowedPredicate().test(getKeyword())) {
                            executionContext.getCollectorContext().getEvaluatedProperties().add(path);
                        }
                    } else {
                        if (errors == null) {
                            errors = new LinkedHashSet<>();
                        }
                        errors.addAll(results);
                    }
                }
            }
        }
        return errors == null ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(schemas.values());
    }
}
