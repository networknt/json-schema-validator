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
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonNodePath;

/**
 * The JSON Schema annotations.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/issues/530">Details
 *      of annotation collection</a>
 */
public class JsonNodeAnnotations {

    public static class Stream {
        private final Map<JsonNodePath, Map<String, Map<JsonNodePath, JsonNodeAnnotation>>> annotations;
        private final JsonNodeAnnotationPredicate filter;

        /**
         * Initialize a new instance of this class.
         * 
         * @param annotations the annotations
         * @param filter      the filter
         */
        protected Stream(final Map<JsonNodePath, Map<String, Map<JsonNodePath, JsonNodeAnnotation>>> annotations,
                JsonNodeAnnotationPredicate filter) {
            this.annotations = annotations;
            this.filter = filter;
        }

        /**
         * Returns a stream that will match the filter.
         * 
         * @param filter the filter
         * @return a new stream
         */
        public Stream filter(JsonNodeAnnotationPredicate filter) {
            return new Stream(this.annotations, filter);
        }

        /**
         * Returns a stream that will match the filter.
         * 
         * @param filter the filter
         * @return a new stream
         */
        public Stream filter(Consumer<JsonNodeAnnotationPredicate.Builder> filter) {
            JsonNodeAnnotationPredicate.Builder builder = JsonNodeAnnotationPredicate.builder();
            filter.accept(builder);
            return filter(builder.build());
        }

        /**
         * Performs an action for each element of this stream.
         * 
         * @param action the action to be performed
         */
        public void forEach(Consumer<JsonNodeAnnotation> action) {
            annotations.entrySet().stream().forEach(instances -> {
                if (filter == null || filter.instanceLocationPredicate == null
                        || filter.instanceLocationPredicate.test(instances.getKey())) {
                    instances.getValue().entrySet().stream().forEach(keywords -> {
                        if (filter == null || filter.keywordPredicate == null
                                || filter.keywordPredicate.test(keywords.getKey())) {
                            keywords.getValue().entrySet().stream().forEach(evaluations -> {
                                if (filter == null || ((filter.evaluationPathPredicate == null
                                        || filter.evaluationPathPredicate.test(evaluations.getKey()))
                                        && (filter.valuePredicate == null
                                                || filter.valuePredicate.test(evaluations.getValue().getValue())))) {
                                    action.accept(evaluations.getValue());
                                }
                            });
                        }
                    });
                }
            });
        }

        /**
         * Collects the elements in the stream to a list.
         * 
         * @return the list
         */
        public List<JsonNodeAnnotation> toList() {
            List<JsonNodeAnnotation> result = new ArrayList<>();
            forEach(result::add);
            return result;
        }
    }

    /**
     * Stores the annotations.
     * <p>
     * instancePath -> keyword -> evaluationPath -> annotation
     */
    private final Map<JsonNodePath, Map<String, Map<JsonNodePath, JsonNodeAnnotation>>> values = new LinkedHashMap<>();

    /**
     * Gets the annotations.
     * <p>
     * instancePath -> keyword -> evaluationPath -> annotation
     * 
     * @return the annotations
     */
    public Map<JsonNodePath, Map<String, Map<JsonNodePath, JsonNodeAnnotation>>> asMap() {
        return this.values;
    }

    /**
     * Puts the annotation.
     * 
     * @param annotation the annotation
     */
    public void put(JsonNodeAnnotation annotation) {
        Map<String, Map<JsonNodePath, JsonNodeAnnotation>> instance = this.values
                .computeIfAbsent(annotation.getInstanceLocation(), (key) -> new LinkedHashMap<>());
        Map<JsonNodePath, JsonNodeAnnotation> keyword = instance.computeIfAbsent(annotation.getKeyword(),
                (key) -> new LinkedHashMap<>());
        keyword.put(annotation.getEvaluationPath(), annotation);

    }

    /**
     * Returns a stream for processing the annotations.
     * 
     * @return the stream
     */
    public Stream stream() {
        return new Stream(values, null);
    }

    @Override
    public String toString() {
        return Formatter.format(this.values);
    }

    /**
     * Formatter for pretty printing the annotations.
     */
    public static class Formatter {
        public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        /**
         * Formats the annotations.
         * 
         * @param annotations the annotations
         * @return the formatted JSON
         */
        public static String format(Map<JsonNodePath, Map<String, Map<JsonNodePath, JsonNodeAnnotation>>> annotations) {
            Map<String, Map<String, Map<String, Object>>> results = new LinkedHashMap<>();
            annotations.entrySet().stream().forEach(instances -> {
                String instancePath = instances.getKey().toString();
                instances.getValue().entrySet().stream().forEach(keywords -> {
                    String keyword = keywords.getKey();
                    keywords.getValue().entrySet().stream().forEach(evaluations -> {
                        String evaluationPath = evaluations.getKey().toString();
                        Object annotation = evaluations.getValue().getValue();
                        Map<String, Object> values = results
                                .computeIfAbsent(instancePath, (key) -> new LinkedHashMap<>())
                                .computeIfAbsent(keyword, (key) -> new LinkedHashMap<>());
                        values.put(evaluationPath, annotation);
                    });
                });
            });

            try {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(results);
            } catch (JsonProcessingException e) {
                return "";
            }
        }

    }

}
