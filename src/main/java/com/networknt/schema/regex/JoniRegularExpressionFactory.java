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
package com.networknt.schema.regex;

/**
 * Joni {@link RegularExpressionFactory}.
 * <p>
 * This requires a dependency on org.jruby.joni:joni which along with its
 * dependency libraries are 2 MB.
 */
public class JoniRegularExpressionFactory implements RegularExpressionFactory {
    private static final JoniRegularExpressionFactory INSTANCE = new JoniRegularExpressionFactory();

    public static JoniRegularExpressionFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public RegularExpression getRegularExpression(String regex) {
        return new JoniRegularExpression(regex);
    }
}
