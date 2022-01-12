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

import java.util.List;

import static org.junit.Assert.*;

public class ValidatorTypeCodeTest {

    @Test public void testFromProperties() {
        ValidatorTypeCode v = ValidatorTypeCode.ADDITIONAL_PROPERTIES;
        assertTrue(v.getMessageFormat().toPattern().contains("extra properties"));
        assertFalse(v.getMessageFormat().toPattern().contains("additional properties"));

    }
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

    @Test(expected = IllegalArgumentException.class)
    public void testFromValueMissing() {
        assertEquals(ValidatorTypeCode.ADDITIONAL_PROPERTIES, ValidatorTypeCode.fromValue("missing"));
    }

    @Test
    public void testIfThenElseNotInV4() {
        List<ValidatorTypeCode> list = ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V4);
        assertFalse(list.contains(ValidatorTypeCode.fromValue("if")));
    }

    @Test
    public void testExclusiveMaximumNotInV4() {
        List<ValidatorTypeCode> list = ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V4);
        assertFalse(list.contains(ValidatorTypeCode.fromValue("exclusiveMaximum")));
    }


}
