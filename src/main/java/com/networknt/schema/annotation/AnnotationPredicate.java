/*
 * Copyright (c) 2023 the original author or authors.
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
package com.networknt.schema.annotation;

import java.util.function.Predicate;

import com.networknt.schema.NodePath;
import com.networknt.schema.SchemaLocation;

/**
 * A predicate for filtering annotations.
 */
public class AnnotationPredicate implements Predicate<Annotation> {
    final Predicate<NodePath> instanceLocationPredicate;
    final Predicate<NodePath> evaluationPathPredicate;
    final Predicate<SchemaLocation> schemaLocationPredicate;
    final Predicate<String> keywordPredicate;
    final Predicate<Object> valuePredicate;

    /**
     * Initialize a new instance of this class.
     * 
     * @param instanceLocationPredicate for instanceLocation
     * @param evaluationPathPredicate   for evaluationPath
     * @param schemaLocationPredicate   for schemaLocation
     * @param keywordPredicate          for keyword
     * @param valuePredicate            for value
     */
    protected AnnotationPredicate(Predicate<NodePath> instanceLocationPredicate,
            Predicate<NodePath> evaluationPathPredicate, Predicate<SchemaLocation> schemaLocationPredicate,
            Predicate<String> keywordPredicate, Predicate<Object> valuePredicate) {
        super();
        this.instanceLocationPredicate = instanceLocationPredicate;
        this.evaluationPathPredicate = evaluationPathPredicate;
        this.schemaLocationPredicate = schemaLocationPredicate;
        this.keywordPredicate = keywordPredicate;
        this.valuePredicate = valuePredicate;
    }

    @Override
    public boolean test(Annotation t) {
        return ((valuePredicate == null || valuePredicate.test(t.getValue()))
                && (keywordPredicate == null || keywordPredicate.test(t.getKeyword()))
                && (instanceLocationPredicate == null || instanceLocationPredicate.test(t.getInstanceLocation()))
                && (evaluationPathPredicate == null || evaluationPathPredicate.test(t.getEvaluationPath()))
                && (schemaLocationPredicate == null || schemaLocationPredicate.test(t.getSchemaLocation())));
    }

    /**
     * Gets the predicate to filter on instanceLocation.
     * 
     * @return the predicate
     */
    public Predicate<NodePath> getInstanceLocationPredicate() {
        return instanceLocationPredicate;
    }

    /**
     * Gets the predicate to filter on evaluationPath.
     * 
     * @return the predicate
     */
    public Predicate<NodePath> getEvaluationPathPredicate() {
        return evaluationPathPredicate;
    }

    /**
     * Gets the predicate to filter on schemaLocation.
     * 
     * @return the predicate
     */
    public Predicate<SchemaLocation> getSchemaLocationPredicate() {
        return schemaLocationPredicate;
    }

    /**
     * Gets the predicate to filter on keyword.
     * 
     * @return the predicate
     */
    public Predicate<String> getKeywordPredicate() {
        return keywordPredicate;
    }

    /**
     * Gets the predicate to filter on value.
     * 
     * @return the predicate
     */
    public Predicate<Object> getValuePredicate() {
        return valuePredicate;
    }

    /**
     * Creates a new builder to create the predicate.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for building a {@link AnnotationPredicate}.
     */
    public static class Builder {
        Predicate<NodePath> instanceLocationPredicate;
        Predicate<NodePath> evaluationPathPredicate;
        Predicate<SchemaLocation> schemaLocationPredicate;
        Predicate<String> keywordPredicate;
        Predicate<Object> valuePredicate;

        public Builder instanceLocation(Predicate<NodePath> instanceLocationPredicate) {
            this.instanceLocationPredicate = instanceLocationPredicate;
            return this;
        }

        public Builder evaluationPath(Predicate<NodePath> evaluationPathPredicate) {
            this.evaluationPathPredicate = evaluationPathPredicate;
            return this;
        }

        public Builder schema(Predicate<SchemaLocation> schemaLocationPredicate) {
            this.schemaLocationPredicate = schemaLocationPredicate;
            return this;
        }

        public Builder keyword(Predicate<String> keywordPredicate) {
            this.keywordPredicate = keywordPredicate;
            return this;
        }

        public Builder value(Predicate<Object> valuePredicate) {
            this.valuePredicate = valuePredicate;
            return this;
        }

        public AnnotationPredicate build() {
            return new AnnotationPredicate(instanceLocationPredicate, evaluationPathPredicate,
                    schemaLocationPredicate, keywordPredicate, valuePredicate);
        }
    }
}
