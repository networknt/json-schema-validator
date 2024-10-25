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
package com.networknt.schema.output;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Represents an output unit.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/blob/main/output/jsonschema-validation-output-machines.md">A
 *      Specification for Machine-Readable Output for JSON Schema Validation and
 *      Annotation</a>
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({ "valid", "evaluationPath", "schemaLocation", "instanceLocation", "errors", "annotations",
        "droppedAnnotations", "details" })
public class OutputUnit {
    private boolean valid;

    private String evaluationPath = null;
    private String schemaLocation = null;
    private String instanceLocation = null;

    private Map<String, Object> errors = null;

    private Map<String, Object> annotations = null;

    private Map<String, Object> droppedAnnotations = null;

    private List<OutputUnit> details = null;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getEvaluationPath() {
        return evaluationPath;
    }

    public void setEvaluationPath(String evaluationPath) {
        this.evaluationPath = evaluationPath;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getInstanceLocation() {
        return instanceLocation;
    }

    public void setInstanceLocation(String instanceLocation) {
        this.instanceLocation = instanceLocation;
    }

    public Map<String, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Object> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, Object> annotations) {
        this.annotations = annotations;
    }

    public Map<String, Object> getDroppedAnnotations() {
        return droppedAnnotations;
    }

    public void setDroppedAnnotations(Map<String, Object> droppedAnnotations) {
        this.droppedAnnotations = droppedAnnotations;
    }

    public List<OutputUnit> getDetails() {
        return details;
    }

    public void setDetails(List<OutputUnit> details) {
        this.details = details;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(annotations, details, droppedAnnotations, errors, evaluationPath, instanceLocation,
                schemaLocation, valid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OutputUnit other = (OutputUnit) obj;
        return Objects.equals(annotations, other.annotations) && Objects.equals(details, other.details)
                && Objects.equals(droppedAnnotations, other.droppedAnnotations) && Objects.equals(errors, other.errors)
                && Objects.equals(evaluationPath, other.evaluationPath)
                && Objects.equals(instanceLocation, other.instanceLocation)
                && Objects.equals(schemaLocation, other.schemaLocation) && valid == other.valid;
    }

    @Override
    public String toString() {
        try {
            return JsonMapperFactory.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "OutputUnit [valid=" + valid + ", evaluationPath=" + evaluationPath + ", schemaLocation="
                    + schemaLocation + ", instanceLocation=" + instanceLocation + ", errors=" + errors
                    + ", annotations=" + annotations + ", droppedAnnotations=" + droppedAnnotations + ", details="
                    + details + "]";
        }
    }
}
