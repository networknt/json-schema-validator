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

import java.net.*;
import java.util.Collections;
import java.util.Set;

/**
 * A URIFactory that uses URL for creating {@link URI}s.
 */
public final class ClasspathURLFactory implements URIFactory {
    static final URLStreamHandler STREAM_HANDLER = new ClasspathURLStreamHandler();

    public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(
            ClasspathURLStreamHandler.SUPPORTED_SCHEMES);

    public static URL convert(final URI uri) throws MalformedURLException {
        return new URL(null, uri.toString(), STREAM_HANDLER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI create(final String uri) {
        try {
            return new URL(null, uri, STREAM_HANDLER).toURI();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI create(final URI baseURI, final String segment) {
        try {
            return new URL(convert(baseURI), segment, STREAM_HANDLER).toURI();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        }
    }
}
