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

import com.networknt.schema.NodePath;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.keyword.Keyword;

/**
 * The annotation.
 */
public class JsonNodeAnnotation {
    private final String keyword;
    private final NodePath instanceLocation;
    private final SchemaLocation schemaLocation;
    private final NodePath evaluationPath;
    private final Object value;

    public JsonNodeAnnotation(String keyword, NodePath instanceLocation, SchemaLocation schemaLocation,
            NodePath evaluationPath, Object value) {
        super();
        this.keyword = keyword;
        this.instanceLocation = instanceLocation;
        this.schemaLocation = schemaLocation;
        this.evaluationPath = evaluationPath;
        this.value = value;
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
     * The attached value(s).
     * 
     * @param <T> the value type
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    @Override
    public String toString() {
        return "JsonNodeAnnotation [evaluationPath=" + evaluationPath + ", schemaLocation=" + schemaLocation
                + ", instanceLocation=" + instanceLocation + ", keyword=" + keyword + ", value=" + value + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(evaluationPath, instanceLocation, keyword, schemaLocation, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JsonNodeAnnotation other = (JsonNodeAnnotation) obj;
        return Objects.equals(evaluationPath, other.evaluationPath)
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

        public JsonNodeAnnotation build() {
            return new JsonNodeAnnotation(keyword, instanceLocation, schemaLocation, evaluationPath, value);
        }
    }

}
