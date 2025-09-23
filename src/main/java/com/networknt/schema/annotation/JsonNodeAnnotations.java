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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.NodePath;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * The JSON Schema annotations.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/issues/530">Details
 *      of annotation collection</a>
 */
public class JsonNodeAnnotations {

    /**
     * Stores the annotations.
     * <p>
     * instancePath to annotation
     */
    private final Map<NodePath, List<JsonNodeAnnotation>> values = new LinkedHashMap<>();

    /**
     * Gets the annotations.
     * <p>
     * instancePath to annotation
     * 
     * @return the annotations
     */
    public Map<NodePath, List<JsonNodeAnnotation>> asMap() {
        return this.values;
    }

    /**
     * Puts the annotation.
     * 
     * @param annotation the annotation
     */
    public void put(JsonNodeAnnotation annotation) {
        this.values.computeIfAbsent(annotation.getInstanceLocation(), (k) -> new ArrayList<>()).add(annotation);

    }

    @Override
    public String toString() {
        return Formatter.format(this.values);
    }

    /**
     * Formatter for pretty printing the annotations.
     */
    public static class Formatter {
        /**
         * Formats the annotations.
         * 
         * @param annotations the annotations
         * @return the formatted JSON
         */
        public static String format(Map<NodePath, List<JsonNodeAnnotation>> annotations) {
            Map<String, Map<String, Map<String, Object>>> results = new LinkedHashMap<>();
            for (List<JsonNodeAnnotation> list : annotations.values()) {
                for (JsonNodeAnnotation annotation : list) {
                    String keyword = annotation.getKeyword();
                    String instancePath = annotation.getInstanceLocation().toString();
                    String evaluationPath = annotation.getEvaluationPath().toString();
                    Map<String, Object> values = results
                            .computeIfAbsent(instancePath, (key) -> new LinkedHashMap<>())
                            .computeIfAbsent(keyword, (key) -> new LinkedHashMap<>());
                    values.put(evaluationPath, annotation.getValue());
                }
            }

            try {
                return JsonMapperFactory.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(results);
            } catch (JsonProcessingException e) {
                return "";
            }
        }

    }

}
