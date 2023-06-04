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

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The URISchemaFactory will proxy to other {@link URIFactory}s based on the scheme being used.
 */
public class URISchemeFactory implements URIFactory {
    private static final Pattern URI_SCHEME_PATTERN = Pattern.compile("^([a-z][a-z0-9+\\.\\-\\\\]*):");

    private final Map<String, URIFactory> uriFactories;

    public URISchemeFactory(final Map<String, URIFactory> uriFactories) {
        if (uriFactories == null) {
            throw new IllegalArgumentException("URIFactory map must not be null");
        }
        this.uriFactories = uriFactories;
    }

    public Map<String, URIFactory> getURIFactories() {
        return this.uriFactories;
    }

    private static String getScheme(final String uri) {
        final Matcher matcher = URI_SCHEME_PATTERN.matcher(uri);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private URIFactory getFactory(final String scheme) {
        final URIFactory uriFactory = this.uriFactories.get(scheme);
        if (uriFactory == null) {
            throw new IllegalArgumentException(String.format("Unsupported URI scheme encountered: %s", scheme));
        }
        return uriFactory;
    }

    /**
     * @param uri String
     * @return URI
     */
    @Override
    public URI create(final String uri) {
        final String scheme = getScheme(uri);
        if (scheme == null) {
            throw new IllegalArgumentException(String.format("Couldn't find URI scheme: %s", uri));
        }

        final URIFactory uriFactory = this.getFactory(scheme);
        return uriFactory.create(uri);
    }

    /**
     * @param baseURI base URI
     * @param segment URI segment
     * @return URI
     */
    @Override
    public URI create(final URI baseURI, final String segment) {
        if (baseURI == null) {
            return this.create(segment);
        }

        // We first attempt to get the scheme in case the segment is an absolute URI path.
        String scheme = getScheme(segment);
        if (scheme == null) {
            // In this case, the segment is relative to the baseURI.
            scheme = baseURI.getScheme();
            final URIFactory uriFactory = this.getFactory(scheme);
            return uriFactory.create(baseURI, segment);
        }

        if ("urn".equals(scheme)) {
            return URI.create(segment);
        }

        // In this case, the segment is an absolute URI path.
        final URIFactory uriFactory = this.getFactory(scheme);
        return uriFactory.create(segment);
    }
}
