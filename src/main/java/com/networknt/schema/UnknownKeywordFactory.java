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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unknown keyword factory.
 * <p>
 * This treats unknown keywords as annotations.
 */
public class UnknownKeywordFactory implements KeywordFactory {
    private static final Logger logger = LoggerFactory.getLogger(UnknownKeywordFactory.class);

    private final Map<String, Keyword> keywords = new ConcurrentHashMap<>();

    @Override
    public Keyword getKeyword(String value, ValidationContext validationContext) {
        return this.keywords.computeIfAbsent(value, keyword -> {
            logger.warn(
                    "Unknown keyword {} - you should define your own Meta Schema. If the keyword is irrelevant for validation, just use a NonValidationKeyword or if it should generate annotations AnnotationKeyword",
                    keyword);
            return new AnnotationKeyword(keyword);
        });
    }

    private static class Holder {
        private static final UnknownKeywordFactory INSTANCE = new UnknownKeywordFactory();
    }

    public static UnknownKeywordFactory getInstance() {
        return Holder.INSTANCE;
    }
}
