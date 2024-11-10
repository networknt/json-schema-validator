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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.networknt.schema.InvalidSchemaException;

/**
 * Test for AllowRegularExpressionFactory.
 */
class AllowRegularExpressionFactoryTest {
    @Test
    void getRegularExpression() {
        boolean[] called = { false };
        RegularExpressionFactory delegate = (regex) -> {
            called[0] = true;
            return null;
        };
        String allowed = "testing";
        RegularExpressionFactory factory = new AllowRegularExpressionFactory(delegate, allowed::equals);
        InvalidSchemaException exception = assertThrows(InvalidSchemaException.class, () -> factory.getRegularExpression("hello"));
        assertEquals("hello", exception.getValidationMessage().getArguments()[0]);
        
        assertDoesNotThrow(() -> factory.getRegularExpression(allowed));
        assertTrue(called[0]);
    }

}
