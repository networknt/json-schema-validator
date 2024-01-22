# Customizing Schema Retrieval

A schema can be identified by its schema identifier which is indicated using the `$id` keyword or `id` keyword in earlier drafts. This is an absolute IRI that uniquely identifies the schema and is not necessarily a network locator. A schema need not be downloadable from it's absolute IRI.

In the event a schema references a schema identifier that is not a subschema resource, for instance defined in the `$defs` keyword or `definitions` keyword. The library will need to be able to retrieve the schema given its schema identifier.

In the event that the schema does not define a schema identifier using the `$id` keyword, the retrieval IRI will be used as it's schema identifier.

## Mapping Schema Identifier to Retrieval IRI

The schema identifier can be mapped to the retrieval IRI by implementing the `AbsoluteIriMapper` interface.

### Configuring AbsoluteIriMapper

```java
JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder()
        .schemaLoaderBuilder(schemaLoaderBuilder -> schemaLoaderBuilder
            .absoluteIriMapper(new CustomAbsoluteIriMapper())
        .addMetaSchema(JsonMetaSchema.getV7())
        .defaultMetaSchemaURI(JsonMetaSchema.getV7().getUri())
        .build();
```

### Configuring Prefix Mappings

```java
JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder()
        .schemaLoaderBuilder(schemaLoaderBuilder -> schemaLoaderBuilder
            .mapPrefix("https://", "http://")
            .mapPrefix("http://json-schema.org", "classpath:"))
        .addMetaSchema(JsonMetaSchema.getV7())
        .defaultMetaSchemaURI(JsonMetaSchema.getV7().getUri())
        .build();
```

## Customizing Network Schema Retrieval

The default `UriSchemaLoader` implementation uses JDK connection/socket without handling network exceptions. It works in most of the cases; however, if you want to have a customized implementation, you can do so. One user has his implementation with urirest to handle the timeout. A detailed discussion can be found in this [issue](https://github.com/networknt/json-schema-validator/issues/240)

### Configuring Custom URI Schema Loader

The default `UriSchemaLoader` can be overwritten in order to customize its behaviour in regards of authorization or error handling.

The `SchemaLoader` interface must implemented and the implementation configured on the `JsonSchemaFactory`.

```java
public class CustomUriSchemaLoader implements SchemaLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUriSchemaLoader.class);

    private final String        authorizationToken;

    private final HttpClient    client;

    public CustomUriSchemaLoader(String authorizationToken) {
        this.authorizationToken = authorizationToken;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public InputStreamSource getSchema(SchemaLocation schemaLocation) {
        URI uri = URI.create(schemaLocation.getAbsoluteIri().toString());
        return () -> {
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
}
```

Within the `JsonSchemaFactory` the custom `SchemaLoader` must be configured.

```java
CustomUriSchemaLoader uriSchemaLoader = new CustomUriSchemaLoader(authorizationToken);

JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder()
        .schemaLoaderBuilder(schemaLoaderBuilder -> schemaLoaderBuilder.schemaLoaders(schemaLoaders -> {
            for (int x = 0; x < schemaLoaders.size(); x++) {
                if (schemaLoaders.get(x) instanceof UriSchemaLoader) {
                    schemaLoaders.set(x, uriSchemaLoader);
                }
            }
        .addMetaSchema(JsonMetaSchema.getV7())
        .defaultMetaSchemaURI(JsonMetaSchema.getV7().getUri())
        .build();
JsonSchema jsonSchema = schemaFactory.getSchema(schemaUri);
for (ValidationMessage validationMessage : jsonSchema.validate(jsonNodeRecord)) {
    // handle the validation messages
}
```
