# OpenAPI Specification

The library includes support for the [OpenAPI Specification](https://swagger.io/specification/).

## Validating a request / response defined in an OpenAPI document

The library can be used to validate requests and responses with the use of the appropriate meta-schema.

| Dialect                                          | Meta-schema                                        |
|--------------------------------------------------|----------------------------------------------------|
| `https://spec.openapis.org/oas/3.0/dialect`      | `com.networknt.schema.oas.OpenApi30.getInstance()` |
| `https://spec.openapis.org/oas/3.1/dialect/base` | `com.networknt.schema.oas.OpenApi31.getInstance()` |

```java
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
        builder -> builder.metaSchema(OpenApi31.getInstance())
                .defaultMetaSchemaIri(OpenApi31.getInstance().getIri()));
JsonSchema schema = factory.getSchema(SchemaLocation.of(
        "classpath:schema/oas/3.1/petstore.yaml#/components/schemas/PetRequest"));
String input = "{\r\n"
        + "  \"petType\": \"dog\",\r\n"
        + "  \"bark\": \"woof\"\r\n"
        + "}";
Set<ValidationMessage> messages = schema.validate(input, InputFormat.JSON);
```

## Validating an OpenAPI document

The library can be used to validate OpenAPI documents, however the OpenAPI meta-schema documents are not bundled with the library.

It is recommended that the relevant meta-schema documents are placed in the classpath and are mapped otherwise they will be loaded over the internet.

The following are the documents required to validate a OpenAPI 3.1 document
* `https://spec.openapis.org/oas/3.1/schema-base/2022-10-07`
* `https://spec.openapis.org/oas/3.1/schema/2022-10-07`
* `https://spec.openapis.org/oas/3.1/dialect/base`
* `https://spec.openapis.org/oas/3.1/meta/base`

```java
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.setPathType(PathType.JSON_POINTER);
JsonSchema schema = JsonSchemaFactory
        .getInstance(VersionFlag.V202012,
                builder -> builder.schemaMappers(schemaMappers -> schemaMappers
                        .mapPrefix("https://spec.openapis.org/oas/3.1", "classpath:oas/3.1")))
        .getSchema(SchemaLocation.of("https://spec.openapis.org/oas/3.1/schema-base/2022-10-07"), config);
Set<ValidationMessage> messages = schema.validate(openApiDocument, InputFormat.JSON);
```
