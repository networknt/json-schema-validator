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
package com.networknt.schema.keyword;

import com.networknt.schema.ValidationContext;

/**
 * Factory for {@link Keyword}.
 */
@FunctionalInterface
public interface KeywordFactory {
    /**
     * Gets the keyword given the keyword value.
     * 
     * @param value the keyword value
     * @param validationContext the validationContext
     * @return the keyword
     */
    Keyword getKeyword(String value, ValidationContext validationContext);
}
