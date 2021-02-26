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
import com.networknt.schema.walk.DefaultItemWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ItemsValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ItemsValidator.class);
    private static final String PROPERTY_ADDITIONAL_ITEMS = "additionalItems";

    private JsonSchema schema;
    private List<JsonSchema> tupleSchema;
    private boolean additionalItems = true;
    private JsonSchema additionalSchema;
    private WalkListenerRunner arrayItemWalkListenerRunner;
    private ValidationContext validationContext;

    public ItemsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
            ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ITEMS, validationContext);
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            schema = new JsonSchema(validationContext, schemaPath, parentSchema.getCurrentUri(), schemaNode,
                    parentSchema);
        } else {
            tupleSchema = new ArrayList<JsonSchema>();
            for (JsonNode s : schemaNode) {
                tupleSchema.add(
                        new JsonSchema(validationContext, schemaPath, parentSchema.getCurrentUri(), s, parentSchema));
            }

            JsonNode addItemNode = getParentSchema().getSchemaNode().get(PROPERTY_ADDITIONAL_ITEMS);
            if (addItemNode != null) {
                if (addItemNode.isBoolean()) {
                    additionalItems = addItemNode.asBoolean();
                } else if (addItemNode.isObject()) {
                    additionalSchema = new JsonSchema(validationContext, parentSchema.getCurrentUri(), addItemNode);
                }
            }
        }
        arrayItemWalkListenerRunner = new DefaultItemWalkListenerRunner(config.getArrayItemWalkListeners());

        this.validationContext = validationContext;

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        if (!node.isArray() && !config.isTypeLoose()) {
            // ignores non-arrays
            return errors;
        }
        if (node.isArray()) {
            int i = 0;
            for (JsonNode n : node) {
                doValidate(errors, i, n, rootNode, at);
                i++;
            }
        } else {
            doValidate(errors, 0, node, rootNode, at);
        }
        return Collections.unmodifiableSet(errors);
    }

    private void doValidate(Set<ValidationMessage> errors, int i, JsonNode node, JsonNode rootNode, String at) {
        if (schema != null) {
            // validate with item schema (the whole array has the same item
            // schema)
            errors.addAll(schema.validate(node, rootNode, at + "[" + i + "]"));
        }

        if (tupleSchema != null) {
            if (i < tupleSchema.size()) {
                // validate against tuple schema
                errors.addAll(tupleSchema.get(i).validate(node, rootNode, at + "[" + i + "]"));
            } else {
                if (additionalSchema != null) {
                    // validate against additional item schema
                    errors.addAll(additionalSchema.validate(node, rootNode, at + "[" + i + "]"));
                } else if (!additionalItems) {
                    // no additional item allowed, return error
                    errors.add(buildValidationMessage(at, "" + i));
                }
            }
        }
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        HashSet<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
        if (node != null && node.isArray()) {
            int i = 0;
            for (JsonNode n : node) {
                doWalk(validationMessages, i, n, rootNode, at, shouldValidateSchema);
                i++;
            }
        } else {
            doWalk(validationMessages, 0, node, rootNode, at, shouldValidateSchema);
        }
        return validationMessages;
    }

    private void doWalk(HashSet<ValidationMessage> validationMessages, int i, JsonNode node, JsonNode rootNode,
            String at, boolean shouldValidateSchema) {
        if (schema != null) {
            // Walk the schema.
            walkSchema(schema, node, rootNode, at + "[" + i + "]", shouldValidateSchema, validationMessages);
        }

        if (tupleSchema != null) {
            if (i < tupleSchema.size()) {
                // walk tuple schema
                walkSchema(tupleSchema.get(i), node, rootNode, at + "[" + i + "]", shouldValidateSchema,
                        validationMessages);
            } else {
                if (additionalSchema != null) {
                    // walk additional item schema
                    walkSchema(additionalSchema, node, rootNode, at + "[" + i + "]", shouldValidateSchema,
                            validationMessages);
                }
            }
        }
    }

    private void walkSchema(JsonSchema walkSchema, JsonNode node, JsonNode rootNode, String at,
            boolean shouldValidateSchema, Set<ValidationMessage> validationMessages) {
        boolean executeWalk = arrayItemWalkListenerRunner.runPreWalkListeners(ValidatorTypeCode.ITEMS.getValue(), node,
                rootNode, at, walkSchema.getSchemaPath(), walkSchema.getSchemaNode(), walkSchema.getParentSchema(),
                validationContext.getJsonSchemaFactory());
        if (executeWalk) {
            validationMessages.addAll(walkSchema.walk(node, rootNode, at, shouldValidateSchema));
        }
        arrayItemWalkListenerRunner.runPostWalkListeners(ValidatorTypeCode.ITEMS.getValue(), node, rootNode, at,
                walkSchema.getSchemaPath(), walkSchema.getSchemaNode(), walkSchema.getParentSchema(),
                validationContext.getJsonSchemaFactory(), validationMessages);

    }

    public List<JsonSchema> getTupleSchema() {
        return this.tupleSchema;
    }

    public JsonSchema getSchema() {
        return schema;
    }

}