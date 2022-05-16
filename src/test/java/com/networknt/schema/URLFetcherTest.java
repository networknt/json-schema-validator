package com.networknt.schema;

import com.networknt.schema.uri.URIFetcher;
import com.networknt.schema.uri.URLFetcher;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class URLFetcherTest {
    // It is ignored as it depends on the petstore server to be started locally.
    @Test
    @Disabled
    public void testPetstore() throws URISyntaxException, IOException {
        final URIFetcher urlFetcher = new URLFetcher();
        InputStream inputStream = urlFetcher.fetch(new URI("https://localhost:9443/v1/pets"));
        Assertions.assertNotNull(inputStream);
    }

    @Test
    public void testPostman() throws URISyntaxException, IOException {
        final URIFetcher urlFetcher = new URLFetcher();
        InputStream inputStream = urlFetcher.fetch(new URI("https://postman-echo.com/get"));
        Assertions.assertNotNull(inputStream);
    }
    /*
    @Test
    public void testHttpClient() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://localhost:9443/v1/pets"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient
                .newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
    */
}
