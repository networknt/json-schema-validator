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

import java.util.HashMap;
import java.util.Map;

/**
 * Vocabularies.
 */
public class Vocabularies {
    private static final Map<String, Vocabulary> VALUES;

    static {
        Map<String, Vocabulary> mapping = new HashMap<>();
        mapping.put(Vocabulary.V201909_CORE.getId(), Vocabulary.V201909_CORE);
        mapping.put(Vocabulary.V201909_APPLICATOR.getId(), Vocabulary.V201909_APPLICATOR);
        mapping.put(Vocabulary.V201909_VALIDATION.getId(), Vocabulary.V201909_VALIDATION);
        mapping.put(Vocabulary.V201909_META_DATA.getId(), Vocabulary.V201909_META_DATA);
        mapping.put(Vocabulary.V201909_FORMAT.getId(), Vocabulary.V201909_FORMAT);
        mapping.put(Vocabulary.V201909_CONTENT.getId(), Vocabulary.V201909_CONTENT);

        mapping.put(Vocabulary.V202012_CORE.getId(), Vocabulary.V202012_CORE);
        mapping.put(Vocabulary.V202012_APPLICATOR.getId(), Vocabulary.V202012_APPLICATOR);
        mapping.put(Vocabulary.V202012_UNEVALUATED.getId(), Vocabulary.V202012_UNEVALUATED);
        mapping.put(Vocabulary.V202012_VALIDATION.getId(), Vocabulary.V202012_VALIDATION);
        mapping.put(Vocabulary.V202012_META_DATA.getId(), Vocabulary.V202012_META_DATA);
        mapping.put(Vocabulary.V202012_FORMAT_ANNOTATION.getId(), Vocabulary.V202012_FORMAT_ANNOTATION);
        mapping.put(Vocabulary.V202012_FORMAT_ASSERTION.getId(), Vocabulary.V202012_FORMAT_ASSERTION);
        mapping.put(Vocabulary.V202012_CONTENT.getId(), Vocabulary.V202012_CONTENT);

        VALUES = mapping;
    }

    /**
     * Gets the vocabulary given its id.
     * 
     * @param vocabulary the vocabulary
     * @return the vocabulary
     */
    public static Vocabulary getVocabulary(String vocabulary) {
        return VALUES.get(vocabulary);
    }
}
