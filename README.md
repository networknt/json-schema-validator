[Stack Overflow](https://stackoverflow.com/questions/tagged/light-4j) |
[Google Group](https://groups.google.com/forum/#!forum/light-4j) |
[Gitter Chat](https://gitter.im/networknt/json-schema-validator) |
[Subreddit](https://www.reddit.com/r/lightapi/) |
[Youtube](https://www.youtube.com/channel/UCHCRMWJVXw8iB7zKxF55Byw) |
[Documentation](https://doc.networknt.com/library/json-schema-validator/) |
[Contribution Guide](https://doc.networknt.com/contribute/) |

[![CI](https://github.com/networknt/json-schema-validator/actions/workflows/ci.yml/badge.svg)](https://github.com/networknt/json-schema-validator/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.networknt/json-schema-validator.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Acom.networknt%20a%3Ajson-schema-validator)
[![codecov.io](https://codecov.io/github/networknt/json-schema-validator/coverage.svg?branch=master)](https://codecov.io/github/networknt/json-schema-validator?branch=master)
[![Javadocs](http://www.javadoc.io/badge/com.networknt/json-schema-validator.svg)](https://www.javadoc.io/doc/com.networknt/json-schema-validator)

This is a Java implementation of the [JSON Schema Core Draft v4, v6, v7, v2019-09 and v2020-12](https://json-schema.org/specification) specification for JSON schema validation. This implementation supports [Customizing Meta-Schemas, Vocabularies, Keywords and Formats](doc/custom-meta-schema.md).

In addition, [OpenAPI](doc/openapi.md) 3 request/response validation is supported with the use of the appropriate meta-schema. For users who want to collect information from a JSON node based on the schema, the [walkers](doc/walkers.md) can help. The JSON parser used is the [Jackson](https://github.com/FasterXML/jackson) parser. As it is a key component in our [light-4j](https://github.com/networknt/light-4j) microservices framework to validate request/response against OpenAPI specification for [light-rest-4j](http://www.networknt.com/style/light-rest-4j/) and RPC schema for [light-hybrid-4j](http://www.networknt.com/style/light-hybrid-4j/) at runtime, performance is the most important aspect in the design.

## JSON Schema Specification compatibility

[![Supported Dialects](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fsupported_versions.json)](https://bowtie.report/#/implementations/java-networknt-json-schema-validator)
[![Draft 2020-12](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft2020-12.json)](https://bowtie.report/#/dialects/draft2020-12)
[![Draft 2019-09](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft2019-09.json)](https://bowtie.report/#/dialects/draft2019-09)
[![Draft 7](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft7.json)](https://bowtie.report/#/dialects/draft7)
[![Draft 6](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft6.json)](https://bowtie.report/#/dialects/draft6)
[![Draft 4](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft4.json)](https://bowtie.report/#/dialects/draft4)

Information on the compatibility support for each version, including known issues, can be found in the [Compatibility with JSON Schema versions](doc/compatibility.md) document.

Since [Draft 2019-09](https://json-schema.org/draft/2019-09/json-schema-validation#rfc.section.7) the `format` keyword only generates annotations by default and does not generate assertions.

This behavior can be overridden to generate assertions by setting the `setFormatAssertionsEnabled` to `true` in `SchemaValidatorsConfig` or `ExecutionConfig`.

## Upgrading to new versions

This library can contain breaking changes in `minor` version releases that may require code changes.

Information on notable or breaking changes when upgrading the library can be found in the [Upgrading to new versions](doc/upgrading.md) document.

The [Releases](https://github.com/networknt/json-schema-validator/releases) page will contain information on the latest versions.

## Comparing against other implementations

The [JSON Schema Validation Comparison](https://github.com/creek-service/json-schema-validation-comparison) project from Creek has an informative [Comparison of JVM based Schema Validation Implementations](https://www.creekservice.org/json-schema-validation-comparison/) which compares both the functional and performance characteristics of a number of different Java implementations.
* [Functional comparison](https://www.creekservice.org/json-schema-validation-comparison/functional#summary-results-table)
* [Performance comparison](https://www.creekservice.org/json-schema-validation-comparison/performance#json-schema-test-suite-benchmark)

The [Bowtie](https://github.com/bowtie-json-schema/bowtie) project has a [report](https://bowtie.report/) that compares functional characteristics of different implementations, including non-Java implementations, but does not do any performance benchmarking.

## Why this library

#### Performance

This should be the fastest Java JSON Schema Validator implementation.

The following is the benchmark results from the [JSON Schema Validator Perftest](https://github.com/networknt/json-schema-validator-perftest) project that uses the [Java Microbenchmark Harness](https://github.com/openjdk/jmh).

Note that the benchmark results are highly dependent on the input data workloads and schemas used for the validation.

In this case this workload is using the Draft 4 specification and largely tests the performance of the evaluating the `properties` keyword. You may refer to [Results of performance comparison of JVM based JSON Schema Validation Implementations](https://www.creekservice.org/json-schema-validation-comparison/performance#json-schema-test-suite-benchmark) for benchmark results for more typical workloads

If performance is an important consideration, the specific sample workloads should be benchmarked, as there are different performance characteristics when certain keywords are used. For instance the use of the `unevaluatedProperties` or `unevaluatedItems` keyword will trigger annotation collection in the related validators, such as the `properties` or `items` validators, and annotation collection will adversely affect performance.

##### NetworkNT 1.4.1

```
Benchmark                                            Mode  Cnt      Score    Error   Units
NetworkntBenchmark.testValidate                     thrpt   10   8352.126 ± 61.870   ops/s
NetworkntBenchmark.testValidate:gc.alloc.rate       thrpt   10    721.296 ±  5.342  MB/sec
NetworkntBenchmark.testValidate:gc.alloc.rate.norm  thrpt   10  90560.013 ±  0.001    B/op
NetworkntBenchmark.testValidate:gc.count            thrpt   10     61.000           counts
NetworkntBenchmark.testValidate:gc.time             thrpt   10     68.000               ms
```

###### Everit 1.14.1

```
Benchmark                                            Mode  Cnt       Score    Error   Units
EveritBenchmark.testValidate                        thrpt   10    3775.453 ± 44.023   ops/s
EveritBenchmark.testValidate:gc.alloc.rate          thrpt   10    1667.345 ± 19.437  MB/sec
EveritBenchmark.testValidate:gc.alloc.rate.norm     thrpt   10  463104.030 ±  0.003    B/op
EveritBenchmark.testValidate:gc.count               thrpt   10     140.000           counts
EveritBenchmark.testValidate:gc.time                thrpt   10     158.000               ms
```

#### Functionality

This implementation is tested against the [JSON Schema Test Suite](https://github.com/json-schema-org/JSON-Schema-Test-Suite). As tests are continually added to the suite, these test results may not be current.

| Implementations | Overall                                                                 | DRAFT_03                                                          | DRAFT_04                                                            | DRAFT_06                                                           | DRAFT_07                                                               | DRAFT_2019_09                                                        | DRAFT_2020_12                                                          |
|-----------------|-------------------------------------------------------------------------|-------------------------------------------------------------------|---------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------------------------------|----------------------------------------------------------------------|------------------------------------------------------------------------|
| NetworkNt       | pass: r:4803 (100.0%) o:2372 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)    |                                                                   | pass: r:610 (100.0%) o:251 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)  | pass: r:822 (100.0%) o:318 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) | pass: r:906 (100.0%) o:541 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)     | pass: r:1220 (100.0%) o:625 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)  | pass: r:1245 (100.0%) o:637 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)    |

* Note that this uses the `JoniRegularExpressionFactory` for the `pattern` and `format` `regex` tests.

#### Jackson Parser

This library uses [Jackson](https://github.com/FasterXML/jackson) which is a Java JSON parser that is widely used in other projects. If you are already using the Jackson parser in your project, it is natural to choose this library over others for schema validation.

#### YAML Support

The library works with JSON and YAML on both schema definitions and input data.

#### OpenAPI Support

The OpenAPI 3.0 specification is using JSON schema to validate the request/response, but there are some differences. With a configuration file, you can enable the library to work with OpenAPI 3.0 validation.

#### Minimal Dependencies

Following the design principle of the Light Platform, this library has minimal dependencies to ensure there are no dependency conflicts when using it.

##### Required Dependencies

The following are the dependencies that will automatically be included when this library is included.

```xml
<dependency>
    <!-- Used for logging -->
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>${version.slf4j}</version>
</dependency>

<dependency>
    <!-- Used to process JSON -->
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${version.jackson}</version>
</dependency>

<dependency>
    <!-- Used to process YAML -->
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
    <version>${version.jackson}</version>
</dependency>

<dependency>
    <!-- Used to validate RFC 3339 date and date-time -->
    <groupId>com.ethlo.time</groupId>
    <artifactId>itu</artifactId>
    <version>${version.itu}</version>
</dependency>
```

##### Optional Dependencies

The following are the optional dependencies that may be required for certain options.

These are not automatically included and setting the relevant option without adding the library will result in a `ClassNotFoundException`.

```xml
<dependency>
    <!-- Used to validate ECMA 262 regular expressions -->
    <!-- Approximately 50 MB in dependencies -->
    <!-- GraalJSRegularExpressionFactory -->
    <groupId>org.graalvm.js</groupId>
    <artifactId>js</artifactId>
    <version>${version.graaljs}</version>
</dependency>

<dependency>
    <!-- Used to validate ECMA 262 regular expressions -->
    <!-- Approximately 2 MB in dependencies -->
    <!-- JoniRegularExpressionFactory -->
    <groupId>org.jruby.joni</groupId>
    <artifactId>joni</artifactId>
    <version>${version.joni}</version>
</dependency>
```

##### Excludable Dependencies

The following are required dependencies that are automatically included, but can be explicitly excluded if they are not required.

The YAML dependency can be excluded if this is not required. Attempting to process schemas or input that are YAML will result in a `ClassNotFoundException`.

```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

The Ethlo Time dependency can be excluded if accurate validation of the `date-time` format is not required. The `date-time` format will then use `java.time.OffsetDateTime` to determine if the `date-time` is valid .

```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.ethlo.time</groupId>
            <artifactId>itu</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#### Community

This library is very active with a lot of contributors. New features and bug fixes are handled quickly by the team members. Because it is an essential dependency of the [light-4j](https://github.com/networknt/light-4j) framework in the same GitHub organization, it will be evolved and maintained along with the framework.

## Prerequisite

The library supports Java 8 and up. If you want to build from the source code, you need to install JDK 8 locally. To support multiple version of JDK, you can use [SDKMAN](https://www.networknt.com/tool/sdk/)

## Usage

### Adding the dependency

This package is available on Maven central.

#### Maven:

```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.5.5</version>
</dependency>
```

#### Gradle:

```java
dependencies {
    implementation(group: 'com.networknt', name: 'json-schema-validator', version: '1.5.5');
}
```

### Validating inputs against a schema

The following example demonstrates how inputs are validated against a schema. It comprises the following steps.

* Creating a schema factory with the default schema dialect and how the schemas can be retrieved.
  * Configuring mapping the `$id` to a retrieval URI using `schemaMappers`.
  * Configuring how the schemas are loaded using the retrieval URI using `schemaLoaders`.
    For instance a `Map<String, String> schemas` containing a mapping of retrieval URI to schema data as a `String` can by configured using `builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(schemas))`. This also accepts a `Function<String, String> schemaRetrievalFunction`.
* Creating a configuration for controlling validator behavior.
* Loading a schema from a schema location along with the validator configuration.
* Using the schema to validate the data along with setting any execution specific configuration like for instance the locale or whether format assertions are enabled.

```java
// This creates a schema factory that will use Draft 2020-12 as the default if $schema is not specified
// in the schema data. If $schema is specified in the schema data then that schema dialect will be used
// instead and this version is ignored.
JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder ->
    // This creates a mapping from $id which starts with https://www.example.org/ to the retrieval URI classpath:schema/
    builder.schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://www.example.org/", "classpath:schema/"))
);

SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
// By default the JDK regular expression implementation which is not ECMA 262 compliant is used
// Note that setting this requires including optional dependencies
// builder.regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
// builder.regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
SchemaValidatorsConfig config = builder.build();

// Due to the mapping the schema will be retrieved from the classpath at classpath:schema/example-main.json.
// If the schema data does not specify an $id the absolute IRI of the schema location will be used as the $id.
JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of("https://www.example.org/example-main.json"), config);
String input = "{\r\n"
    + "  \"main\": {\r\n"
    + "    \"common\": {\r\n"
    + "      \"field\": \"invalidfield\"\r\n"
    + "    }\r\n"
    + "  }\r\n"
    + "}";

Set<ValidationMessage> assertions = schema.validate(input, InputFormat.JSON, executionContext -> {
    // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
    executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
});
```

### Validating a schema against a meta-schema

The following example demonstrates how a schema is validated against a meta-schema.

This is actually the same as validating inputs against a schema except in this case the input is the schema and the schema used is the meta-schema.

Note that the meta-schemas for Draft 4, Draft 6, Draft 7, Draft 2019-09 and Draft 2020-12 are bundled with the library and these classpath resources will be used by default.

```java
JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012);

SchemaValidatorsConfig.Builder builder = SchemaValidatorsConfig.builder();
// By default the JDK regular expression implementation which is not ECMA 262 compliant is used
// Note that setting this requires including optional dependencies
// builder.regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
// builder.regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
SchemaValidatorsConfig config = builder.build();

// Due to the mapping the meta-schema will be retrieved from the classpath at classpath:draft/2020-12/schema.
JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V202012), config);
String input = "{\r\n"
    + "  \"type\": \"object\",\r\n"
    + "  \"properties\": {\r\n"
    + "    \"key\": {\r\n"
    + "      \"title\" : \"My key\",\r\n"
    + "      \"type\": \"invalidtype\"\r\n"
    + "    }\r\n"
    + "  }\r\n"
    + "}";
Set<ValidationMessage> assertions = schema.validate(input, InputFormat.JSON, executionContext -> {
    // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
    executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
});
```
### Results and output formats

#### Results

The following types of results are generated by the library.

| Type        | Description
|-------------|-------------------
| Assertions  | Validation errors generated by a keyword on a particular input data instance. This is generally described in a `ValidationMessage` or in a `OutputUnit`. Note that since Draft 2019-09 the `format` keyword no longer generates assertions by default and instead generates only annotations unless configured otherwise using a configuration option or by using a meta-schema that uses the appropriate vocabulary.
| Annotations | Additional information generated by a keyword for a particular input data instance. This is generally described in a `OutputUnit`. Annotation collection and reporting is turned off by default. Annotations required by keywords such as `unevaluatedProperties` or `unevaluatedItems` are always collected for evaluation purposes and cannot be disabled but will not be reported unless configured to do so.

The following information is used to describe both types of results.

| Type              | Description
|-------------------|-------------------
| Evaluation Path   | This is the set of keys from the root through which evaluation passes to reach the schema for evaluating the instance. This includes `$ref` and `$dynamicRef`. eg. ```/properties/bar/$ref/properties/bar-prop```
| Schema Location   | This is the canonical IRI of the schema plus the JSON pointer fragment to the schema that was used for evaluating the instance. eg. ```https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop```
| Instance Location | This is the JSON pointer fragment to the instance data that was being evaluated. eg. ```/bar/bar-prop```

Assertions contains the following additional information

| Type              | Description
|-------------------|-------------------
| Message           | The validation error message.
| Code              | The error code.
| Message Key       | The message key used for generating the message for localization.
| Arguments         | The arguments used for generating the message.
| Type              | The keyword that generated the message.
| Property          | The property name that caused the validation error for example for the `required` keyword. Note that this is not part of the instance location as that points to the instance node.
| Schema Node       | The `JsonNode` pointed to by the Schema Location. This is the schema data that caused the input data to fail. It is possible to get the location information by configuring the `JsonSchemaFactory` with a `JsonNodeReader` that uses the `LocationJsonNodeFactoryFactory` and using `JsonNodes.tokenLocationOf(schemaNode)`.
| Instance Node     | The `JsonNode` pointed to by the Instance Location. This is the input data that failed validation. It is possible to get the location information by configuring the `JsonSchemaFactory` with a `JsonNodeReader` that uses the `LocationJsonNodeFactoryFactory` and using `JsonNodes.tokenLocationOf(instanceNode)`.
| Error             | The error.
| Details           | Additional details that can be set by custom keyword validator implementations. This is not used by the library.

Annotations contains the following additional information

| Type              | Description
|-------------------|-------------------
| Value             | The annotation value generated

##### Line and Column Information

The library can be configured to store line and column information in the `JsonNode` instances for the instance and schema nodes. This will adversely affect performance and is not configured by default.

This is done by configuring a `JsonNodeReader` that uses the `LocationJsonNodeFactoryFactory`on the `JsonSchemaFactory`. The `JsonLocation` information can then be retrieved using `JsonNodes.tokenLocationOf(jsonNode)`.

```java
String schemaData = "{\r\n"
                + "  \"$id\": \"https://schema/myschema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"startDate\": {\r\n"
                + "      \"format\": \"date\",\r\n"
                + "      \"minLength\": 6\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
String inputData = "{\r\n"
                + "  \"startDate\": \"1\"\r\n"
                + "}";
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
        builder -> builder.jsonNodeReader(JsonNodeReader.builder().locationAware().build()));
SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
JsonSchema schema = factory.getSchema(schemaData, InputFormat.JSON, config);
Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON, executionContext -> {
    executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
});
List<ValidationMessage> list = messages.stream().collect(Collectors.toList());
ValidationMessage format = list.get(0);
JsonLocation formatInstanceNodeTokenLocation = JsonNodes.tokenLocationOf(format.getInstanceNode());
JsonLocation formatSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(format.getSchemaNode());
ValidationMessage minLength = list.get(1);
JsonLocation minLengthInstanceNodeTokenLocation = JsonNodes.tokenLocationOf(minLength.getInstanceNode());
JsonLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(minLength.getSchemaNode());

assertEquals("format", format.getType());
assertEquals("date", format.getSchemaNode().asText());
assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
assertEquals(17, formatSchemaNodeTokenLocation.getColumnNr());
assertEquals("1", format.getInstanceNode().asText());
assertEquals(2, formatInstanceNodeTokenLocation.getLineNr());
assertEquals(16, formatInstanceNodeTokenLocation.getColumnNr());
assertEquals("minLength", minLength.getType());
assertEquals("6", minLength.getSchemaNode().asText());
assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
assertEquals(20, minLengthSchemaNodeTokenLocation.getColumnNr());
assertEquals("1", minLength.getInstanceNode().asText());
assertEquals(2, minLengthInstanceNodeTokenLocation.getLineNr());
assertEquals(16, minLengthInstanceNodeTokenLocation.getColumnNr());
assertEquals(16, minLengthInstanceNodeTokenLocation.getColumnNr());
```


#### Output formats

This library implements the Flag, List and Hierarchical output formats defined in the [Specification for Machine-Readable Output for JSON Schema Validation and Annotation](https://github.com/json-schema-org/json-schema-spec/blob/8270653a9f59fadd2df0d789f22d486254505bbe/jsonschema-validation-output-machines.md).

The List and Hierarchical output formats are particularly helpful for understanding how the system arrived at a particular result.

| Output Format     | Description
|-------------------|-------------------
| Default           | Generates the list of assertions.
| Boolean           | Returns `true` if the validation is successful. Note that the fail fast option is turned on by default for this output format.
| Flag              | Returns an `OutputFlag` object with `valid` having `true` if the validation is successful. Note that the fail fast option is turned on by default for this output format.
| List              | Returns an `OutputUnit` object with `details` with a list of `OutputUnit` objects with the assertions and annotations. Note that annotations are not collected by default and it has to be enabled as it will impact performance.
| Hierarchical      | Returns an `OutputUnit` object with a hierarchy of `OutputUnit` objects for the evaluation path with the assertions and annotations. Note that annotations are not collected by default and it has to be enabled as it will impact performance.

The following example shows how to generate the hierarchical output format with annotation collection and reporting turned on and format assertions turned on.

```java
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
SchemaValidatorsConfig config = SchemaValidatorsConfig().builder().formatAssertionsEnabled(true).build();
JsonSchema schema = factory.getSchema(SchemaLocation.of("https://json-schema.org/schemas/example"), config);

OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL, executionContext -> {
    executionContext.getExecutionConfig().setAnnotationCollectionEnabled(true);
    executionContext.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
});
```
The following is sample output from the Hierarchical format.

```json
{
  "valid" : false,
  "evaluationPath" : "",
  "schemaLocation" : "https://json-schema.org/schemas/example#",
  "instanceLocation" : "",
  "droppedAnnotations" : {
    "properties" : [ "foo", "bar" ],
    "title" : "root"
  },
  "details" : [ {
    "valid" : false,
    "evaluationPath" : "/properties/foo/allOf/0",
    "schemaLocation" : "https://json-schema.org/schemas/example#/properties/foo/allOf/0",
    "instanceLocation" : "/foo",
    "errors" : {
      "required" : "required property 'unspecified-prop' not found"
    }
  }, {
    "valid" : false,
    "evaluationPath" : "/properties/foo/allOf/1",
    "schemaLocation" : "https://json-schema.org/schemas/example#/properties/foo/allOf/1",
    "instanceLocation" : "/foo",
    "droppedAnnotations" : {
      "properties" : [ "foo-prop" ],
      "title" : "foo-title",
      "additionalProperties" : [ "foo-prop", "other-prop" ]
    },
    "details" : [ {
      "valid" : false,
      "evaluationPath" : "/properties/foo/allOf/1/properties/foo-prop",
      "schemaLocation" : "https://json-schema.org/schemas/example#/properties/foo/allOf/1/properties/foo-prop",
      "instanceLocation" : "/foo/foo-prop",
      "errors" : {
        "const" : "must be a constant value 1"
      },
      "droppedAnnotations" : {
        "title" : "foo-prop-title"
      }
    } ]
  }, {
    "valid" : false,
    "evaluationPath" : "/properties/bar/$ref",
    "schemaLocation" : "https://json-schema.org/schemas/example#/$defs/bar",
    "instanceLocation" : "/bar",
    "droppedAnnotations" : {
      "properties" : [ "bar-prop" ],
      "title" : "bar-title"
    },
    "details" : [ {
      "valid" : false,
      "evaluationPath" : "/properties/bar/$ref/properties/bar-prop",
      "schemaLocation" : "https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop",
      "instanceLocation" : "/bar/bar-prop",
      "errors" : {
        "minimum" : "must have a minimum value of 10"
      },
      "droppedAnnotations" : {
        "title" : "bar-prop-title"
      }
    } ]
  } ]
}
```

## Configuration

### Execution Configuration

| Name                           | Description                                                                                                                                                                                                                       | Default Value
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------
| `annotationCollectionEnabled`  | Controls whether annotations are collected during processing. Note that collecting annotations will adversely affect performance.                                                                                                 | `false`
| `annotationCollectionFilter`   | The predicate used to control which keyword to collect and report annotations for. This requires `annotationCollectionEnabled` to be `true`.                                                                                      | `keyword -> false`
| `locale`                       | The locale to use for generating messages in the `ValidationMessage`. Note that this value is copied from `SchemaValidatorsConfig` for each execution.                                                                            | `Locale.getDefault()`
| `failFast`                     | Whether to return failure immediately when an assertion is generated. Note that this value is copied from `SchemaValidatorsConfig` for each execution but is automatically set to `true` for the Boolean and Flag output formats. | `false`
| `formatAssertionsEnabled`      | The default is to generate format assertions from Draft 4 to Draft 7 and to only generate annotations from Draft 2019-09. Setting to `true` or `false` will override the default behavior.                                        | `null`
| `debugEnabled`                 | Controls whether debug logging is enabled for logging the node information when processing. Note that this will generate a lot of logs that will affect performance.                                                              | `false`

### Schema Validators Configuration

| Name                                  | Description                                                                                                                                                                                                                       | Default Value
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------
| `applyDefaultsStrategy`               | The strategy for applying defaults when walking when missing or null nodes are encountered.                                                                                                                                       | `ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY`
| `cacheRefs`                           | Whether the schemas loaded from refs will be cached and reused for subsequent runs. Setting this to `false` will affect performance but may be neccessary to prevent high memory usage for the cache if multiple nested applicators like `anyOf`, `oneOf` and `allOf` are used.  | `true`
| `discriminatorKeywordEnabled`         | Whether the `discriminator` keyword is handled according to OpenAPI 3.                                                                                                                                                            | `false`
| `errorMessageKeyword`                 | The keyword to use for custom error messages in the schema. If not set this features is disabled. This is typically set to `errorMessage` or `message`.                                                                           | `null`
| `executionContextCustomizer`          | This can be used to customize the `ExecutionContext` generated by the `JsonSchema` for each validation run.                                                                                                                       | `null`
| `failFast`                            | Whether to return failure immediately when an assertion is generated.                                                                                                                                                             | `false`
| `formatAssertionsEnabled`             | The default is to generate format assertions from Draft 4 to Draft 7 and to only generate annotations from Draft 2019-09. Setting to `true` or `false` will override the default behavior.                                        | `null`
| `javaSemantics`                       | Whether java semantics is used for the `type` keyword.                                                                                                                                                                            | `false`
| `locale`                              | The locale to use for generating messages in the `ValidationMessage`.                                                                                                                                                             | `Locale.getDefault()`
| `losslessNarrowing`                   | Whether lossless narrowing is used for the `type` keyword.                                                                                                                                                                        | `false`
| `messageSource`                       | This is used to retrieve the locale specific messages.                                                                                                                                                                            | `DefaultMessageSource.getInstance()`
| `nullableKeywordEnabled`              | Whether the `nullable` keyword is handled according to OpenAPI 3.0. This affects the `enum` and `type` keywords.                                                                                                                  | `false`
| `pathType`                            | The path type to use for reporting the instance location and evaluation path. Set to `PathType.JSON_PATH` to use JSON Path.                                                                                                       | `PathType.JSON_POINTER`
| `preloadJsonSchema`                   | Whether the schema will be preloaded before processing any input. This will use memory but the execution of the validation will be faster.                                                                                        | `true`
| `preloadJsonSchemaRefMaxNestingDepth` | The max depth of the evaluation path to preload when preloading refs.                                                                                                                                                             | `40`
| `readOnly`                            | Whether schema is read only. This affects the `readOnly` keyword.                                                                                                                                                                 | `null`
| `regularExpressionFactory`            | The factory to use to create regular expressions for instance `JoniRegularExpressionFactory` or `GraalJSRegularExpressionFactory`. This requires the dependency to be manually added to the project or a `ClassNotFoundException` will be thrown. | `JDKRegularExpressionFactory.getInstance()`
| `schemaIdValidator`                   | This is used to customize how the `$id` values are validated. Note that the default implementation allows non-empty fragments where no base IRI is specified and also allows non-absolute IRI `$id` values in the root schema.    | `JsonSchemaIdValidator.DEFAULT`
| `strict`                              | This is set whether keywords are strict in their validation. What this does depends on the individual validators.                                                                                                                 |
| `typeLoose`                           | Whether types are interpreted in a loose manner. If set to true, a single value can be interpreted as a size 1 array. Strings may also be interpreted as number, integer or boolean.                                              | `false`
| `writeOnly`                           | Whether schema is write only. This affects the `writeOnly` keyword.                                                                                                                                                               | `null`

## Performance Considerations

When the library creates a schema from the schema factory, it creates a distinct validator instance for each location on the evaluation path. This means if there are different `$ref` that reference the same schema location, different validator instances are created for each evaluation path.

When the schema is created, the library will by default automatically preload all the validators needed and resolve references. This can be disabled with the `preloadJsonSchema` option in the `SchemaValidatorsConfig`. At this point, no exceptions will be thrown if a reference cannot be resolved. If there are references that are cyclic, only the first cycle will be preloaded. If you wish to ensure that remote references can all be resolved, the `initializeValidators` method needs to be called on the `JsonSchema` which will throw an exception if there are references that cannot be resolved.

Instances for `JsonSchemaFactory` and the `JsonSchema` created from it are designed to be thread-safe provided its configuration is not modified and should be cached and reused. Not reusing the `JsonSchema` means that the schema data needs to be repeated parsed with validator instances created and references resolved. When references are resolved, the validators created will be cached. For schemas that have deeply nested references, the memory needed for the validators may be very high, in which case the caching may need to be disabled using the `cacheRefs` option in the `SchemaValidatorsConfig`. Disabling this will mean the validators from the references need to be re-created for each validation run which will impact performance.

Collecting annotations will adversely affect validation performance.

The earlier draft specifications contain less keywords that can potentially impact performance. For instance the use of the `unevaluatedProperties` or `unevaluatedItems` keyword will trigger annotation collection in the related validators, such as the `properties` or `items` validators.

This does not mean that using a schema with a later draft specification will automatically cause a performance impact. For instance, the `properties` validator will perform checks to determine if annotations need to be collected, and checks if the meta-schema contains the `unevaluatedProperties` keyword and whether the `unevaluatedProperties` keyword exists adjacent the evaluation path.

## Security Considerations

The library assumes that the schemas being loaded are trusted. This security model assumes the use case where the schemas are bundled with the application on the classpath.

| Issue                 | Description                                                                                                                                                                                                                                          | Mitigation
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------
| Schema Loading        | The library by default will load schemas from the classpath and over the internet if needed.                                                                                                                                                         | A `DisallowSchemaLoader` can be configured to not allow schema retrieval. Alternatively an `AllowSchemaLoader` can be configured to restrict the retrieval IRIs that are allowed.
| Schema Caching        | The library by default preloads and caches references when loading schemas. While there is a max nesting depth when preloading schemas it is still possible to construct a schema that has a fan out that consumes a lot of memory from the server.  | Set `cacheRefs` option in `SchemaValidatorsConfig` to false.
| Regular Expressions   | The library does not validate if a given regular expression is susceptable to denial of service ([ReDoS](https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS)).                                                     | An `AllowRegularExpressionFactory` can be configured to perform validation on the regular expressions that are allowed.
| Validation Errors     | The library by default attempts to return all validation errors. The use of applicators such as `allOf` with a large number of schemas may result in a large number of validation errors taking up memory.                                           | Set `failFast` option in `SchemaValidatorsConfig` to immediately return when the first error is encountered. The `OutputFormat.BOOLEAN` or `OutputFormat.FLAG` also can be used.

## [Quick Start](doc/quickstart.md)

## [Customizing Schema Retrieval](doc/schema-retrieval.md)

## [Customizing Meta-Schemas, Vocabularies, Keywords and Formats](doc/custom-meta-schema.md)

## [OpenAPI Specification](doc/openapi.md)

## [Validators](doc/validators.md)

## [Configuration](doc/config.md)

## [Specification Version](doc/specversion.md)

## [YAML Validation](doc/yaml.md)

## [Collector Context](doc/collector-context.md)

## [JSON Schema Walkers and WalkListeners](doc/walkers.md)

## [Regular Expressions](doc/ecma-262.md)

## [Custom Error Messages](doc/cust-msg.md)

## [Multiple Language](doc/multiple-language.md)

## [MetaSchema Validation](doc/metaschema-validation.md)

## [Validating RFC 3339 durations](doc/duration.md)

## Projects

The [light-rest-4j](https://github.com/networknt/light-rest-4j), [light-graphql-4j](https://github.com/networknt/light-graphql-4j) and [light-hybrid-4j](https://github.com/networknt/light-hybrid-4j) use this library to validate the request and response based on the specifications. If you are using other frameworks like Spring Boot, you can use the [OpenApiValidator](https://github.com/mservicetech/openapi-schema-validation), a generic OpenAPI 3.0 validator based on the OpenAPI 3.0 specification.

If you have a project using this library, please submit a PR to add your project below.

## Contributors

Thanks to the following people who have contributed to this project. If you are using this library, please consider to be a sponsor for one of the contributors.

[@stevehu](https://github.com/sponsors/stevehu)

[@prashanth-chaitanya](https://github.com/prashanth-chaitanya)

[@fdutton](https://github.com/fdutton)

[@valfirst](https://github.com/valfirst)

[@BalloonWen](https://github.com/BalloonWen)

[@jiachen1120](https://github.com/jiachen1120)

[@ddobrin](https://github.com/ddobrin)

[@eskabetxe](https://github.com/eskabetxe)

[@ehrmann](https://github.com/ehrmann)

[@prashanthjos](https://github.com/prashanthjos)

[@Subhajitdas298](https://github.com/Subhajitdas298)

[@FWiesner](https://github.com/FWiesner)

[@rhwood](https://github.com/rhwood)

[@jawaff](https://github.com/jawaff)

[@nitin1891](https://github.com/nitin1891)


For all contributors, please visit https://github.com/networknt/json-schema-validator/graphs/contributors

If you are a contributor, please join the [GitHub Sponsors](https://github.com/sponsors) and switch the link to your sponsors dashboard via a PR.

## Sponsors


### Individual Sponsors


### Corporation Sponsors
