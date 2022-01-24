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
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;

public class AdditionalPropertiesOneOfFailsTest {

    protected JsonSchema getJsonSchemaFromStreamContent(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    @Test
    public void withJsonSchema() throws Exception {

        String schemaPathJson = "/openapi3/AdditionalPropertiesOneOfFailsTest.json";
        String dataPath = "/data/AdditionalPropertiesOneOfFailsTest.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPathJson);

        JsonSchema schema = getJsonSchemaFromStreamContent(schemaInputStream);
        schema.getValidationContext().getConfig().setFailFast(false);


        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);

        Set<ValidationMessage> errors = schema.validate(node);

        System.out.println("nr. of errors: " + errors.size());
        errors.stream().forEach(er -> System.out.println(er.toString()));

        // correct assertions would include:
        Assertions.assertTrue(errors.stream().filter(er -> er.toString().contains("toxic: is not defined in the schema")).count()==1);
        Assertions.assertTrue(errors.stream().filter(er -> er.toString().contains("depth: is not defined in the schema")).count()==1);
        Assertions.assertFalse(errors.stream().filter(er-> er.toString().contains("$.activities[0].chemical.categoryName: is not defined in the schema")).count()==1);
        Assertions.assertTrue(errors.stream().filter(er-> er.toString().contains("$.activities[1].weight: is missing")).count()==1);
        Assertions.assertTrue(errors.stream().filter(er-> er.toString().contains("$.activities[1].heigth: number found, integer expected")).count()==1);


    }
}
