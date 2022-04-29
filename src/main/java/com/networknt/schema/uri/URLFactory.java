/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema.uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A URIFactory that uses URL for creating {@link URI}s.
 */
public final class URLFactory implements URIFactory {
    // These supported schemes are defined in {@link #URL(String, String, int, String)}.
    public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("http", "https", "ftp", "file", "jar")));

    /**
     * @param uri String
     * @return URI
     */
    @Override
    public URI create(final String uri) {
        try {
            return URI.create(uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        }
    }

    /**
     * @param baseURI URI
     * @param segment String
     * @return URI
     */
    @Override
    public URI create(final URI baseURI, final String segment) {
        try {
            return new URL(baseURI.toURL(), segment).toURI();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        }
    }
}
