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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

/**
 * Tests the values that java.net.URI cannot parse but RFC 3986 allows: an empty authority, or an empty hier-part with
 * or without a fragment.
 */
class UriFallbackFormatTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            // scheme ":" "//" with an empty authority, and scheme ":" with an empty hier-part
            "uri | http:// | true",
            "uri | a+b.c-d9:// | true",
            "uri | about: | true",
            "iri | http:// | true",
            "uri-reference | http:// | true",
            "uri-reference | about: | true",
            "iri-reference | about: | true",
            // the network-path reference "//" has no scheme, so it is not an absolute URI
            "uri | // | false",
            "iri | // | false",
            "uri-reference | // | true",
            "iri-reference | // | true",
            // scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
            "uri | 1http:// | false",
            "uri | bar,baz:// | false",
            "uri | ,: | false",
            "uri | :// | false",
            "uri-reference | 1http: | false",
            "uri-reference | 1http:// | false",
            "iri-reference | bar,baz: | false",
            "iri | 1http:// | false",
            "iri | about: | true",
            // the empty scheme that 3.0.9 accepted
            "iri | :// | false",
            "uri-reference | :// | false",
            "iri-reference | :// | false",
            // scheme ":" "#" fragment, with an empty hier-part
            "uri | about:#frag | true",
            "uri | about:# | true",
            "uri-reference | about:#frag | true",
            "uri | about:#a b | false",
            "uri | about:#\u0192 | false",
            "iri | about:#\u0192 | true",
            "iri | about:#a\u202Eb | false",
            // the value is checked as it is: JDK 25 trims the input of the URISyntaxException
            "uri | ' http://' | false",
            "uri | 'about: ' | false",
            "uri-reference | ' // ' | false",
            "iri | ' about:#frag' | false",
    })
    void fallback(String format, String value, boolean valid) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"format\":\"" + format + "\"}");
        List<Error> messages = schema.validate("\"" + value + "\"", InputFormat.JSON,
                executionContext -> executionContext
                        .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(valid, messages.isEmpty(), () -> format + " " + value + ": " + messages);
    }

    @Test
    void subclassGetsValueUnchanged() {
        String[] input = new String[1];
        AbstractRFC3986Format format = new UriReferenceFormat() {
            @Override
            protected boolean handleException(URISyntaxException e) {
                input[0] = e.getInput();
                return super.handleException(e);
            }
        };
        assertFalse(format.matches(null, "  http:// "));
        assertEquals("  http:// ", input[0]);
    }

    @Test
    void subclassValidatesFallback() {
        // a custom format for absolute https URIs only
        AbstractRFC3986Format format = new AbstractRFC3986Format() {
            @Override
            protected boolean validate(URI uri) {
                return uri.isAbsolute() && "https".equals(uri.getScheme());
            }

            @Override
            public String getName() {
                return "https-uri";
            }
        };
        assertTrue(format.matches(null, "https://"));
        assertFalse(format.matches(null, "//"));
        assertFalse(format.matches(null, "http://"));
        assertFalse(format.matches(null, "ftp:"));
    }
}
