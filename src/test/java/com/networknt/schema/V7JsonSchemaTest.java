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

public class V7JsonSchemaTest extends BaseSuiteJsonSchemaTest {
    public V7JsonSchemaTest() {
        super(SpecVersion.VersionFlag.V7);
    }

    @Test
    public void testOptionalBignumValidator() throws Exception {
        runTestFile("draft7/optional/bignum.json");
    }

    @Test
    @Disabled
    public void testOptionalContentValidator() throws Exception {
        runTestFile("draft7/optional/content.json");
    }

    @Test
    @Disabled
    public void testEcmascriptRegexValidator() throws Exception {
        runTestFile("draft7/optional/ecmascript-regex.json");
    }

    @Test
    @Disabled
    public void testZeroTerminatedFloatsValidator() throws Exception {
        runTestFile("draft7/optional/zeroTerminatedFloats.json");
    }

    @Test
    public void testOptionalFormatDateValidator() throws Exception {
        runTestFile("draft7/optional/format/date.json");
    }

    @Test
    public void testOptionalFormatDateTimeValidator() throws Exception {
        runTestFile("draft7/optional/format/date-time.json");
    }

    @Test
    public void testOptionalFormatEmailValidator() throws Exception {
        runTestFile("draft7/optional/format/email.json");
    }

    @Test
    public void testOptionalFormatHostnameValidator() throws Exception {
        runTestFile("draft7/optional/format/hostname.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatIdnEmailValidator() throws Exception {
        runTestFile("draft7/optional/format/idn-email.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatIdnHostnameValidator() throws Exception {
        runTestFile("draft7/optional/format/idn-hostname.json");
    }

    @Test
    public void testOptionalFormatIpv4Validator() throws Exception {
        runTestFile("draft7/optional/format/ipv4.json");
    }

    @Test
    public void testOptionalFormatIpv6Validator() throws Exception {
        runTestFile("draft7/optional/format/ipv6.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatIriValidator() throws Exception {
        runTestFile("draft7/optional/format/iri.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatIriReferenceValidator() throws Exception {
        runTestFile("draft7/optional/format/iri-reference.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatJsonPointerValidator() throws Exception {
        runTestFile("draft7/optional/format/json-pointer.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatRegexValidator() throws Exception {
        runTestFile("draft7/optional/format/regex.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatRelativeJsonPointerValidator() throws Exception {
        runTestFile("draft7/optional/format/relative-json-pointer.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatTimeValidator() throws Exception {
        runTestFile("draft7/optional/format/time.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatUriValidator() throws Exception {
        runTestFile("draft7/optional/format/uri.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatUriReferenceValidator() throws Exception {
        runTestFile("draft7/optional/format/uri-reference.json");
    }

    @Test
    @Disabled
    public void testOptionalFormatUriTemplateValidator() throws Exception {
        runTestFile("draft7/optional/format/uri-template.json");
    }

    @Test
    public void testAdditionalItemsValidator() throws Exception {
        runTestFile("draft7/additionalItems.json");
    }

    @Test
    public void testAdditionalPropertiesValidator() throws Exception {
        runTestFile("draft7/additionalProperties.json");
    }

    @Test
    public void testAllOfValidator() throws Exception {
        runTestFile("draft7/allOf.json");
    }

    @Test
    public void testAnyOfValidator() throws Exception {
        runTestFile("draft7/anyOf.json");
    }

    @Test
    public void testBooleanSchemaValidator() throws Exception {
        runTestFile("draft7/boolean_schema.json");
    }

    @Test
    public void testConstValidator() throws Exception {
        runTestFile("draft7/const.json");
    }

    @Test
    public void testContainsValidator() throws Exception {
        runTestFile("draft7/contains.json");
    }

    @Test
    public void testDefaultValidator() throws Exception {
        runTestFile("draft7/default.json");
    }

    @Test
    public void testDefsValidator() throws Exception {
        runTestFile("draft7/definitions.json");
    }

    @Test
    public void testDependenciesValidator() throws Exception {
        runTestFile("draft7/dependencies.json");
    }

    @Test
    public void testEnumValidator() throws Exception {
        runTestFile("draft7/enum.json");
    }

    @Test
    public void testExclusiveMaximumValidator() throws Exception {
        runTestFile("draft7/exclusiveMaximum.json");
    }

    @Test
    public void testExclusiveMinimumValidator() throws Exception {
        runTestFile("draft7/exclusiveMinimum.json");
    }

    @Test
    public void testFormatValidator() throws Exception {
        runTestFile("draft7/format.json");
    }

    @Test
    public void testIfThenElseValidator() throws Exception {
        runTestFile("draft7/if-then-else.json");
    }

    @Test
    public void testItemsValidator() throws Exception {
        runTestFile("draft7/items.json");
    }

    @Test
    public void testMaximumValidator() throws Exception {
        runTestFile("draft7/maximum.json");
    }

    @Test
    public void testMaxItemsValidator() throws Exception {
        runTestFile("draft7/maxItems.json");
    }

    @Test
    public void testMaxLengthValidator() throws Exception {
        runTestFile("draft7/maxLength.json");
    }

    @Test
    public void testMaxPropertiesValidator() throws Exception {
        runTestFile("draft7/maxProperties.json");
    }

    @Test
    public void testMinimumValidator() throws Exception {
        runTestFile("draft7/minimum.json");
    }

    @Test
    public void testMinItemsValidator() throws Exception {
        runTestFile("draft7/minItems.json");
    }

    @Test
    public void testMinLengthValidator() throws Exception {
        runTestFile("draft7/minLength.json");
    }

    @Test
    public void testMinPropertiesValidator() throws Exception {
        runTestFile("draft7/minProperties.json");
    }

    @Test
    public void testMultipleOfValidator() throws Exception {
        runTestFile("draft7/multipleOf.json");
    }

    @Test
    public void testNotValidator() throws Exception {
        runTestFile("draft7/not.json");
    }

    @Test
    public void testOneOfValidator() throws Exception {
        runTestFile("draft7/oneOf.json");
    }

    @Test
    public void testPatternValidator() throws Exception {
        runTestFile("draft7/pattern.json");
    }

    @Test
    public void testPatternPropertiesValidator() throws Exception {
        runTestFile("draft7/patternProperties.json");
    }

    @Test
    public void testPropertiesValidator() throws Exception {
        runTestFile("draft7/properties.json");
    }

    @Test
    public void testPropertyNamesValidator() throws Exception {
        runTestFile("draft7/propertyNames.json");
    }

    @Test
    @Disabled
    public void testRefValidator() throws Exception {
        runTestFile("draft7/ref.json");
    }

    @Test
    public void testRefRemoteValidator() throws Exception {
        runTestFile("draft7/refRemote.json");
    }

    @Test
    public void testRefIdReference() throws Exception {
        runTestFile("draft7/idRef.json");
    }

    @Test
    @Disabled
    public void testRefRemoteValidator_Ignored() throws Exception {
        runTestFile("draft7/refRemote_ignored.json");
    }

    @Test
    public void testRequiredValidator() throws Exception {
        runTestFile("draft7/required.json");
    }

    @Test
    public void testTypeValidator() throws Exception {
        runTestFile("draft7/type.json");
    }

    @Test
    public void testUniqueItemsValidator() throws Exception {
        runTestFile("draft7/uniqueItems.json");
    }

    @Test
    public void testMultipleOfScale() throws Exception {
        runTestFile("multipleOfScale.json");
    }

}
