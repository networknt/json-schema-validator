### Configuration

To control the behavior of the library, we have introduced SchemaValidatorsConfig recently. It gives users great flexibility when using the library in different contexts. 

For some users, it is just a JSON schema validator implemented mainly based on v4 with some additions from v5 to v7. 

For others, it is used as a critical component in the REST API frameworks to validate the request or response. The library was developed as part of the [light-4j](https://github.com/networknt/light-4j) framework in the beginning. 

Most of the configuration flags are used to control the difference between Swagger/OpenAPI specification and JSON schema specification as they are not the same. The future of the OpenAPI version might resolve this problem, but the release date is not set yet. 

#### How to use config

When you create a `JsonSchema` instance from the `JsonSchemaFactory`, you can pass an object of SchemaValidatorsConfig as the second parameter. 

```java
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.setTypeLoose(false);
JsonSchema jsonSchema = JsonSchemaFactory.getInstance().getSchema(schema, config);
```

#### Configurations

* typeLoose

When typeLoose is true, the validator will convert strings to different types to match the type defined in the schema. This is mostly used to validate the JSON request or response for headers, query parameters, path parameters, and cookies. For the HTTP protocol, these are all strings and might be defined as other types in the schema. For example, the page number might be an integer in the schema but passed as a query parameter in string. 

* strictness
This is a map of keywords to whether the keyword's validators should perform a strict or permissive analysis. When strict is true, validators will perform strict checking against the schema.
This is the default behavior. When set to false, validators are free to relax some constraints but not required. Each validator has its own understanding of what constitutes strict and
permissive.

* failFast

When set to true, the validation process stops immediately when the first error occurs. This mostly used on microservices that is designed to [fail-fast](https://www.networknt.com/architecture/fail-fast/), or users don't want to see hundreds of errors for a big payload. Please be aware that the validator throws an exception in the case the first error occurs. To learn how to use it, please follow the [test case](https://github.com/networknt/json-schema-validator/blob/master/src/test/java/com/networknt/schema/V4JsonSchemaTest.java#L352). 

* handleNullableField

When a field is set as nullable in the OpenAPI specification, the schema validator validates that it is nullable; however, it continues with validation against the nullable field.

If handleNullableField is set to true && incoming field is nullable && value is field: null --> succeed

If handleNullableField is set to false && incoming field is nullable && value is field: null --> it is up to the type validator using the SchemaValidator to handle it.

The default value is true in the SchemaValidatorsConfig object. 

For more details, please refer to this [issue](https://github.com/networknt/json-schema-validator/issues/183). 

* javaSemantics

When set to true, use Java-specific semantics rather than native JavaScript semantics.

For example, if the node type is `number` per JS semantics where the value can be losslesly interpreted as `java.lang.Long`, the validator would use `integer` as the node type instead of `number`. This is useful when schema type is `integer`, since validation would fail otherwise.

For more details, please refer to this [issue](https://github.com/networknt/json-schema-validator/issues/334).

* losslessNarrowing

When set to true, can interpret round doubles as integers.

Note that setting `javaSemantics = true` will achieve the same functionality at this time.

For more details, please refer to this [issue](https://github.com/networknt/json-schema-validator/issues/344).

* pathType

This defines how path expressions are defined and returned once validation is performed through `ValidationMessage` instances. This can either be set to `PathType.JSON_POINTER` for [JSONPointer](https://www.rfc-editor.org/rfc/rfc6901.html) expressions,
or to `PathType.JSON_PATH` for [JSONPath](https://datatracker.ietf.org/doc/draft-ietf-jsonpath-base/) expressions. Doing so allows you to report the path for each finding and to potentially lookup nodes
(see [here](https://github.com/networknt/json-schema-validator/blob/c41df270a71f8423c63cfaa379d2e9b3f570b73e/doc/yaml-line-numbers.md#scenario-2---validationmessage-line-locations) for an example). By default, path expressions use a
`PathType.LEGACY` format which is close to JSONPath but does not escape reserved characters.
