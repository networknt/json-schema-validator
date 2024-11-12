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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

class Issue857Test {
    @Test
    void test() {
        String schema = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"id\": {\r\n"
                + "      \"not\": {\r\n"
                + "        \"enum\": [\r\n"
                + "          \"1\",\r\n"
                + "          \"2\",\r\n"
                + "          \"3\"\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"$id\": \"https://d73abc/filter.json\"\r\n"
                + "}";

        String input = "{\r\n"
                + "  \"id\": \"4\"\r\n"
                + "}";

        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().failFast(true).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        Set<ValidationMessage> result = factory.getSchema(schema, config).validate(input, InputFormat.JSON);
        assertTrue(result.isEmpty());
    }
}
