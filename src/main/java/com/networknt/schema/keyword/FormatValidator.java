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
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.format.BaseFormatValidator;
import com.networknt.schema.format.Format;
import com.networknt.schema.path.NodePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.PatternSyntaxException;

/**
 * Validator for Format.
 */
public class FormatValidator extends BaseFormatValidator implements KeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(FormatValidator.class);

    private final Format format;
    
    public FormatValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext, Format format,
            Keyword keyword) {
        super(schemaLocation, schemaNode, parentSchema, keyword, schemaContext);
        this.format = format;
    }

    /**
     * Gets the annotation value.
     * 
     * @return the annotation value
     */
    protected Object getAnnotationValue() {
        if (this.format != null) {
            return this.format.getName();
        }
        return this.schemaNode.isTextual() ? schemaNode.textValue() : null;
    }
    
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        
        /*
         * Annotations must be collected even if the format is unknown according to the specification.
         */
        if (collectAnnotations(executionContext)) {
            Object annotationValue = getAnnotationValue();
            if (annotationValue != null) {
                putAnnotation(executionContext,
                        annotation -> annotation.instanceLocation(instanceLocation).value(annotationValue));
            }
        }

        boolean assertionsEnabled = isAssertionsEnabled(executionContext);
        if (this.format != null) {
            try {
                format.validate(executionContext, schemaContext, node, rootNode, instanceLocation,
                        assertionsEnabled,
                        () -> this.error().instanceNode(node).instanceLocation(instanceLocation)
                                .evaluationPath(executionContext.getEvaluationPath()).messageKey(format.getMessageKey())
                                .locale(executionContext.getExecutionConfig().getLocale())
                                ,
                        this);
            } catch (PatternSyntaxException pse) {
                // String is considered valid if pattern is invalid
                logger.error("Failed to apply pattern on {}: Invalid RE syntax [{}]", instanceLocation,
                        format.getName(), pse);
            }
        } else {
            validateUnknownFormat(executionContext, node, rootNode, instanceLocation);
        }
    }

    /**
     * When the Format-Assertion vocabulary is specified, implementations MUST fail upon encountering unknown formats.
     * 
     * @param executionContext the execution context
     * @param node the node
     * @param rootNode the root node
     * @param instanceLocation the instance location
     */
    protected void validateUnknownFormat(ExecutionContext executionContext,
            JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        /*
         * Unknown formats should create an assertion if the vocab is specified
         * according to the specification.
         */
        if (createUnknownFormatAssertions(executionContext) && this.schemaNode.isTextual()) {
            executionContext.addError(error().instanceLocation(instanceLocation).instanceNode(node)
                    .evaluationPath(executionContext.getEvaluationPath()).messageKey("format.unknown").arguments(schemaNode.textValue()).build());
        }
    }

    /**
     * When the Format-Assertion vocabulary is specified, implementations MUST fail
     * upon encountering unknown formats.
     * <p>
     * Note that this is different from setting the setFormatAssertionsEnabled
     * configuration option.
     * <p>
     * The following logic will return true if the format assertions option is
     * turned on and strict is enabled (default false) or the format assertion
     * vocabulary is enabled.
     * 
     * @param executionContext the execution context
     * @return true if format assertions should be generated
     */
    protected boolean createUnknownFormatAssertions(ExecutionContext executionContext) {
        return (isAssertionsEnabled(executionContext) && isStrict(executionContext)) || (isFormatAssertionVocabularyEnabled());
    }

    /**
     * Determines if strict handling.
     * <p>
     * Note that this defaults to false.
     * 
     * @param executionContext the execution context
     * @return whether to perform strict handling
     */
    protected boolean isStrict(ExecutionContext executionContext) {
        return this.schemaContext.getSchemaRegistryConfig().isStrict(getKeyword(), Boolean.FALSE);
    }
}
