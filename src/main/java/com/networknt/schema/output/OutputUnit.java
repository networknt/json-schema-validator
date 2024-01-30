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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents an output unit.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/blob/main/jsonschema-validation-output-machines.md">A
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

    private Map<String, String> errors = null;

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

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
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

}
