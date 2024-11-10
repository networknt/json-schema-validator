/*
 * Copyright (c) 2024 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Test MultipleOfValidator validator.
 */
class MultipleOfValidatorTest {
    String schemaData = "{" +
            "  \"type\": \"object\"," +
            "  \"properties\": {" +
            "    \"value1\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }," +
            "    \"value2\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }," +
            "    \"value3\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }" +
            "  }" +
            "}";

    @Test
    void test() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(schemaData);
        String inputData = "{\"value1\":123.892,\"value2\":123456.2934,\"value3\":123.123}";
        String validData = "{\"value1\":123.89,\"value2\":123456,\"value3\":123.010}";
        
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(3, messages.stream().filter(m -> "multipleOf".equals(m.getType())).count());
        
        messages = schema.validate(validData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void testTypeLoose() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(schemaData);
        
        String inputData = "{\"value1\":\"123.892\",\"value2\":\"123456.2934\",\"value3\":123.123}";
        String validTypeLooseInputData = "{\"value1\":\"123.89\",\"value2\":\"123456.29\",\"value3\":123.12}";
        
        // Without type loose this has 2 type and 1 multipleOf errors
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(2, messages.stream().filter(m -> "type".equals(m.getType())).count());
        assertEquals(1, messages.stream().filter(m -> "multipleOf".equals(m.getType())).count());
        
        // 2 type errors
        messages = schema.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(2, messages.size());
        assertEquals(2, messages.stream().filter(m -> "type".equals(m.getType())).count());
        
        // With type loose this has 3 multipleOf errors
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().typeLoose(true).build();
        JsonSchema typeLoose = factory.getSchema(schemaData, config);
        messages = typeLoose.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(3, messages.stream().filter(m -> "multipleOf".equals(m.getType())).count());
        
        // No errors
        messages = typeLoose.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }
}
