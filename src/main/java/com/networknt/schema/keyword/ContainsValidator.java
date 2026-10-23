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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * {@link KeywordValidator} for contains.
 */
public class ContainsValidator extends BaseKeywordValidator {
    private static final CountLimit DEFAULT_MINIMUM = CountLimit.of(1);
    private static final String CONTAINS_MAX = "contains.max";
    private static final String CONTAINS_MIN = "contains.min";

    private final Schema schema;
    private final boolean hasMinContains;

    private final CountLimit min;
    private final CountLimit max;

    public ContainsValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.CONTAINS, schemaNode, schemaLocation, parentSchema, schemaContext);

        // Draft 6 added the contains keyword but maxContains and minContains first
        // appeared in Draft 2019-09. They only apply when the dialect has them, so
        // Draft 6 and 7 ignore them unless a custom dialect adds them.
        Map<String, Keyword> keywords = this.schemaContext.getDialect().getKeywords();
        this.hasMinContains = keywords.containsKey(KeywordType.MIN_CONTAINS.getValue());
        boolean hasMaxContains = keywords.containsKey(KeywordType.MAX_CONTAINS.getValue());

        CountLimit currentMax = null;
        CountLimit currentMin = null;
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = schemaContext.newSchema(schemaLocation, schemaNode, parentSchema);
            // Without these keywords, contains only needs one matching element.
            JsonNode parentSchemaNode = parentSchema.getSchemaNode();
            if (hasMaxContains) {
                currentMax = CountLimit.parse(parentSchemaNode.get(KeywordType.MAX_CONTAINS.getValue()),
                        parentSchemaNode, KeywordType.MAX_CONTAINS.getValue());
            }
            if (this.hasMinContains) {
                currentMin = CountLimit.parse(parentSchemaNode.get(KeywordType.MIN_CONTAINS.getValue()),
                        parentSchemaNode, KeywordType.MIN_CONTAINS.getValue());
            }
        } else {
            this.schema = null;
        }
        this.max = currentMax;
        this.min = currentMin;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        // ignores non-arrays
        int actual = 0, i = 0;
        List<Integer> indexes = new ArrayList<>(); // for the annotation
        if (null != this.schema && node.isArray()) {
            // Save flag as nested schema evaluation shouldn't trigger fail fast
            boolean failFast = executionContext.isFailFast();
            List<Error> existingErrors = executionContext.getErrors();
            try {
                executionContext.setFailFast(false);
                List<Error> test = new ArrayList<>();
                executionContext.setErrors(test);
                for (JsonNode n : node) {
                    NodePath path = instanceLocation.append(i);
                    this.schema.validate(executionContext, n, rootNode, path);
                    if (test.isEmpty()) {
                        ++actual;
                        indexes.add(i);
                    } else {
                        test.clear();
                    }
                    ++i;
                }
            } finally {
                // Restore flag
                executionContext.setFailFast(failFast);
                executionContext.setErrors(existingErrors);
            }
            CountLimit m = DEFAULT_MINIMUM; // default to 1 if "min" not specified
            if (this.min != null) {
                m = this.min;
            }
            if (!m.isMinimumSatisfiedBy(actual)) {
                boundsViolated(executionContext, this.hasMinContains ? KeywordType.MIN_CONTAINS : KeywordType.CONTAINS,
                        executionContext.getExecutionConfig().getLocale(),
                        node, instanceLocation, m);
            }

            if (this.max != null && !this.max.isMaximumSatisfiedBy(actual)) {
                boundsViolated(executionContext, KeywordType.MAX_CONTAINS,
                        executionContext.getExecutionConfig().getLocale(),
                        node, instanceLocation, this.max);
            }
        }
        
        boolean collectAnnotations = hasUnevaluatedItemsInEvaluationPath(executionContext);
        if (this.schema != null) {
            // This keyword produces an annotation value which is an array of the indexes to
            // which this keyword validates successfully when applying its subschema, in
            // ascending order. The value MAY be a boolean "true" if the subschema validates
            // successfully when applied to every index of the instance. The annotation MUST
            // be present if the instance array to which this keyword's schema applies is
            // empty.
            
            if (collectAnnotations || collectAnnotations(executionContext, "contains")) {
                if (actual == i) {
                    // evaluated all
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                                    .keyword("contains").value(true).build());
                } else {
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                                    .keyword("contains").value(indexes).build());
                }
            }
            
            // Add minContains and maxContains annotations
            if (this.min != null) {
                String minContainsKeyword = "minContains";
                if (collectAnnotations || collectAnnotations(executionContext, minContainsKeyword)) {
                    // Omitted keywords MUST NOT produce annotation results. However, as described
                    // in the section for contains, the absence of this keyword's annotation causes
                    // contains to assume a minimum value of 1.
                    executionContext.evaluationPathAddLast(minContainsKeyword);
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath())
                                    .schemaLocation(this.schemaLocation.append(minContainsKeyword))
                                    .keyword(minContainsKeyword).value(this.min.value()).build());
                    executionContext.evaluationPathRemoveLast();
                }
            }
            
            if (this.max != null) {
                String maxContainsKeyword = "maxContains";
                if (collectAnnotations || collectAnnotations(executionContext, maxContainsKeyword)) {
                    executionContext.evaluationPathAddLast(maxContainsKeyword);
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath())
                                    .schemaLocation(this.schemaLocation.append(maxContainsKeyword))
                                    .keyword(maxContainsKeyword).value(this.max.value()).build());
                    executionContext.evaluationPathRemoveLast();
                }
            }
        }
    }

    @Override
    public void preloadSchema() {
        Optional.ofNullable(this.schema).ifPresent(Schema::initializeValidators);
    }

    private void boundsViolated(ExecutionContext executionContext, KeywordType validatorTypeCode, Locale locale,
            JsonNode instanceNode, NodePath instanceLocation, CountLimit bounds) {
        String messageKey = "contains";
        if (KeywordType.MIN_CONTAINS.equals(validatorTypeCode)) {
            messageKey = CONTAINS_MIN;
        } else if (KeywordType.MAX_CONTAINS.equals(validatorTypeCode)) {
            messageKey = CONTAINS_MAX;
        }
        executionContext.addError(error().instanceNode(instanceNode).instanceLocation(instanceLocation).evaluationPath(executionContext.getEvaluationPath()).messageKey(messageKey)
                        .locale(locale).arguments(bounds.messageArgument(), this.schema.getSchemaNode().toString())
                        .keyword(validatorTypeCode.getValue()).build());
    }
    
}
