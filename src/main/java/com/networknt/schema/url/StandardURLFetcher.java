package com.networknt.schema.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Standard URL fetcher that uses {@link URL#openStream()} for fetching.
 */
public class StandardURLFetcher implements URLFetcher {

    @Override
    public InputStream fetch(URL url) throws IOException {
        return url.openStream();
    }
}
