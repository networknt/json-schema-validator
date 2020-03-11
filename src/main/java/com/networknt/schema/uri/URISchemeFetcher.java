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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * The URISchemeFetcher will proxy to other {@link URIFetcher}s based on the scheme being used.
 */
public class URISchemeFetcher implements URIFetcher {
    private final Map<String, URIFetcher> uriFetchers;

    public URISchemeFetcher(final Map<String, URIFetcher> uriFetchers) {
        if (uriFetchers == null) {
            throw new IllegalArgumentException("URIFetcher map must not be null");
        }
        this.uriFetchers = uriFetchers;
    }

    public Map<String, URIFetcher> getURIFetchers() {
        return this.uriFetchers;
    }

    /**
     * @param uri URI
     * @return InputStream
     */
    public InputStream fetch(final URI uri) throws IOException {
        final URIFetcher uriFetcher = this.uriFetchers.get(uri.getScheme());
        if (uriFetcher == null) {
            throw new IllegalArgumentException(String.format("Unsupported URI scheme encountered: %s", uri.getScheme()));
        }
        return uriFetcher.fetch(uri);
    }
}
