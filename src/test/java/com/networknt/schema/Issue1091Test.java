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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class Issue1091Test {
    @Test
    @Disabled // Disabled as this test takes quite long to run for ci
    void testHasAdjacentKeywordInEvaluationPath() throws Exception {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4)
                .getSchema(SchemaLocation.of("classpath:schema/issue1091.json"));
        List<Error> errors = schema.validate(AbsoluteIri.of("classpath:data/issue1091.json"), InputFormat.JSON);
        assertEquals(0, errors.size());
    }
}
