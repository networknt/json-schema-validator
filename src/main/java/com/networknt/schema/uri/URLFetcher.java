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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Set;

/**
 * A URIfetcher that uses {@link URL#openStream()} for fetching and assumes given {@link URI}s are actualy {@link URL}s.
 */
public final class URLFetcher implements URIFetcher {

    // These supported schemes are defined in {@link #URL(String, String, int, String)}.
    // This fetcher also supports the {@link URL}s created with the {@link ClasspathURIFactory}.
    public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(URLFactory.SUPPORTED_SCHEMES);

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream fetch(final URI uri) throws IOException {
        URLConnection conn = uri.toURL().openConnection();
        return this.openConnectionCheckRedirects(conn);
    }

    // https://www.cs.mun.ca/java-api-1.5/guide/deployment/deployment-guide/upgrade-guide/article-17.html
    private InputStream openConnectionCheckRedirects(URLConnection c) throws IOException {
        boolean redir;
        int redirects = 0;
        InputStream in = null;
        do {
            if (c instanceof HttpURLConnection) {
                ((HttpURLConnection) c).setInstanceFollowRedirects(false);
            }
            // We want to open the input stream before getting headers
            // because getHeaderField() et al swallow IOExceptions.
            in = c.getInputStream();
            redir = false;
            if (c instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) c;
                int stat = http.getResponseCode();
                if (stat >= 300 && stat <= 307 && stat != 306
                    && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    URL base = http.getURL();
                    String loc = http.getHeaderField("Location");
                    URL target = null;
                    if (loc != null) {
                        target = new URL(base, loc);
                    }
                    http.disconnect();
                    // Redirection should be allowed only for HTTP and HTTPS
                    // and should be limited to 5 redirections at most.
                    if (target == null
                        || !(target.getProtocol().equals("http")
                        || target.getProtocol().equals("https"))
                        || redirects >= 5) {
                            throw new SecurityException("illegal URL redirect");
                    }
                    redir = true;
                    c = target.openConnection();
                    redirects++;
                }
            }
        }
        while (redir);
        return in;
    }
}
