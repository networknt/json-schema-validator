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

package com.networknt.schema;

import org.junit.Test;

import static com.networknt.schema.TypeValidator.isNumeric;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeValidatorTest {
    
    private static final String[] validNumericValues  = {
            "1", "-1", "1.1", "-1.1", "0E+1", "0E-1", "0E1", "-0E+1", "-0E-1", "-0E1", "0.1E+1", "0.1E-1", "0.1E1",
            "-0.1E+1", "-0.1E-1", "-0.1E1", "10.1", "-10.1", "10E+1", "10E-1", "10E1", "-10E+1", "-10E-1", "-10E1",
            "10.1E+1", "10.1E-1", "10.1E1", "-10.1E+1", "-10.1E-1", "-10.1E1", "1E+0", "1E-0", "1E0",
            "1E00000000000000000000"
    };
    private static final String[] invalidNumericValues = {
            "01.1", "1.", ".1", "0.1.1", "E1", "E+1", "E-1", ".E1", ".E+1", ".E-1", ".1E1", ".1E+1", ".1E-1", "1E-",
            "1E+", "1E", "+", "-", "1a", "0.1a", "0E1a", "0E-1a", "1.0a", "1.0aE1"
            //, "+0", "+1" // for backward compatibility, in violation of JSON spec
    };
    
    @Test
    public void testNumeicValues() {
        for(String validValue : validNumericValues) {
            assertTrue(validValue, isNumeric(validValue));
        }
    }

    @Test
    public void testNonNumeicValues() {
        for(String invalidValue : invalidNumericValues) {
            assertFalse(invalidValue, isNumeric(invalidValue));
        }
    }
}
