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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;

class Issue1091Test {
    @Test
    @Disabled // Disabled as this test takes quite long to run for ci
    void testHasAdjacentKeywordInEvaluationPath() throws Exception {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().cacheRefs(false).build();

        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V4)
                .getSchema(SchemaLocation.of("classpath:schema/issue1091.json"), config);
        JsonNode node = JsonMapperFactory.getInstance()
                .readTree(Issue1091Test.class.getClassLoader().getResource("data/issue1091.json"));

        List<String> messages = schema.validate(node)
                .stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toList());

        assertEquals(0, messages.size());
    }
}
