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
package com.networknt.schema.assertion;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.ValidationMessage;

/**
 * The JSON Schema assertions.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/issues/530">Details
 *      of annotation collection</a>
 */
public class JsonNodeAssertions {

    /**
     * Stores the assertions.
     * <p>
     * evaluationPath -> keyword -> instancePath -> assertion
     */
    private final Map<JsonNodePath, Map<String, Map<JsonNodePath, ValidationMessage>>> values = new LinkedHashMap<>();

    /**
     * Stores the assertions.
     * <p>
     * evaluationPath -> keyword -> instancePath -> assertion
     */
    public Map<JsonNodePath, Map<String, Map<JsonNodePath, ValidationMessage>>> asMap() {
        return this.values;
    }

    /**
     * Puts the assertion.
     * 
     * @param assertion the assertion
     */
    public void put(ValidationMessage assertion) {
        Map<String, Map<JsonNodePath, ValidationMessage>> instance = this.values
                .computeIfAbsent(assertion.getEvaluationPath(), (key) -> new LinkedHashMap<>());
        Map<JsonNodePath, ValidationMessage> keyword = instance.computeIfAbsent(assertion.getType(),
                (key) -> new LinkedHashMap<>());
        keyword.put(assertion.getInstanceLocation(), assertion);

    }

    @Override
    public String toString() {
        return Formatter.format(this.values);
    }

    /**
     * Formatter for pretty printing the assertions.
     */
    public static class Formatter {
        public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        /**
         * Formats the assertions.
         * 
         * @param assertions the assertions
         * @return the formatted JSON
         */
        public static String format(Map<JsonNodePath, Map<String, Map<JsonNodePath, ValidationMessage>>> assertions) {
            Map<String, Map<String, Map<String, Object>>> results = new LinkedHashMap<>();
            assertions.entrySet().stream().forEach(instances -> {
                String instancePath = instances.getKey().toString();
                instances.getValue().entrySet().stream().forEach(keywords -> {
                    String keyword = keywords.getKey();
                    keywords.getValue().entrySet().stream().forEach(evaluations -> {
                        String evaluationPath = evaluations.getKey().toString();
                        Object assertion = evaluations.getValue().getMessage();
                        Map<String, Object> values = results
                                .computeIfAbsent(instancePath, (key) -> new LinkedHashMap<>())
                                .computeIfAbsent(keyword, (key) -> new LinkedHashMap<>());
                        values.put(evaluationPath, assertion);
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
