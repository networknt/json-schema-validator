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

import java.util.function.Predicate;

import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.ValidationMessage;

/**
 * {@link RegularExpressionFactory} that allows regular expressions to be used.
 */
public class AllowRegularExpressionFactory implements RegularExpressionFactory {
    private final RegularExpressionFactory delegate;
    private final Predicate<String> allowed;

    public AllowRegularExpressionFactory(RegularExpressionFactory delegate, Predicate<String> allowed) {
        this.delegate = delegate;
        this.allowed = allowed;
    }

    @Override
    public RegularExpression getRegularExpression(String regex) {
        if (this.allowed.test(regex)) {
            // Allowed to delegate
            return this.delegate.getRegularExpression(regex);
        }
        throw new InvalidSchemaException(ValidationMessage.builder()
                .message("Regular expression ''{1}'' is not allowed to be used.").arguments(regex).build());
    }
}
