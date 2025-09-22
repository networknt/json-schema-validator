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

import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.annotation.JsonNodeAnnotation;

/**
 * Abstract {@link KeywordValidator}.
 */
public abstract class AbstractKeywordValidator implements KeywordValidator {
    private final String keyword;
    protected final JsonNode schemaNode;
    protected final SchemaLocation schemaLocation;

    protected final JsonNodePath evaluationPath;

    /**
     * Constructor.
     * @param keyword        the keyword
     * @param schemaNode     the schema node
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     */
    public AbstractKeywordValidator(String keyword, JsonNode schemaNode, SchemaLocation schemaLocation, JsonNodePath evaluationPath) {
        this.keyword = keyword;
        this.schemaNode = schemaNode;
        this.schemaLocation = schemaLocation;
        this.evaluationPath = evaluationPath;
    }

    /**
     * Constructor.
     * @param keyword        the keyword
     * @param schemaNode     the schema node
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     */
    public AbstractKeywordValidator(Keyword keyword, JsonNode schemaNode, SchemaLocation schemaLocation, JsonNodePath evaluationPath) {
        this(keyword.getValue(), schemaNode, schemaLocation, evaluationPath);
    }

    @Override
    public SchemaLocation getSchemaLocation() {
        return schemaLocation;
    }

    @Override
    public JsonNodePath getEvaluationPath() {
        return evaluationPath;
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    /**
     * The schema node used to create the validator.
     * 
     * @return the schema node
     */
    public JsonNode getSchemaNode() {
        return this.schemaNode;
    }

    @Override
    public String toString() {
        return getEvaluationPath().getName(-1);
    }

    /**
     * Determine if annotations should be reported.
     * 
     * @param executionContext the execution context
     * @return true if annotations should be reported
     */
    protected boolean collectAnnotations(ExecutionContext executionContext) {
        return collectAnnotations(executionContext, getKeyword());
    }

    /**
     * Determine if annotations should be reported.
     * 
     * @param executionContext the execution context
     * @param keyword          the keyword
     * @return true if annotations should be reported
     */
    protected boolean collectAnnotations(ExecutionContext executionContext, String keyword) {
        return executionContext.getExecutionConfig().isAnnotationCollectionEnabled()
                && executionContext.getExecutionConfig().getAnnotationCollectionFilter().test(keyword);
    }

    /**
     * Puts an annotation.
     * 
     * @param executionContext the execution context
     * @param customizer to customize the annotation
     */
    protected void putAnnotation(ExecutionContext executionContext, Consumer<JsonNodeAnnotation.Builder> customizer) {
        JsonNodeAnnotation.Builder builder = JsonNodeAnnotation.builder().evaluationPath(this.evaluationPath)
                .schemaLocation(this.schemaLocation).keyword(getKeyword());
        customizer.accept(builder);
        executionContext.getAnnotations().put(builder.build());
    }
}
