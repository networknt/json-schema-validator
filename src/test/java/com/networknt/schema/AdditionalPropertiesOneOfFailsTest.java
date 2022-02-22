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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;

public class AdditionalPropertiesOneOfFailsTest {

    private static Set<ValidationMessage> errors = null;

    protected JsonSchema getJsonSchemaFromStreamContent(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    @BeforeEach
    public void withJsonSchema() {
        // correct processing would include the  assertions in the tests
        if (errors == null) {
            String schemaPathJson = "/openapi3/AdditionalPropertiesOneOfFailsTest.json";
            String dataPath = "/data/AdditionalPropertiesOneOfFailsTest.json";
            InputStream schemaInputStream = getClass().getResourceAsStream(schemaPathJson);

            JsonSchema schema = getJsonSchemaFromStreamContent(schemaInputStream);
            schema.getValidationContext().getConfig().setFailFast(false);


            InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
            try {
                JsonNode node = getJsonNodeFromStreamContent(dataInputStream);

                errors = schema.validate(node);

                System.out.println("nr. of reported errors: " + errors.size());
                errors.stream().forEach(er -> System.out.println(er.toString()));
            } catch (Exception e) {
                System.out.println("Fail!");
            }

        }
    }

    @Test
    @Disabled
    public void toxicIsAdditional() {
        Assertions.assertTrue(errors.stream().filter(er -> er.toString().contains("toxic: is not defined in the schema")).count() == 2,
                "property toxic is not defined on activity chemical");
    }

    @Test
    @Disabled
    public void chemicalCharacteristicNameIsAdditional() {


        Assertions.assertTrue(errors.stream().filter(er -> er.toString().contains("$.activities[2].chemicalCharacteristic.name: is not defined in the schema")).count() == 1,
                "property name is not defined in 'oneOf' the  ChemicalCharacteristic component schemas");
    }


    @Test
    @Disabled
    public void depthIsAdditional() {

        Assertions.assertTrue(errors.stream().filter(er -> er.toString().contains("depth: is not defined in the schema")).count() == 1,
                "property depth is not defined on activity machine");
    }

    @Test
    @Disabled
    public void chemicalCharacteristicCategoryNameIsDefined() {

        Assertions.assertFalse(errors.stream().filter(er -> er.toString().contains("$.activities[0].chemicalCharacteristic.categoryName: is not defined in the schema")).count() == 1,
                "property categoryName is defined in 'oneOf' the ChemicalCharacteristic component schemas ");
    }

    @Test
    @Disabled
    public void weightIsMissingOnlyOnce() {

        Assertions.assertTrue(errors.stream().filter(er -> er.toString().contains("weight: is missing")).count() == 1,
                "property weight is required on activity machine ");
    }

    @Test
    @Disabled
    public void heightIsNotMissingNotOnceAndNotTwice() {

        Assertions.assertFalse(errors.stream().filter(er -> er.toString().contains("heigth: is missing")).count() == 1,
                "property height is defined ");

    }

    @Test
    @Disabled
    public void heightWrongType() {

        Assertions.assertTrue(errors.stream().filter(er -> er.toString().contains("heigth: number found, integer expected")).count() == 1,
                "property height has the wrong type");

    }
}
