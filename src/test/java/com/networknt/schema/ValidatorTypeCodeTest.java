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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ValidatorTypeCodeTest {
    @Parameterized.Parameters
    public static Collection<?> parameters() {
      return Arrays.asList(new Object[][] {
         { SpecVersion.VersionFlag.V4 },
         { SpecVersion.VersionFlag.V6 },
         { SpecVersion.VersionFlag.V7 },
         { SpecVersion.VersionFlag.V201909 }
      });
    }

    private final SpecVersion.VersionFlag specVersion;
    
    public ValidatorTypeCodeTest(final SpecVersion.VersionFlag specVersion) {
        super();
        this.specVersion = specVersion;
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
        List<ValidatorTypeCode> list = ValidatorTypeCode.getNonFormatKeywords(this.specVersion);
        boolean isInList = list.contains(ValidatorTypeCode.fromValue("if"));
        if (SpecVersion.VersionFlag.V4 == this.specVersion
                || SpecVersion.VersionFlag.V6 == this.specVersion) {
            Assert.assertFalse(isInList);
        }
        else {
            Assert.assertTrue(isInList);
        }
    }

    @Test
    public void testExclusiveMaximumNotInV4() {
        List<ValidatorTypeCode> list = ValidatorTypeCode.getNonFormatKeywords(this.specVersion);
        boolean isInList = list.contains(ValidatorTypeCode.fromValue("exclusiveMaximum"));
        if (SpecVersion.VersionFlag.V4 == this.specVersion) {
            Assert.assertFalse(isInList);
        }
        else {
            Assert.assertTrue(isInList);
        }
    }
}
