/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class V6JsonSchemaTest extends BaseSuiteJsonSchemaTest {
    public V6JsonSchemaTest() {
        super(SpecVersion.VersionFlag.V6);
    }

    @Test
    public void testOptionalBignumValidator() throws Exception {
        runTestFile("draft6/optional/bignum.json");
    }

    @Test
    @Disabled
    public void testEcmascriptRegexValidator() throws Exception {
        runTestFile("draft6/optional/ecmascript-regex.json");
    }

    @Test
    @Disabled
    public void testZeroTerminatedFloatsValidator() throws Exception {
        runTestFile("draft6/optional/zeroTerminatedFloats.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatValidator() throws Exception {
        runTestFile("draft6/optional/format.json");
    }

    @Test
    public void testAdditionalItemsValidator() throws Exception {
        runTestFile("draft6/additionalItems.json");
    }

    @Test
    public void testAdditionalPropertiesValidator() throws Exception {
        runTestFile("draft6/additionalProperties.json");
    }

    @Test
    public void testAllOfValidator() throws Exception {
        runTestFile("draft6/allOf.json");
    }

    @Test
    public void testAnyOfValidator() throws Exception {
        runTestFile("draft6/anyOf.json");
    }

    @Test
    public void testBooleanSchemaValidator() throws Exception {
        runTestFile("draft6/boolean_schema.json");
    }

    @Test
    public void testConstValidator() throws Exception {
        runTestFile("draft6/const.json");
    }

    @Test
    public void testContainsValidator() throws Exception {
        runTestFile("draft6/contains.json");
    }

    @Test
    public void testDefaultValidator() throws Exception {
        runTestFile("draft6/default.json");
    }

    @Test
    public void testDefinitionsValidator() throws Exception {
        runTestFile("draft6/definitions.json");
    }

    @Test
    public void testDependenciesValidator() throws Exception {
        runTestFile("draft6/dependencies.json");
    }

    @Test
    public void testEnumValidator() throws Exception {
        runTestFile("draft6/enum.json");
    }

    @Test
    public void testExclusiveMaximumValidator() throws Exception {
        runTestFile("draft6/exclusiveMaximum.json");
    }

    @Test
    public void testExclusiveMinimumValidator() throws Exception {
        runTestFile("draft6/exclusiveMinimum.json");
    }

    @Test
    public void testFormatValidator() throws Exception {
        runTestFile("draft6/format.json");
    }

    @Test
    public void testItemsValidator() throws Exception {
        runTestFile("draft6/items.json");
    }

    @Test
    public void testMaximumValidator() throws Exception {
        runTestFile("draft6/maximum.json");
    }

    @Test
    public void testMaxItemsValidator() throws Exception {
        runTestFile("draft6/maxItems.json");
    }

    @Test
    public void testMaxLengthValidator() throws Exception {
        runTestFile("draft6/maxLength.json");
    }

    @Test
    public void testMaxPropertiesValidator() throws Exception {
        runTestFile("draft6/maxProperties.json");
    }

    @Test
    public void testMinimumValidator() throws Exception {
        runTestFile("draft6/minimum.json");
    }

    @Test
    public void testMinItemsValidator() throws Exception {
        runTestFile("draft6/minItems.json");
    }

    @Test
    public void testMinLengthValidator() throws Exception {
        runTestFile("draft6/minLength.json");
    }

    @Test
    public void testMinPropertiesValidator() throws Exception {
        runTestFile("draft6/minProperties.json");
    }

    @Test
    public void testMultipleOfValidator() throws Exception {
        runTestFile("draft6/multipleOf.json");
    }

    @Test
    public void testNotValidator() throws Exception {
        runTestFile("draft6/not.json");
    }

    @Test
    public void testOneOfValidator() throws Exception {
        runTestFile("draft6/oneOf.json");
    }

    @Test
    public void testPatternValidator() throws Exception {
        runTestFile("draft6/pattern.json");
    }

    @Test
    public void testPatternPropertiesValidator() throws Exception {
        runTestFile("draft6/patternProperties.json");
    }

    @Test
    public void testPropertiesValidator() throws Exception {
        runTestFile("draft6/properties.json");
    }

    @Test
    public void testPropertyNamesValidator() throws Exception {
        runTestFile("draft6/propertyNames.json");
    }

    @Test
    @Disabled
    public void testRefValidator() throws Exception {
        runTestFile("draft6/ref.json");
    }

    @Test
    public void testRefRemoteValidator() throws Exception {
        runTestFile("draft6/refRemote.json");
    }

    @Test
    public void testRefIdReference() throws Exception {
        runTestFile("draft6/idRef.json");
    }

    @Test
    @Disabled
    public void testRefRemoteValidator_Ignored() throws Exception {
        runTestFile("draft6/refRemote_ignored.json");
    }

    @Test
    public void testRequiredValidator() throws Exception {
        runTestFile("draft6/required.json");
    }

    @Test
    public void testTypeValidator() throws Exception {
        runTestFile("draft6/type.json");
    }

    @Test
    public void testUniqueItemsValidator() throws Exception {
        runTestFile("draft6/uniqueItems.json");
    }

}
