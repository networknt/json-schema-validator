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

import java.util.Objects;

import com.networknt.schema.SchemaLocation;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.path.NodePath;

/**
 * The annotation.
 */
public class Annotation {
    private final String keyword;
    private final NodePath instanceLocation;
    private final SchemaLocation schemaLocation;
    private final NodePath evaluationPath;
    private final Object value;
    private final boolean schemaNodeAnnotation;
    private boolean valid = true; // If not valid it means dropped

    public Annotation(String keyword, NodePath instanceLocation, SchemaLocation schemaLocation,
            NodePath evaluationPath, Object value) {
        this(keyword, instanceLocation, schemaLocation, evaluationPath, value, false);
    }

    private Annotation(String keyword, NodePath instanceLocation, SchemaLocation schemaLocation,
            NodePath evaluationPath, Object value, boolean schemaNodeAnnotation) {
        super();
        this.keyword = keyword;
        this.instanceLocation = instanceLocation;
        this.schemaLocation = schemaLocation;
        this.evaluationPath = evaluationPath;
        this.value = value;
        this.schemaNodeAnnotation = schemaNodeAnnotation;
    }

    /**
     * The keyword that produces the annotation.
     * 
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Whether the annotation reports the raw value of its schema node rather than the
     * result of applying an applicator to the instance.
     * <p>
     * This is true for every annotation produced by
     * {@code AbstractKeywordValidator.putSchemaNodeAnnotation}: the annotation keywords,
     * including an unknown keyword, a root field of a document container and a keyword a
     * dialect registers as an {@code AnnotationKeyword}, and {@code readOnly} and
     * {@code writeOnly}. Such a keyword may have the same name as a keyword an applicator
     * consumes in another dialect, for example {@code items}, {@code contains},
     * {@code properties} or {@code unevaluatedProperties}. The applicator validators must
     * ignore these annotations, or collecting annotations would change the validation
     * result.
     *
     * @return whether the annotation reports a schema node value
     */
    public boolean isSchemaNodeAnnotation() {
        return schemaNodeAnnotation;
    }

    /**
     * The instance location to which it is attached, as a JSON Pointer.
     * 
     * @return the instance location
     */
    public NodePath getInstanceLocation() {
        return instanceLocation;
    }

    /**
     * The schema location of the attaching keyword, as a IRI and JSON Pointer
     * fragment.
     * 
     * @return the schema location
     */
    public SchemaLocation getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * The evaluation path, indicating how reference keywords such as "$ref" were
     * followed to reach the absolute schema location.
     * 
     * @return the evaluation path
     */
    public NodePath getEvaluationPath() {
        return evaluationPath;
    }

    /**
     * The value the annotation reports, which is not always the keyword's JSON value.
     * <p>
     * For a keyword whose annotation is collected from its schema node (the
     * annotation keywords and readOnly/writeOnly), the value is that node's value as
     * the Java type that matches the JSON type: a string, a number and a boolean are
     * unwrapped to {@link String}, {@link Number} and {@link Boolean}, and an object,
     * an array and a JSON null are reported as the schema's own node, not as a copy,
     * and must be treated as read-only, because mutating the returned node mutates the
     * cached schema. A JSON null is therefore a node whose {@code isNull()} is true,
     * not the Java null that means that the annotation has no value.
     * <p>
     * Other keywords report the value their own validator computes, which has the type
     * that validator documents and need not be the keyword's JSON value. In particular
     * {@code properties}, {@code patternProperties} and {@code additionalProperties}
     * report a {@link java.util.Set} of the matched property names;
     * {@code prefixItems} and {@code items} report {@code true} or, when the instance
     * array is longer than the tuple, the number of tuple schemas that were applied as
     * an {@link Integer}; {@code contains} reports {@code true} or a
     * {@link java.util.List} of the matched indexes as {@link Integer}; and
     * {@code format} and {@code contentEncoding} report their computed value. The value
     * of such a keyword must not be read as its schema node.
     *
     * @param <T> the value type
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return "Annotation [evaluationPath=" + evaluationPath + ", schemaLocation=" + schemaLocation
                + ", instanceLocation=" + instanceLocation + ", keyword=" + keyword + ", value=" + value
                + ", schemaNodeAnnotation=" + schemaNodeAnnotation + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationPath, instanceLocation, keyword, schemaLocation, value, schemaNodeAnnotation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Annotation other = (Annotation) obj;
        return schemaNodeAnnotation == other.schemaNodeAnnotation && Objects.equals(evaluationPath, other.evaluationPath)
                && Objects.equals(instanceLocation, other.instanceLocation) && Objects.equals(keyword, other.keyword)
                && Objects.equals(schemaLocation, other.schemaLocation) && Objects.equals(value, other.value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keyword;
        private NodePath instanceLocation;
        private SchemaLocation schemaLocation;
        private NodePath evaluationPath;
        private Object value;
        private boolean schemaNodeAnnotation;

        public Builder keyword(Keyword keyword) {
            this.keyword = keyword.getValue();
            return this;
        }

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder instanceLocation(NodePath instanceLocation) {
            this.instanceLocation = instanceLocation;
            return this;
        }

        public Builder schemaLocation(SchemaLocation schemaLocation) {
            this.schemaLocation = schemaLocation;
            return this;
        }

        public Builder evaluationPath(NodePath evaluationPath) {
            this.evaluationPath = evaluationPath;
            return this;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        /**
         * Marks that the annotation reports the raw value of its schema node.
         *
         * @param schemaNodeAnnotation whether the annotation reports a schema node value
         * @return the builder
         */
        public Builder schemaNodeAnnotation(boolean schemaNodeAnnotation) {
            this.schemaNodeAnnotation = schemaNodeAnnotation;
            return this;
        }

        public Annotation build() {
            return new Annotation(keyword, instanceLocation, schemaLocation, evaluationPath, value, schemaNodeAnnotation);
        }
    }

}
