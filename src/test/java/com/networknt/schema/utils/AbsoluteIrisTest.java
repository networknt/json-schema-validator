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
package com.networknt.schema.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.networknt.schema.AbsoluteIri;

/**
 * Tests for AbsoluteIris.
 */
class AbsoluteIrisTest {
    @Test
    void uri() {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("https://www.example.org/test"));
        assertEquals("https://www.example.org/test", result);
    }

    @Test
    void uriWithQueryString() {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("https://www.example.org/test/?filter[test]=hello"));
        assertEquals("https://www.example.org/test/?filter%5Btest%5D=hello", result);
    }

    @Test
    void iriDomain() {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("https://Bücher.example"));
        assertEquals("https://xn--bcher-kva.example", result);
    }

    @Test
    void iriDomainWithPath() {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("https://Bücher.example/assets/produktdatenblätter.pdf"));
        result = URI.create(result).toASCIIString();
        assertEquals("https://xn--bcher-kva.example/assets/produktdatenbl%C3%A4tter.pdf", result);
    }
    
    @Test
    void uriDomainWithPath() {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("https://www.example.org/assets/produktdatenblätter.pdf"));
        result = URI.create(result).toASCIIString();
        assertEquals("https://www.example.org/assets/produktdatenbl%C3%A4tter.pdf", result);
    }

    @Test
    void iriDomainWithPathTrailingSlash() {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("https://Bücher.example/assets/produktdatenblätter/"));
        assertEquals("https://xn--bcher-kva.example/assets/produktdatenbl%C3%A4tter/", result);
    }

    @Test
    void iriDomainWithQueryString() throws MalformedURLException {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("https://Bücher.example/assets/produktdatenblätter/?filter[test]=hello"));
        assertEquals("https://xn--bcher-kva.example/assets/produktdatenbl%C3%A4tter/?filter%5Btest%5D=hello", result);
        URL url = URI.create(result).toURL();
        assertEquals("https", url.getProtocol());
        assertEquals("xn--bcher-kva.example", url.getHost());
        assertEquals("/assets/produktdatenbl%C3%A4tter/", url.getPath());
        assertEquals("filter%5Btest%5D=hello", url.getQuery());
    }
    
    @Test
    void invalid() {
        String result = AbsoluteIris.toUri(AbsoluteIri.of("www.example.org/test"));
        assertEquals("www.example.org/test", result);
    }
}
