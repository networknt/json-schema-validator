# Custom URIFetcher

The default `URIFetcher` implementation uses JDK connection/socket without handling network exceptions. It works in most of the cases; however, if you want to have a customized implementation, you can do so. One user has his implementation with urirest to handle the timeout. A detailed discussion can be found in this [issue](https://github.com/networknt/json-schema-validator/issues/240)

## Example implementation

The default URIFetcher can be overwritten in order to customize its behaviour in regards of authorization or error handling.
Therefore the _URIFetcher_ interface must implemented and the method _fetch_ must be overwritten.

```
public class CustomUriFetcher implements URIFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUriFetcher.class);

    private final String        authorizationToken;

    private final HttpClient    client;

    public CustomUriFetcher(String authorizationToken) {
        this.authorizationToken = authorizationToken;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public InputStream fetch(URI uri) throws IOException {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).header("Authorization", authorizationToken).build();
        try {
            HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if ((200 > response.statusCode()) || (response.statusCode() > 299)) {
                String errorMessage = String.format("Could not get data from schema endpoint. The following status %d was returned.", response.statusCode());
                LOGGER.error(errorMessage);
            }

            return new ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

Within the _JsonSchemaFactory_ the custom URIFetcher can be referenced.
This also works for schema references ($ref) inside the schema.

```
...
CustomUriFetcher uriFetcher = new CustomUriFetcher(authorizationToken);

JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder()
        .uriFetcher(uriFetcher, "http")
        .addMetaSchema(JsonMetaSchema.getV7())
        .defaultMetaSchemaURI(JsonMetaSchema.getV7().getUri())
        .build();
JsonSchema jsonSchema = schemaFactory.getSchema(schemaUri);
for (ValidationMessage validationMessage : jsonSchema.validate(jsonNodeRecord)) {
    // handle the validation messages
}
```

**_NOTE:_** 
Within `.uriFetcher(uriFetcher, "http")` your URI must be mapped to the related protocol like http, ftp, ...