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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatorTypeCodeTest {

    @Test
    public void testFromValueString() {
        assertEquals(ValidatorTypeCode.ADDITIONAL_PROPERTIES, ValidatorTypeCode.fromValue("additionalProperties"));
    }

    @Test
    public void testFromValueAll() {
        for (ValidatorTypeCode code : ValidatorTypeCode.values()) {
            assertEquals(code, ValidatorTypeCode.fromValue(code.getValue()));
        }
    }

    @Test
    public void testFromValueMissing() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> assertEquals(ValidatorTypeCode.ADDITIONAL_PROPERTIES, ValidatorTypeCode.fromValue("missing")));
    }

    @Test
    public void testIfThenElseNotInV4() {
        List<ValidatorTypeCode> list = ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V4);
        Assertions.assertFalse(list.contains(ValidatorTypeCode.fromValue("if")));
    }

    @Test
    public void testExclusiveMaximumNotInV4() {
        List<ValidatorTypeCode> list = ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V4);
        Assertions.assertFalse(list.contains(ValidatorTypeCode.fromValue("exclusiveMaximum")));
    }


}
