While working with JSON schema validation, we have to use external references sometimes. However, there are two issues to have references to schemas on the Internet. 

* Some applications are running inside a corporate network without Internet access. 
* Some of the Internet resources are not reliable.
* A test environment may serve unpublished schemas, which are not yet available at the location identified by the payload's `$schema` property.

One solution is to change all the external reference to internal in JSON schemas, but this is error-prone and hard to maintain in a long run. 

A smart solution is to map the external references to internal ones in a configuration file. This allows us to use the resources as they are without any modification. In the JSON schema specification, it is not allowed to use local filesystem resource directly. With the mapping, we can use the local resources without worrying about breaking the specification as the references are still in URL format in schemas. In addition, the mapped URL can be a different external URL, or embbeded within a JAR file with a lot more flexibility. 

Note that when using a mapping, the local copy is always used, and the external reference is not queried.

### URI Translation

Both `SchemaValidatorsConfig` and `JsonSchemaFactory` accept one or more `URITranslator` instances. A `URITranslator` is responsible for providing a new URI when the given URI matches certain criteria.

#### Examples

Automatically map HTTP to HTTPS

```java
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.addUriTranlator(uri -> {
    if ("http".equalsIgnoreCase(uri.getScheme()) {
        try {
            return new URI(
                "https",
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
            );
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
    }
    return uri;
});
```

Map a public schema to a test environment

```java
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.addUriTranlator(uri -> {
    if (true
        && "https".equalsIgnoreCase(uri.getScheme()
        && "schemas.acme.org".equalsIgnoreCase(uri.getHost())
        && (-1 == uri.getPort() || 443 == uri.getPort())
    ) {
        try {
            return new URI(
                "http",
                uri.getUserInfo(),
                "test-schemas.acme.org",
                8080,
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
            );
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
    }
    return uri;
});
```

Replace a URI with another

**Note:**
This also works for mapping URNs to resources.

```java
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.addUriTranlator(URITranslator.map("https://schemas.acme.org/Foo", "classpath://Foo");
```

### Precedence

Both `SchemaValidatorsConfig` and `JsonSchemaFactory` accept multiple `URITranslator`s and in general, they are evaluated in the order of addition. This means that each `URITranslator` receives the output of the previous translator. For example, assuming the following configuration:

```
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.addUriTranlator(uri -> {
    if ("http".equalsIgnoreCase(uri.getScheme()) {
        try {
            return new URI(
                "https",
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
            );
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
    }
    return uri;
});
config.addUriTranlator(uri -> {
    if (true
        && "https".equalsIgnoreCase(uri.getScheme()
        && "schemas.acme.org".equalsIgnoreCase(uri.getHost())
        && (-1 == uri.getPort() || 443 == uri.getPort())
    ) {
        try {
            return new URI(
                "http",
                uri.getUserInfo(),
                "test-schemas.acme.org",
                8080,
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
            );
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x.getMessage(), x);
        }
    }
    return uri;
});
config.addUriTranlator(URITranslator.map("http://test-schemas.acme.org:8080/Foo", "classpath://Foo");
```

Given a starting URI of `https://schemas.acme.org/Foo`, the configuration above produces the following translations (in order):

1. The translation from HTTP to HTTPS does not occur since the original URI already specifies HTTPS.
2. The second rule receives the original URI since nothing happened in the first rule. The second rule translates the URI from `https://schemas.acme.org/Foo` to `http://test-schemas.acme.org:8080/Foo` since the scheme, host and port match this rule.
3. The third rule receives the URI produced by the second rule and performs a simple mapping to a local resource.

Since all `JsonSchemaFactory`s are created from an optional `SchemaValidatorsConfig`, any `URITranslator`s added to the factory are evaluated after those provided by `SchemaValidatorsConfig`.

### Deprecated

Previously, this library supported simple mappings from one URI to another through `SchemaValidatorsConfig.setUriMappings()` and `JsonSchemaFactory.addUriMappings()`. Usage of these methods are still supported but are now discouraged. `URITranslator` provides a more powerful mechanism of dealing with URI mapping than what was provided before.

