One of the features of this library is to validate the YAML file in addition to the JSON. In fact, the main use case for this library is to be part of the light-4j framework to validate the request/response at runtime against the OpenAPI specification file openapi.yaml.

### Usage

By default including the library would also include the `jackson-dataformat-yaml` unless explicitly excluded.

```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>2.17.1</version>
</dependency>
```

By default the object mapper from `YamlMapperFactory.getInstance()` will be used but this can configured by using a `JsonNodeReader`.

```java
ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
JsonNodeReader jsonNodeReader = JsonNodeReader.builder().yamlMapper(yamlMapper).build();
```

#### Example
```java
String schemaData = "---\r\n"
        + "\"$id\": 'https://schema/myschema'\r\n"
        + "properties:\r\n"
        + "  startDate:\r\n"
        + "    format: 'date'\r\n"
        + "    minLength: 6\r\n"
        + "";
String inputData = "---\r\n"
        + "startDate: '1'\r\n"
        + "";
ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
JsonNodeReader jsonNodeReader = JsonNodeReader.builder().yamlMapper(yamlMapper).build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
        builder -> builder.jsonNodeReader(jsonNodeReader).build());
SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
JsonSchema schema = factory.getSchema(schemaData, InputFormat.YAML, config);
Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.YAML, executionContext -> {
    executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
});
```

