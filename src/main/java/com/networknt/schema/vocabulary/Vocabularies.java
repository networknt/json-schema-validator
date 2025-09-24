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
package com.networknt.schema.vocabulary;

import java.util.HashMap;
import java.util.Map;

/**
 * Vocabularies.
 */
public class Vocabularies {
    private static final Map<String, Vocabulary> VALUES;

    static {
        Map<String, Vocabulary> mapping = new HashMap<>();
        mapping.put(Vocabulary.DRAFT_2019_09_CORE.getIri(), Vocabulary.DRAFT_2019_09_CORE);
        mapping.put(Vocabulary.DRAFT_2019_09_APPLICATOR.getIri(), Vocabulary.DRAFT_2019_09_APPLICATOR);
        mapping.put(Vocabulary.DRAFT_2019_09_VALIDATION.getIri(), Vocabulary.DRAFT_2019_09_VALIDATION);
        mapping.put(Vocabulary.DRAFT_2019_09_META_DATA.getIri(), Vocabulary.DRAFT_2019_09_META_DATA);
        mapping.put(Vocabulary.DRAFT_2019_09_FORMAT.getIri(), Vocabulary.DRAFT_2019_09_FORMAT);
        mapping.put(Vocabulary.DRAFT_2019_09_CONTENT.getIri(), Vocabulary.DRAFT_2019_09_CONTENT);

        mapping.put(Vocabulary.DRAFT_2020_12_CORE.getIri(), Vocabulary.DRAFT_2020_12_CORE);
        mapping.put(Vocabulary.DRAFT_2020_12_APPLICATOR.getIri(), Vocabulary.DRAFT_2020_12_APPLICATOR);
        mapping.put(Vocabulary.DRAFT_2020_12_UNEVALUATED.getIri(), Vocabulary.DRAFT_2020_12_UNEVALUATED);
        mapping.put(Vocabulary.DRAFT_2020_12_VALIDATION.getIri(), Vocabulary.DRAFT_2020_12_VALIDATION);
        mapping.put(Vocabulary.DRAFT_2020_12_META_DATA.getIri(), Vocabulary.DRAFT_2020_12_META_DATA);
        mapping.put(Vocabulary.DRAFT_2020_12_FORMAT_ANNOTATION.getIri(), Vocabulary.DRAFT_2020_12_FORMAT_ANNOTATION);
        mapping.put(Vocabulary.DRAFT_2020_12_FORMAT_ASSERTION.getIri(), Vocabulary.DRAFT_2020_12_FORMAT_ASSERTION);
        mapping.put(Vocabulary.DRAFT_2020_12_CONTENT.getIri(), Vocabulary.DRAFT_2020_12_CONTENT);

        mapping.put(Vocabulary.OPENAPI_3_1_BASE.getIri(), Vocabulary.OPENAPI_3_1_BASE);

        VALUES = mapping;
    }

    /**
     * Gets the vocabulary given its uri.
     * 
     * @param uri the vocabulary
     * @return the vocabulary
     */
    public static Vocabulary getVocabulary(String uri) {
        return VALUES.get(uri);
    }
}
