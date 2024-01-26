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
package com.networknt.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vocabularies.
 */
public class Vocabularies {
    private static final Map<String, List<String>> KEYWORDS_MAPPING;

    static {
        Map<String, List<String>> mapping = new HashMap<>();
        List<String> validation = new ArrayList<>();
        validation.add("type");
        validation.add("enum");
        validation.add("const");

        validation.add("multipleOf");
        validation.add("maximum");
        validation.add("exclusiveMaximum");
        validation.add("minimum");
        validation.add("exclusiveMinimum");
        
        validation.add("maxLength");
        validation.add("minLength");
        validation.add("pattern");

        validation.add("maxItems");
        validation.add("minItems");
        validation.add("uniqueItems");
        validation.add("maxContains");
        validation.add("minContains");
        
        validation.add("maxProperties");
        validation.add("minProperties");
        validation.add("required");
        validation.add("dependentRequired");
        
        mapping.put("validation", validation);
        
        KEYWORDS_MAPPING = mapping;
    }

    /**
     * Gets the keywords associated with a vocabulary.
     * 
     * @param vocabulary the vocabulary
     * @return the keywords
     */
    public static List<String> getKeywords(String vocabulary) {
        return KEYWORDS_MAPPING.get(vocabulary);
    }

    /**
     * Gets the vocabulary IRI.
     * 
     * @param specification the specification
     * @param vocabulary the vocabulary
     * @return the vocabulary IRI
     */
    public static String getVocabulary(SpecVersion.VersionFlag specification, String vocabulary) {
        String base = specification.getId().substring(0, specification.getId().lastIndexOf('/'));
        return base + "/vocab/" + vocabulary;
    }
}
