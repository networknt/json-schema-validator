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
import com.networknt.schema.SpecVersion.VersionFlag;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class Issue375Test {
    protected JsonSchema getJsonSchemaFromStreamContent(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    void shouldFailAndShowValidationValuesWithError() throws Exception {
        String schemaPath = "/draft2019-09/issue375.json";
        String dataPath = "/data/issue375.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContent(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        Set<ValidationMessage> errors = schema.validate(node);
        List<String> errorMessages = new ArrayList<String>();
        for (ValidationMessage error: errors) {
            errorMessages.add(error.getMessage());
        }

        List<String> expectedMessages = Arrays.asList(
            "$.fields: property 'longName123' name is not valid: must be at most 5 characters long",
            "$.fields: property 'longName123' name is not valid: does not match the regex pattern ^[a-zA-Z]+$",
            "$.fields: property 'a' name is not valid: must be at least 3 characters long");
        MatcherAssert.assertThat(errorMessages, Matchers.containsInAnyOrder(expectedMessages.toArray()));
    }
}
