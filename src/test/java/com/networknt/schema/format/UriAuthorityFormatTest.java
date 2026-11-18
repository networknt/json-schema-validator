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
 * Tests the RFC 3986 section 3.2 authority checks shared by the uri, uri-reference, iri and iri-reference formats.
 */
class UriAuthorityFormatTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            // port = *DIGIT
            "uri | http://example.com:8080/p | true",
            "uri | http://example.com:/p | true",
            "uri | http://example.com:abc/p | false",
            "uri | http://example.com:80a/p | false",
            "uri | http://[::1]:8080/p | true",
            // userinfo = *( unreserved / pct-encoded / sub-delims / ":" )
            "uri | http://user:pass@example.com/p | true",
            "uri | http://us%40er@example.com/p | true",
            "uri | http://a@b@example.com/p | false",
            // reg-name: a registry-based name that java.net.URI cannot parse as a server is still valid
            "uri | http://a_b.example/p | true",
            "uri | http://087.10.0.1/ | true",
            // IP-literal = "[" ( IPv6address / IPvFuture ) "]"
            "uri | http://[2001:db8::7]/p | true",
            "uri | http://[::ffff:1.2.3.4]/p | true",
            "uri | http://[::ffff:01.2.3.4]/p | false",
            "uri | http://[fe80::1%25en1]/p | false",
            "uri | http://2001:db8::1/p | false",
            // no authority, or an empty one
            "uri | http:// | true",
            "uri | about: | true",
            "uri | urn:example:resource | true",
            "uri-reference | //example.com:abc/p | false",
            "uri-reference | //a@b@example.com/ | false",
            "uri-reference | //[::ffff:192.168.0.01]/p | false",
            "uri-reference | //example.com:8080/p | true",
            "uri-reference | /p | true",
            // ucschar is allowed in the userinfo and the host of an IRI only
            "iri | http://â@ƒøø.ßår/ | true",
            "iri | http://â@b@ƒøø.ßår/ | false",
            "iri | http://ƒøø.ßår:abc/path | false",
            "iri | http://ƒøø.ßår:8080/path | true",
            "iri-reference | //ƒøø.ßår/p | true",
            "iri-reference | //ƒøø.ßår:abc/p | false",
            "uri | http://ƒøø.ßår/ | false",
            // a percent-encoded "::" is a valid reg-name, not an IPv6 address
            "uri | http://1%3A%3A2/ | true",
            "iri | http://1%3A%3A2/ | true",
            "iri-reference | //1%3A%3A2/ | true",
            // RFC 3987 section 4.1 forbids bidirectional formatting characters
            "iri | http://ex\u202Eample/ | false",
            "iri | http://ex\u200Fample/ | false",
            "iri-reference | //ex\u202Aample/p | false",
    })
    void authority(String format, String value, boolean valid) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"format\":\"" + format + "\"}");
        List<Error> messages = schema.validate("\"" + value + "\"", InputFormat.JSON,
                executionContext -> executionContext
                        .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(valid, messages.isEmpty(), () -> format + " " + value + ": " + messages);
    }
}
