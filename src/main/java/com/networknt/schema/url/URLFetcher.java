package com.networknt.schema.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface URLFetcher {
    InputStream fetch(URL url) throws IOException;
}
