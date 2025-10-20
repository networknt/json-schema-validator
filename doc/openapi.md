# OpenAPI Specification

The library includes support for the [OpenAPI Specification](https://swagger.io/specification/).

## Validating a request / response defined in an OpenAPI document

The library can be used to validate requests and responses with the use of the appropriate dialect.

| Dialect                                          | Instance                                               |
| ------------------------------------------------ | ------------------------------------------------------ |
| `https://spec.openapis.org/oas/3.0/dialect`      | `com.networknt.schema.dialect.Dialects.getOpenApi30()` |
| `https://spec.openapis.org/oas/3.1/dialect/base` | `com.networknt.schema.dialect.Dialects.getOpenApi31()` |

```java
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getOpenApi31());
Schema schema = schemaRegistry.getSchema(SchemaLocation.of(
        "classpath:schema/oas/3.1/petstore.yaml#/components/schemas/PetRequest"));
String input = "{\r\n"
        + "  \"petType\": \"dog\",\r\n"
        + "  \"bark\": \"woof\"\r\n"
        + "}";
List<Error> errors = schema.validate(input, InputFormat.JSON);
```

## Validating an OpenAPI document

The library can be used to validate OpenAPI documents, however the OpenAPI meta-schema documents are not bundled with the library.

It is recommended that the relevant meta-schema documents are placed in the classpath and are mapped otherwise they will be loaded over the internet.

The following are the documents required to validate a OpenAPI 3.1 document

- `https://spec.openapis.org/oas/3.1/schema-base/2022-10-07`
- `https://spec.openapis.org/oas/3.1/schema/2022-10-07`
- `https://spec.openapis.org/oas/3.1/dialect/base`
- `https://spec.openapis.org/oas/3.1/meta/base`

```java
Schema schema = SchemaRegistry
        .withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaIdResolvers(schemaIdResolvers -> schemaIdResolvers
                        .mapPrefix("https://spec.openapis.org/oas/3.1", "classpath:oas/3.1")))
        .getSchema(SchemaLocation.of("https://spec.openapis.org/oas/3.1/schema-base/2022-10-07"));
List<Error> errors = schema.validate(openApiDocument, InputFormat.JSON);
```
