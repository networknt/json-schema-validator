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

##### NetworkNT 1.3.1

```
Benchmark                                                          Mode  Cnt       Score      Error   Units
NetworkntBenchmark.testValidate                                   thrpt   10    6776.693 ±  115.309   ops/s
NetworkntBenchmark.testValidate:·gc.alloc.rate                    thrpt   10     971.191 ±   16.420  MB/sec
NetworkntBenchmark.testValidate:·gc.alloc.rate.norm               thrpt   10  165318.816 ±    0.459    B/op
NetworkntBenchmark.testValidate:·gc.churn.G1_Eden_Space           thrpt   10     968.894 ±   51.234  MB/sec
NetworkntBenchmark.testValidate:·gc.churn.G1_Eden_Space.norm      thrpt   10  164933.962 ± 8636.203    B/op
NetworkntBenchmark.testValidate:·gc.churn.G1_Survivor_Space       thrpt   10       0.002 ±    0.001  MB/sec
NetworkntBenchmark.testValidate:·gc.churn.G1_Survivor_Space.norm  thrpt   10       0.274 ±    0.218    B/op
NetworkntBenchmark.testValidate:·gc.count                         thrpt   10      89.000             counts
NetworkntBenchmark.testValidate:·gc.time                          thrpt   10      99.000                 ms
```

###### Everit 1.14.1

```
Benchmark                                                          Mode  Cnt       Score       Error   Units
EveritBenchmark.testValidate                                      thrpt   10    3719.192 ±   125.592   ops/s
EveritBenchmark.testValidate:·gc.alloc.rate                       thrpt   10    1448.208 ±    74.746  MB/sec
EveritBenchmark.testValidate:·gc.alloc.rate.norm                  thrpt   10  449621.927 ±  7400.825    B/op
EveritBenchmark.testValidate:·gc.churn.G1_Eden_Space              thrpt   10    1446.397 ±    79.919  MB/sec
EveritBenchmark.testValidate:·gc.churn.G1_Eden_Space.norm         thrpt   10  449159.799 ± 18614.931    B/op
EveritBenchmark.testValidate:·gc.churn.G1_Survivor_Space          thrpt   10       0.001 ±     0.001  MB/sec
EveritBenchmark.testValidate:·gc.churn.G1_Survivor_Space.norm     thrpt   10       0.364 ±     0.391    B/op
EveritBenchmark.testValidate:·gc.count                            thrpt   10     133.000              counts
EveritBenchmark.testValidate:·gc.time                             thrpt   10     148.000                  ms
```

#### Functionality

This implementation is tested against the [JSON Schema Test Suite](https://github.com/json-schema-org/JSON-Schema-Test-Suite). As tests are continually added to the suite, these test results may not be current.

| Implementations | Overall                                                                 | DRAFT_03                                                          | DRAFT_04                                                            | DRAFT_06                                                           | DRAFT_07                                                               | DRAFT_2019_09                                                        | DRAFT_2020_12                                                          |
|-----------------|-------------------------------------------------------------------------|-------------------------------------------------------------------|---------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------------------------------|----------------------------------------------------------------------|------------------------------------------------------------------------|
| NetworkNt       | pass: r:4703 (100.0%) o:2369 (100.0%)<br>fail: r:0 (0.0%) o:1 (0.0%)    |                                                                   | pass: r:600 (100.0%) o:251 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)  | pass: r:796 (100.0%) o:318 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) | pass: r:880 (100.0%) o:541 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)     | pass: r:1201 (100.0%) o:625 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)  | pass: r:1226 (100.0%) o:634 (99.8%)<br>fail: r:0 (0.0%) o:1 (0.2%)     |

* Note that this uses the ECMA 262 Validator option turned on for the `pattern` tests.

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
<!-- This is required when setting setEcma262Validator(true)  -->
<dependency>
    <!-- Used to validate ECMA 262 regular expressions -->
    <groupId>org.jruby.joni</groupId>
    <artifactId>joni</artifactId>
    <version>${version.joni}</version>
    <optional>true</optional>
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
    <version>1.4.0</version>
