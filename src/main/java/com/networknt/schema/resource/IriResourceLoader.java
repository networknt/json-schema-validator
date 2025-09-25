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
package com.networknt.schema.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.utils.AbsoluteIris;

/**
 * Loads from iri.
 */
public class IriResourceLoader implements ResourceLoader {
    private static class Holder {
        private static final IriResourceLoader INSTANCE = new IriResourceLoader();
    }
    
    public static IriResourceLoader getInstance() {
        return Holder.INSTANCE;
    }
    
    @Override
    public InputStreamSource getResource(AbsoluteIri absoluteIri) {
        URI uri = toURI(absoluteIri);
        URL url = toURL(uri);
        return () -> {
            URLConnection conn = url.openConnection();
            return this.openConnectionCheckRedirects(conn);
        };
    }

    /**
     * Converts an AbsoluteIRI to a URI.
     * <p>
     * Internationalized domain names will be converted using java.net.IDN.toASCII.
     * 
     * @param absoluteIri the absolute IRI
     * @return the URI
     */
    protected URI toURI(AbsoluteIri absoluteIri) {
        return URI.create(AbsoluteIris.toUri(absoluteIri));
    }

    /**
     * Converts a URI to a URL.
     * <p>
     * This will throw if the URI is not a valid URL. For instance if the URI is not
     * absolute.
     * 
     * @param uri the URL
     * @return the URL
     */
    protected URL toURL(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // https://www.cs.mun.ca/java-api-1.5/guide/deployment/deployment-guide/upgrade-guide/article-17.html
    protected InputStream openConnectionCheckRedirects(URLConnection c) throws IOException {
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
                if (stat >= 300 && stat <= 307 && stat != 306 && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    URL base = http.getURL();
                    String loc = http.getHeaderField("Location");
                    URL target = null;
                    if (loc != null) {
                        target = new URL(base, loc);
                    }
                    http.disconnect();
                    // Redirection should be allowed only for HTTP and HTTPS
                    // and should be limited to 5 redirections at most.
                    if (target == null || !(target.getProtocol().equals("http") || target.getProtocol().equals("https"))
                            || redirects >= 5) {
                        throw new SecurityException("Maximum number of redirects exceeded");
                    }
                    redir = true;
                    c = target.openConnection();
                    redirects++;
                }
            }
        } while (redir);
        return in;
    }
}
