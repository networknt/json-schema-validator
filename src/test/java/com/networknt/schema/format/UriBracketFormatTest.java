/*
 * Copyright (c) 2026 the original author or authors.
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
package com.networknt.schema.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

/**
 * Tests that "[" and "]" are valid only around an IP-literal in the authority.
 */
class UriBracketFormatTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "uri | http://[::1]/p?q#f | true",
            "uri | http://x/?a[b | false",
            "uri | http://x/#a[b | false",
            "uri | http://x/#a]b | false",
            "uri | http://x/#a%5Bb%5D | true",
            "uri | urn:a[b | false",
            "uri | mailto:a]b | false",
            "uri | about:#a[b | false",
            "uri-reference | #a[b | false",
            "uri-reference | //[::1]/#a]b | false",
            "iri | http://x/#a[b | false",
            "iri | urn:a]b | false",
            "iri-reference | #a]b | false",
            "iri-reference | ?a[b | false",
    })
    void brackets(String format, String value, boolean valid) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"format\":\"" + format + "\"}");
        List<Error> messages = schema.validate("\"" + value + "\"", InputFormat.JSON,
                executionContext -> executionContext
                        .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(valid, messages.isEmpty(), () -> format + " " + value + ": " + messages);
    }
}