</dependency>
```

#### Gradle:

```java
dependencies {
    implementation(group: 'com.networknt', name: 'json-schema-validator', version: '1.4.0');
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

SchemaValidatorsConfig config = new SchemaValidatorsConfig();
// By default JSON Path is used for reporting the instance location and evaluation path
config.setPathType(PathType.JSON_POINTER);
// By default the JDK regular expression implementation which is not ECMA 262 compliant is used
// Note that setting this to true requires including the optional joni dependency
// config.setEcma262Validator(true);

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

SchemaValidatorsConfig config = new SchemaValidatorsConfig();
// By default JSON Path is used for reporting the instance location and evaluation path
config.setPathType(PathType.JSON_POINTER);
// By default the JDK regular expression implementation which is not ECMA 262 compliant is used
// Note that setting this to true requires including the optional joni dependency
// config.setEcma262Validator(true);

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
    executionContext.getConfig().setFormatAssertionsEnabled(true);
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
| Schema Node       | The `JsonNode` pointed to by the Schema Location.
| Instance Node     | The `JsonNode` pointed to by the Instance Location.
| Details           | Additional details that can be set by custom keyword validator implementations. This is not used by the library.

Annotations contains the following additional information

| Type              | Description 
|-------------------|-------------------
| Value             | The annotation value generated


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
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.setPathType(PathType.JSON_POINTER);
config.setFormatAssertionsEnabled(true);
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

### Schema Validators Configuration

| Name                                  | Description                                                                                                                                                                                                                       | Default Value
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------
| `pathType`                            | The path type to use for reporting the instance location and evaluation path. Set to `PathType.JSON_POINTER` to use JSON Pointer.                                                                                                 | `PathType.DEFAULT`
| `ecma262Validator`                    | Whether to use the ECMA 262 `joni` library to validate the `pattern` keyword. This requires the dependency to be manually added to the project or a `ClassNotFoundException` will be thrown.                                      | `false`
| `executionContextCustomizer`          | This can be used to customize the `ExecutionContext` generated by the `JsonSchema` for each validation run.                                                                                                                       | `null`
| `schemaIdValidator`                   | This is used to customize how the `$id` values are validated. Note that the default implementation allows non-empty fragments where no base IRI is specified and also allows non-absolute IRI `$id` values in the root schema.    | `JsonSchemaIdValidator.DEFAULT`
| `messageSource`                       | This is used to retrieve the locale specific messages.                                                                                                                                                                            | `DefaultMessageSource.getInstance()`
| `preloadJsonSchema`                   | Whether the schema will be preloaded before processing any input. This will use memory but the execution of the validation will be faster.                                                                                        | `true`
| `preloadJsonSchemaRefMaxNestingDepth` | The max depth of the evaluation path to preload when preloading refs.                                                                                                                                                             | `40`
| `cacheRefs`                           | Whether the schemas loaded from refs will be cached and reused for subsequent runs. Setting this to `false` will affect performance but may be neccessary to prevent high memory usage for the cache if multiple nested applicators like `anyOf`, `oneOf` and `allOf` are used.  | `true`
| `locale`                              | The locale to use for generating messages in the `ValidationMessage`.                                                                                                                                                             | `Locale.getDefault()`
| `failFast`                            | Whether to return failure immediately when an assertion is generated.                                                                                                                                                             | `false`
| `formatAssertionsEnabled`             | The default is to generate format assertions from Draft 4 to Draft 7 and to only generate annotations from Draft 2019-09. Setting to `true` or `false` will override the default behavior.                                        | `null`

## Performance Considerations

When the library creates a schema from the schema factory, it creates a distinct validator instance for each location on the evaluation path. This means if there are different `$ref` that reference the same schema location, different validator instances are created for each evaluation path.

When the schema is created, the library will automatically preload all the validators needed and resolve references. At this point, no exceptions will be thrown if a reference cannot be resolved. If there are references that are cyclic, only the first cycle will be preloaded. If you wish to ensure that remote references can all be resolved, the `initializeValidators` method needs to be called on the `JsonSchema` which will throw an exception if there are references that cannot be resolved.

The `JsonSchema` created from the factory should be cached and reused. Not reusing the `JsonSchema` means that the schema data needs to be repeated parsed with validator instances created and references resolved.

Collecting annotations will adversely affect validation performance.

The earlier draft specifications contain less keywords that can potentially impact performance. For instance the use of the `unevaluatedProperties` or `unevaluatedItems` keyword will trigger annotation collection in the related validators, such as the `properties` or `items` validators.

This does not mean that using a schema with a later draft specification will automatically cause a performance impact. For instance, the `properties` validator will perform checks to determine if annotations need to be collected, and checks if the meta-schema contains the `unevaluatedProperties` keyword and whether the `unevaluatedProperties` keyword exists adjacent the evaluation path.


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

## [ECMA-262 Regex](doc/ecma-262.md)

## [Custom Message](doc/cust-msg.md)

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



