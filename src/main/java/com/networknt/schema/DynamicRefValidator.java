/*
 * Copyright (c) 2024 the original author or authors.
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
import java.util.function.Supplier;

/**
 * {@link JsonValidator} that resolves $dynamicRef.
 */
public class DynamicRefValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DynamicRefValidator.class);

    protected final JsonSchemaRef schema;

    public DynamicRefValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.DYNAMIC_REF, validationContext);
        String refValue = schemaNode.asText();
        this.schema = getRefSchema(parentSchema, validationContext, refValue, evaluationPath);
    }

    static JsonSchemaRef getRefSchema(JsonSchema parentSchema, ValidationContext validationContext, String refValue,
            JsonNodePath evaluationPath) {
        String ref = resolve(parentSchema, refValue);
        return new JsonSchemaRef(getSupplier(() -> {
            JsonSchema refSchema = validationContext.getDynamicAnchors().get(ref);
            if (refSchema == null) { // This is a $dynamicRef without a matching $dynamicAnchor
                // A $dynamicRef without a matching $dynamicAnchor in the same schema resource
                // behaves like a normal $ref to $anchor
                // A $dynamicRef without anchor in fragment behaves identical to $ref
                JsonSchemaRef r = RefValidator.getRefSchema(parentSchema, validationContext, refValue, evaluationPath);
                if (r != null) {
                    refSchema = r.getSchema();
                }
            } else {
                // Check parents
                JsonSchema base = parentSchema;
                int index = ref.indexOf("#");
                String anchor = ref.substring(index);
                String absoluteIri = ref.substring(0, index);
                while (base.getEvaluationParentSchema() != null) {
                    base = base.getEvaluationParentSchema();
                    String baseAbsoluteIri = base.getSchemaLocation().getAbsoluteIri() != null ? base.getSchemaLocation().getAbsoluteIri().toString() : "";
                    if (!baseAbsoluteIri.equals(absoluteIri)) {
                        absoluteIri = baseAbsoluteIri;
                        String parentRef = SchemaLocation.resolve(base.getSchemaLocation(), anchor);
                        JsonSchema parentRefSchema = validationContext.getDynamicAnchors().get(parentRef);
                        if (parentRefSchema != null) {
                            refSchema = parentRefSchema;
                        }
                    }
                }
            }
            
            if (refSchema != null) {
                refSchema = refSchema.fromRef(parentSchema, evaluationPath);
            }
            return refSchema;
        }, validationContext.getConfig().isCacheRefs()));
    }

    static <T> Supplier<T> getSupplier(Supplier<T> supplier, boolean cache) {
        return cache ? new CachedSupplier<>(supplier) : supplier;
    }

    private static String resolve(JsonSchema parentSchema, String refValue) {
        // $ref prevents a sibling $id from changing the base uri
        JsonSchema base = parentSchema;
        if (parentSchema.getId() != null && parentSchema.parentSchema != null) {
            base = parentSchema.parentSchema;
        }
        return SchemaLocation.resolve(base.getSchemaLocation(), refValue);
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);
        JsonSchema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            ValidationMessage validationMessage = message().type(ValidatorTypeCode.DYNAMIC_REF.getValue())
                    .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                    .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                    .arguments(schemaNode.asText()).build();
            throw new InvalidSchemaRefException(validationMessage);
        }
        return refSchema.validate(executionContext, node, rootNode, instanceLocation);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        debug(logger, executionContext, node, rootNode, instanceLocation);
        // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
        // these schemas will be cached along with config. We have to replace the config for cached $ref references
        // with the latest config. Reset the config.
        JsonSchema refSchema = this.schema.getSchema();
        if (refSchema == null) {
            ValidationMessage validationMessage = message().type(ValidatorTypeCode.DYNAMIC_REF.getValue())
                    .code("internal.unresolvedRef").message("{0}: Reference {1} cannot be resolved")
                    .instanceLocation(instanceLocation).evaluationPath(getEvaluationPath())
                    .arguments(schemaNode.asText()).build();
            throw new InvalidSchemaRefException(validationMessage);
        }
        if (node == null) {
            // Check for circular dependency
            SchemaLocation schemaLocation = refSchema.getSchemaLocation();
            JsonSchema check = refSchema;
            boolean circularDependency = false;
            while (check.getEvaluationParentSchema() != null) {
                check = check.getEvaluationParentSchema();
                if (check.getSchemaLocation().equals(schemaLocation)) {
                    circularDependency = true;
                    break;
                }
            }
            if (circularDependency) {
                return Collections.emptySet();
            }
        }
        return refSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
    }

	public JsonSchemaRef getSchemaRef() {
		return this.schema;
	}

    @Override
    public void preloadJsonSchema() {
        JsonSchema jsonSchema = null;
        try {
            jsonSchema = this.schema.getSchema();
        } catch (JsonSchemaException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new JsonSchemaException(e);
        }
        // Check for circular dependency
        // Only one cycle is pre-loaded
        // The rest of the cycles will load at execution time depending on the input
        // data
        SchemaLocation schemaLocation = jsonSchema.getSchemaLocation();
        JsonSchema check = jsonSchema;
        boolean circularDependency = false;
        int depth = 0;
        while (check.getEvaluationParentSchema() != null) {
            depth++;
            check = check.getEvaluationParentSchema();
            if (check.getSchemaLocation().equals(schemaLocation)) {
                circularDependency = true;
                break;
            }
        }
        if (this.validationContext.getConfig().isCacheRefs() && !circularDependency
                && depth < this.validationContext.getConfig().getPreloadJsonSchemaRefMaxNestingDepth()) {
            jsonSchema.initializeValidators();
        }
    }
}
