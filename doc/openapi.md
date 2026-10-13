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

### Unknown-keyword diagnostics

When loading a schema by a JSON Pointer fragment, such as `#/components/schemas/PetRequest`,
either directly or through an external `$ref`,
unknown fields at the containing document's root are treated as annotations without
calling the unknown-keyword factory. Known keywords are still processed so that
reference resolution retains the document context. Unknown keywords in the selected
schema and its subschemas still use the configured factory. Loading the document
directly, without a fragment, or through an anchor fragment retains normal unknown-keyword diagnostics.
Container loads and direct schema loads use separate registry caches so diagnostics for
separate registry loads do not depend on which form was loaded first.
Within a single schema, references still share loaded documents: if a JSON Pointer
reference loads a document before a whole-document reference to it, the latter reuses
the container and does not report unknown root fields, even with a strict factory;
reversing that reference order retains normal root diagnostics.

In releases affected by [#1205](https://github.com/networknt/json-schema-validator/issues/1205),
these document fields warn once per name per JVM. A custom dialect can treat them as
`AnnotationKeyword` and delegate other names to `UnknownKeywordFactory`, at the cost
of accepting those names throughout the dialect. Setting the logger
`com.networknt.schema.keyword.UnknownKeywordFactory` to `ERROR` is a simpler workaround,
but also hides genuine unknown-keyword warnings.

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
