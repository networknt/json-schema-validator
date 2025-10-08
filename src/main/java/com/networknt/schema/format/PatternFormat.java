/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema.format;

import java.util.regex.Pattern;

import com.networknt.schema.ExecutionContext;

/**
 * Format using a regex pattern.
 */
public class PatternFormat implements Format {
    private final String name;
    private final Pattern pattern;
    private final String messageKey;

    /**
     * Constructor.
     * <p>
     * Use {@link #of(String, String, String)} instead.
     * 
     * @param name the name
     * @param regex the regex
     */
    private PatternFormat(String name, String regex, String messageKey) {
        this.name = name;
        this.messageKey = messageKey;
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Creates a pattern format.
     * 
     * @param name the name
     * @param regex the regex pattern
     * @param messageKey the message key
     * @return the pattern format
     */
    public static PatternFormat of(String name, String regex, String messageKey) {
        return new PatternFormat(name, regex, messageKey != null ? messageKey : "format");
    }

    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        return this.pattern.matcher(value).matches();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getMessageKey() {
        return this.messageKey;
    }
}
