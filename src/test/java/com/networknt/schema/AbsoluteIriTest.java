/*
 * Copyright (c) 2023 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AbsoluteIriTest {

    @Test
    void absolute() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("classpath:resource", iri.resolve("classpath:resource").toString());
    }

    @Test
    void resolveNull() {
        AbsoluteIri iri = new AbsoluteIri(null);
        assertEquals("test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeColonDotPathSegment() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/foo/foo:bar", iri.resolve("./foo:bar").toString());
    }

    @Test
    void relativeColonSecondSegment() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/foo/bar/foo:bar", iri.resolve("bar/foo:bar").toString());
    }

    @Test
    void relativeColonQueryString() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/foo/test.json?queryParam=foo:bar", iri.resolve("test.json?queryParam=foo:bar").toString());
    }

    @Test
    void relativeColonAnchor() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/foo/test.json#foo:bar", iri.resolve("test.json#foo:bar").toString());
    }

    @Test
    void relativeAtDocument() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/foo/test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtDirectory() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/");
        assertEquals("http://www.example.org/foo/test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtRoot() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org");
        assertEquals("http://www.example.org/test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtRootWithTrailingSlash() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/");
        assertEquals("http://www.example.org/test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtRootWithSchemeSpecificPart() {
        AbsoluteIri iri = new AbsoluteIri("classpath:resource");
        assertEquals("classpath:test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtRootWithSchemeSpecificPartNoPath() {
        AbsoluteIri iri = new AbsoluteIri("classpath:");
        assertEquals("classpath:test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtRootWithSchemeSpecificPartSlash() {
        AbsoluteIri iri = new AbsoluteIri("classpath:/resource");
        assertEquals("classpath:/test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtRootWithSchemeSpecificPartNoPathTrailingSlash() {
        AbsoluteIri iri = new AbsoluteIri("classpath:/");
        assertEquals("classpath:/test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeAtRootWithSchemeSpecificPartTrailingSlash() {
        AbsoluteIri iri = new AbsoluteIri("classpath:resource/");
        assertEquals("classpath:resource/test.json", iri.resolve("test.json").toString());
    }

    @Test
    void relativeParentWithSchemeSpecificPart() {
        AbsoluteIri iri = new AbsoluteIri("classpath:resource/hello/world/testing.json");
        assertEquals("classpath:resource/test.json", iri.resolve("../../test.json").toString());
    }

    @Test
    void rootColonDotPathSegment() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/foo:bar", iri.resolve("/foo:bar").toString());
    }

    @Test
    void rootColonSecondSegment() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/bar/foo:bar", iri.resolve("/bar/foo:bar").toString());
    }

    @Test
    void rootAbsoluteAtDocument() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/bar.json");
        assertEquals("http://www.example.org/test.json", iri.resolve("/test.json").toString());
    }

    @Test
    void rootAbsoluteAtDirectory() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/foo/");
        assertEquals("http://www.example.org/test.json", iri.resolve("/test.json").toString());
    }

    @Test
    void rootAbsoluteAtRoot() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org");
        assertEquals("http://www.example.org/test.json", iri.resolve("/test.json").toString());
    }

    @Test
    void rootAbsoluteAtRootWithTrailingSlash() {
        AbsoluteIri iri = new AbsoluteIri("http://www.example.org/");
        assertEquals("http://www.example.org/test.json", iri.resolve("/test.json").toString());
    }

    @Test
    void rootAbsoluteAtRootSchemeSpecificPart() {
        AbsoluteIri iri = new AbsoluteIri("classpath:resource");
        assertEquals("classpath:resource/test.json", iri.resolve("/test.json").toString());
    }

    @Test
    void schemeClasspath() {
        assertEquals("classpath", AbsoluteIri.of("classpath:resource/test.json").getScheme());
    }

    @Test
    void schemeHttps() {
        assertEquals("https", AbsoluteIri.of("https://www.example.org").getScheme());
    }

    @Test
    void schemeNone() {
        assertEquals("", AbsoluteIri.of("relative").getScheme());
    }

    @Test
    void schemeUrn() {
        assertEquals("urn", AbsoluteIri.of("urn:isbn:1234567890").getScheme());
    }

}
