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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.DisallowUnknownKeywordFactory;

class DisallowUnknownKeywordFactoryTest {
    @Test
    void shouldThrowForUnknownKeywords() {
        DisallowUnknownKeywordFactory factory = DisallowUnknownKeywordFactory.getInstance();
        assertThrows(InvalidSchemaException.class, () -> factory.getKeyword("helloworld", null));
    }
    
    @Test
    void getSchemaShouldThrowForUnknownKeywords() {
        Dialect dialect = Dialect.builder(Dialects.getDraft202012())
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(dialect);
        String schemaData = "{\r\n"
                + "  \"equals\": \"world\"\r\n"
                + "}";
        assertThrows(InvalidSchemaException.class, () -> factory.getSchema(schemaData));
    }
}
