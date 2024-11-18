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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unknown keyword factory that rejects unknown keywords.
 */
public class DisallowUnknownKeywordFactory implements KeywordFactory {
    private static final Logger logger = LoggerFactory.getLogger(DisallowUnknownKeywordFactory.class);

    @Override
    public Keyword getKeyword(String value, ValidationContext validationContext) {
        logger.error("Keyword '{}' is unknown and must be configured on the meta-schema or vocabulary", value);
        throw new InvalidSchemaException(ValidationMessage.builder()
                .message("Keyword ''{1}'' is unknown and must be configured on the meta-schema or vocabulary")
                .arguments(value).build());
    }

    private static class Holder {
        private static final DisallowUnknownKeywordFactory INSTANCE = new DisallowUnknownKeywordFactory();
    }

    /**
     * Gets the instance of {@link DisallowUnknownKeywordFactory}.
     *
     * @return the keyword factory
     */
    public static DisallowUnknownKeywordFactory getInstance() {
        return Holder.INSTANCE;
    }
}
