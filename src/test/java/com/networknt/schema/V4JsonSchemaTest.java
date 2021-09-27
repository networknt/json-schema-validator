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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class V4JsonSchemaTest extends BaseSuiteJsonSchemaTest {
    public V4JsonSchemaTest() {
        super(SpecVersion.VersionFlag.V4);
    }

    @Test(/*expected = java.lang.StackOverflowError.class*/)
    public void testLoadingWithId() throws Exception {
        URL url = new URL("http://localhost:1234/self_ref/selfRef.json");
        JsonNode schemaJson = mapper.readTree(url);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        @SuppressWarnings("unused")
        JsonSchema schema = factory.getSchema(schemaJson);
    }

    @Test
    public void testBignumValidator() throws Exception {
        runTestFile("draft4/optional/bignum.json");
    }

    @Test
    public void testFormatValidator() throws Exception {
        runTestFile("draft4/optional/format.json");
    }

    @Test
    public void testComplexSchema() throws Exception {
        runTestFile("draft4/optional/complex.json");
    }

    @Test
    public void testZeroTerminatedFloatsValidator() throws Exception {
        runTestFile("draft4/optional/zeroTerminatedFloats.json");
    }

    @Test
    public void testAdditionalItemsValidator() throws Exception {
        runTestFile("draft4/additionalItems.json");
    }

    @Test
    public void testAdditionalPropertiesValidator() throws Exception {
        runTestFile("draft4/additionalProperties.json");
    }

    @Test
    public void testAllOfValidator() throws Exception {
        runTestFile("draft4/allOf.json");
    }

    @Test
    public void testAnyOFValidator() throws Exception {
        runTestFile("draft4/anyOf.json");
    }

    @Test
    public void testDefaultValidator() throws Exception {
        runTestFile("draft4/default.json");
    }

    @Test
    public void testDefinitionsValidator() throws Exception {
        runTestFile("draft4/definitions.json");
    }

    @Test
    public void testDependenciesValidator() throws Exception {
        runTestFile("draft4/dependencies.json");
    }

    @Test
    public void testEnumValidator() throws Exception {
        runTestFile("draft4/enum.json");
    }

    @Test
    public void testItemsValidator() throws Exception {
        runTestFile("draft4/items.json");
    }

    @Test
    public void testMaximumValidator() throws Exception {
        runTestFile("draft4/maximum.json");
    }

    @Test
    public void testMaxItemsValidator() throws Exception {
        runTestFile("draft4/maxItems.json");
    }

    @Test
    public void testMaxLengthValidator() throws Exception {
        runTestFile("draft4/maxLength.json");
    }

    @Test
    public void testMaxPropertiesValidator() throws Exception {
        runTestFile("draft4/maxProperties.json");
    }

    @Test
    public void testMinimumValidator() throws Exception {
        runTestFile("draft4/minimum.json");
    }

    @Test
    public void testMinItemsValidator() throws Exception {
        runTestFile("draft4/minItems.json");
    }

    @Test
    public void testMinLengthValidator() throws Exception {
        runTestFile("draft4/minLength.json");
    }

    @Test
    public void testMinPropertiesValidator() throws Exception {
        runTestFile("draft4/minProperties.json");
    }

    @Test
    public void testMultipleOfValidator() throws Exception {
        runTestFile("draft4/multipleOf.json");
    }

    @Test
    public void testNotValidator() throws Exception {
        runTestFile("draft4/not.json");
    }

    @Test
    public void testOneOfValidator() throws Exception {
        runTestFile("draft4/oneOf.json");
    }

    @Test
    public void testPatternValidator() throws Exception {
        runTestFile("draft4/pattern.json");
    }

    @Test
    public void testPatternPropertiesValidator() throws Exception {
        runTestFile("draft4/patternProperties.json");
    }

    @Test
    public void testPropertiesValidator() throws Exception {
        runTestFile("draft4/properties.json");
    }

    @Test
    public void testRefValidator() throws Exception {
        runTestFile("draft4/ref.json");
    }

    @Test
    public void testRefRemoteValidator() throws Exception {
        runTestFile("draft4/refRemote.json");
    }

    @Test
    public void testRefIdReference() throws Exception {
        runTestFile("draft4/idRef.json");
    }

    @Test
    public void testRelativeRefRemoteValidator() throws Exception {
        runTestFile("draft4/relativeRefRemote.json");
    }

    @Test
    public void testRequiredValidator() throws Exception {
        runTestFile("draft4/required.json");
    }

    @Test
    public void testTypeValidator() throws Exception {
        runTestFile("draft4/type.json");
    }

    @Test
    public void testUnionTypeValidator() throws Exception {
        runTestFile("draft4/union_type.json");
    }

    @Test
    public void testUniqueItemsValidator() throws Exception {
        runTestFile("draft4/uniqueItems.json");
    }

    @Test
    public void testEnumObject() throws Exception {
        runTestFile("draft4/enumObject.json");
    }

    @Test
    public void testIdSchemaWithUrl() throws Exception {
        runTestFile("draft4/property.json");
    }

    @Test
    public void testSchemaFromClasspath() throws Exception {
        runTestFile("draft4/classpath/schema.json");
    }

    @Test
    public void testUUIDValidator() throws Exception {
        runTestFile("draft4/uuid.json");
    }

    /**
     * Although, the data file has three errors, but only on is reported
     */
    @Test
    public void testFailFast_AllErrors() throws IOException {
        try {
            validateFailingFastSchemaFor("product.schema.json", "product-all-errors-data.json");
            fail("Exception must be thrown");
        } catch (JsonSchemaException e) {
            final Set<ValidationMessage> messages = e.getValidationMessages();
            assertEquals(1, messages.size());
        }
    }

    /**
     * File contains only one error and that is reported.
     */
    @Test
    public void testFailFast_OneErrors() throws IOException {
        try {
            validateFailingFastSchemaFor("product.schema.json", "product-one-error-data.json");
            fail("Exception must be thrown");
        } catch (JsonSchemaException e) {
            final Set<ValidationMessage> messages = e.getValidationMessages();
            assertEquals(1, messages.size());
        }
    }

    /**
     * Although, the file contains two errors, but only one is reported
     */
    @Test
    public void testFailFast_TwoErrors() throws IOException {
        try {
            validateFailingFastSchemaFor("product.schema.json", "product-two-errors-data.json");
            fail("Exception must be thrown");
        } catch (JsonSchemaException e) {
            final Set<ValidationMessage> messages = e.getValidationMessages();
            assertEquals(1, messages.size());
        }
    }

    /**
     * The file contains no errors, in ths case {@link Set}&lt;{@link ValidationMessage}&gt; must be empty
     */
    @Test
    public void testFailFast_NoErrors() throws IOException {
        try {
            final Set<ValidationMessage> messages = validateFailingFastSchemaFor("product.schema.json", "product-no-errors-data.json");
            assertTrue(messages.isEmpty());
        } catch (JsonSchemaException e) {
            fail("Must not get an errors");
        }
    }

    private Set<ValidationMessage> validateFailingFastSchemaFor(final String schemaFileName, final String dataFileName) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode schema = getJsonNodeFromResource(objectMapper, schemaFileName);
        final JsonNode dataFile = getJsonNodeFromResource(objectMapper, dataFileName);
        final SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setFailFast(true);
        return JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4))
                .objectMapper(objectMapper)
                .build()
                .getSchema(schema, config)
                .validate(dataFile);
    }

    private JsonNode getJsonNodeFromResource(final ObjectMapper mapper, final String locationInTestResources) throws IOException {
        return mapper.readTree(
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("draft4" + System.getProperty("file.separator") + locationInTestResources));

    }
}
