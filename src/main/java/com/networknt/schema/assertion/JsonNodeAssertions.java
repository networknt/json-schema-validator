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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     */
    private Set<ValidationMessage> values = Collections.emptySet();

    /**
     * Gets the assertions
     */
    public Set<ValidationMessage> values() {
        return this.values;
    }

    /**
     * Puts the assertion.
     * 
     * @param assertion the assertion
     */
    public void setValues(Set<ValidationMessage> assertions) {
        if (assertions != null) {
            this.values = assertions;
        } else {
            this.values = Collections.emptySet();
        }
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
        public static String format(Set<ValidationMessage> assertions) {
            Map<String, Map<String, Map<String, Object>>> results = new LinkedHashMap<>();
            assertions.stream().forEach(assertion -> {
                String instanceLocation = assertion.getInstanceLocation().toString();
                String keyword = assertion.getType();
                String evaluationPath = assertion.getEvaluationPath().toString();
                Object value = assertion.getMessage();
                Map<String, Object> values = results.computeIfAbsent(instanceLocation, (key) -> new LinkedHashMap<>())
                        .computeIfAbsent(keyword, (key) -> new LinkedHashMap<>());
                values.put(evaluationPath, value);
            });

            try {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(results);
            } catch (JsonProcessingException e) {
                return "";
            }
        }

    }

}
