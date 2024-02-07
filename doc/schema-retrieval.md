# Customizing Schema Retrieval

A schema can be identified by its schema identifier which is indicated using the `$id` keyword or `id` keyword in earlier drafts. This is an absolute IRI that uniquely identifies the schema and is not necessarily a network locator. A schema need not be downloadable from it's absolute IRI.

In the event a schema references a schema identifier that is not a subschema resource, for instance defined in the `$defs` keyword or `definitions` keyword. The library will need to be able to retrieve the schema given its schema identifier.

In the event that the schema does not define a schema identifier using the `$id` keyword, the retrieval IRI will be used as it's schema identifier.

## Loading Schemas from memory

Schemas can be loaded through a map.

```java
String schemaData = "{\r\n"
        + "  \"type\": \"integer\"\r\n"
        + "}";
Map<String, String> schemas = Collections.singletonMap("https://www.example.com/integer.json", schemaData); 
JsonSchemaFactory schemaFactory = JsonSchemaFactory
    .getInstance(VersionFlag.V7,
        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(schemas)));
```

Schemas can be loaded through a function.

```java
String schemaData = "{\r\n"
        + "  \"type\": \"integer\"\r\n"
        + "}";
Map<String, String> schemas = Collections.singletonMap("https://www.example.com/integer.json", schemaData); 
    JsonSchemaFactory schemaFactory = JsonSchemaFactory
        .getInstance(VersionFlag.V7,
            builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(schemas::get)));
```

Schemas can also be loaded in the following manner.

```java
class RegistryEntry {
    private final String schemaData;

    public RegistryEntry(String schemaData) {
        this.schemaData = schemaData;
    }

    public String getSchemaData() {
        return this.schemaData;
    }
}

String schemaData = "{\r\n"
        + "  \"type\": \"integer\"\r\n"
        + "}";
Map<String, RegistryEntry> registry = Collections
    .singletonMap("https://www.example.com/integer.json", new RegistryEntry(schemaData));
JsonSchemaFactory schemaFactory = JsonSchemaFactory
    .getInstance(VersionFlag.V7, builder -> builder
        .schemaLoaders(schemaLoaders -> schemaLoaders.schemas(registry::get, RegistryEntry::getSchemaData)));
```

## Mapping Schema Identifier to Retrieval IRI

The schema identifier can be mapped to the retrieval IRI by implementing the `SchemaMapper` interface.

### Configuring Schema Mapper

```java
class CustomSchemaMapper implements SchemaMapper {
    @Override
    public AbsoluteIri map(AbsoluteIri absoluteIRI) {
        String iri = absoluteIRI.toString();
        if ("https://www.example.com/integer.json".equals(iri)) {
            return AbsoluteIri.of("classpath:schemas/integer.json");
        }
        return null;
    }
}

JsonSchemaFactory schemaFactory = JsonSchemaFactory
    .getInstance(VersionFlag.V7,
        builder -> builder.schemaMappers(schemaMappers -> schemaMappers.add(new CustomSchemaMapper())));
```

### Configuring Prefix Mappings

```java
JsonSchemaFactory schemaFactory = JsonSchemaFactory
    .getInstance(VersionFlag.V7,
        builder -> builder
            .schemaMappers(schemaMappers -> schemaMappers
                .mapPrefix("https://json-schema.org", "classpath:")
                .mapPrefix("http://json-schema.org", "classpath:")));
```

### Configuring Mappings

```java
Map<String, String> mappings = Collections
    .singletonMap("https://www.example.com/integer.json", "classpath:schemas/integer.json");

JsonSchemaFactory schemaFactory = JsonSchemaFactory
    .getInstance(VersionFlag.V7,
        builder -> builder.schemaMappers(schemaMappers -> schemaMappers.mappings(mappings)));
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
    public InputStreamSource getSchema(AbsoluteIri absoluteIri) {
        String scheme = absoluteIri.getScheme();
        if ("https".equals(scheme) || "http".equals(scheme)) {
            URI uri = URI.create(absoluteIri.toString());
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
        return null;
    }
}
```

Within the `JsonSchemaFactory` the custom `SchemaLoader` must be configured.

```java
CustomUriSchemaLoader uriSchemaLoader = new CustomUriSchemaLoader(authorizationToken);

JsonSchemaFactory schemaFactory = JsonSchemaFactory
    .getInstance(VersionFlag.V7,
        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.add(uriSchemaLoader)));
```
