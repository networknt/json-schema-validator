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
import com.networknt.schema.annotation.JsonNodeAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.networknt.schema.VersionCode.MinV201909;

public class ContainsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ContainsValidator.class);
    private static final String CONTAINS_MAX = "contains.max";
    private static final String CONTAINS_MIN = "contains.min";

    private final JsonSchema schema;
    private final boolean isMinV201909;

    private Integer min = null;
    private Integer max = null;
    
    private Boolean hasUnevaluatedItemsValidator = null;

    public ContainsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.CONTAINS, validationContext);

        // Draft 6 added the contains keyword but maxContains and minContains first
        // appeared in Draft 2019-09 so the semantics of the validation changes
        // slightly.
        this.isMinV201909 = MinV201909.getVersions().contains(this.validationContext.getMetaSchema().getSpecification());

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
            JsonNode parentSchemaNode = parentSchema.getSchemaNode();
            Optional.ofNullable(parentSchemaNode.get(ValidatorTypeCode.MAX_CONTAINS.getValue()))
                    .filter(JsonNode::canConvertToExactIntegral)
                    .ifPresent(node -> this.max = node.intValue());

            Optional.ofNullable(parentSchemaNode.get(ValidatorTypeCode.MIN_CONTAINS.getValue()))
                    .filter(JsonNode::canConvertToExactIntegral)
                    .ifPresent(node -> this.min = node.intValue());
        } else {
            this.schema = null;
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        // ignores non-arrays
        Set<ValidationMessage> results = null;
        int actual = 0, i = 0;
        List<Integer> indexes = new ArrayList<>(); // for the annotation
        if (null != this.schema && node.isArray()) {
            for (JsonNode n : node) {
                JsonNodePath path = instanceLocation.append(i);
                if (this.schema.validate(executionContext, n, rootNode, path).isEmpty()) {
                    ++actual;
                    indexes.add(i);
                }
                ++i;
            }
            int m = 1; // default to 1 if "min" not specified
            if (this.min != null) {
                m = this.min;
            }
            if (actual < m) {
                if(isMinV201909) {
                    updateValidatorType(ValidatorTypeCode.MIN_CONTAINS);
                }
                results = boundsViolated(isMinV201909 ? CONTAINS_MIN : ValidatorTypeCode.CONTAINS.getValue(),
                        executionContext.getExecutionConfig().getLocale(), instanceLocation, m);
            }

            if (this.max != null && actual > this.max) {
                if(isMinV201909) {
                    updateValidatorType(ValidatorTypeCode.MAX_CONTAINS);
                }
                results = boundsViolated(isMinV201909 ? CONTAINS_MAX : ValidatorTypeCode.CONTAINS.getValue(),
                        executionContext.getExecutionConfig().getLocale(), instanceLocation, this.max);
            }
        }

        if (collectAnnotations()) {
            if (this.schema != null) {
                // This keyword produces an annotation value which is an array of the indexes to
                // which this keyword validates successfully when applying its subschema, in
                // ascending order. The value MAY be a boolean "true" if the subschema validates
                // successfully when applied to every index of the instance. The annotation MUST
                // be present if the instance array to which this keyword's schema applies is
                // empty.
                if (actual == i) {
                    // evaluated all
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(true).build());
                } else {
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(indexes).build());
                }
                // Add minContains and maxContains annotations
                if (this.min != null) {
                    // Omitted keywords MUST NOT produce annotation results. However, as described
                    // in the section for contains, the absence of this keyword's annotation causes
                    // contains to assume a minimum value of 1.
                    String minContainsKeyword = "minContains";
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath.append(minContainsKeyword))
                                    .schemaLocation(this.schemaLocation.append(minContainsKeyword))
                                    .keyword(minContainsKeyword).value(this.min).build());
                }
                if (this.max != null) {
                    String maxContainsKeyword = "maxContains";
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath.append(maxContainsKeyword))
                                    .schemaLocation(this.schemaLocation.append(maxContainsKeyword))
                                    .keyword(maxContainsKeyword).value(this.max).build());
                }
            }
        }
        return results == null ? Collections.emptySet() : results;
    }

    @Override
    public void preloadJsonSchema() {
        Optional.ofNullable(this.schema).ifPresent(JsonSchema::initializeValidators);
        collectAnnotations();
    }

    private Set<ValidationMessage> boundsViolated(String messageKey, Locale locale, JsonNodePath instanceLocation, int bounds) {
        return Collections.singleton(message().instanceLocation(instanceLocation).messageKey(messageKey).locale(locale)
                .arguments(String.valueOf(bounds), this.schema.getSchemaNode().toString()).build());
    }
    
    private boolean collectAnnotations() {
        return hasUnevaluatedItemsValidator();
    }

    private boolean hasUnevaluatedItemsValidator() {
        if (this.hasUnevaluatedItemsValidator == null) {
            this.hasUnevaluatedItemsValidator = hasAdjacentKeywordInEvaluationPath("unevaluatedItems");
        }
        return hasUnevaluatedItemsValidator;
    }
}
