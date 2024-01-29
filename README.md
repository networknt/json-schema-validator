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


This is a Java implementation of the [JSON Schema Core Draft v4, v6, v7, v2019-09 and v2020-12](http://json-schema.org/latest/json-schema-core.html) specification for JSON schema validation.

In addition, it also works for OpenAPI 3.0 request/response validation with some [configuration flags](doc/config.md). For users who want to collect information from a JSON node based on the schema, the [walkers](doc/walkers.md) can help. The JSON parser used is the [Jackson](https://github.com/FasterXML/jackson) parser. As it is a key component in our [light-4j](https://github.com/networknt/light-4j) microservices framework to validate request/response against OpenAPI specification for [light-rest-4j](http://www.networknt.com/style/light-rest-4j/) and RPC schema for [light-hybrid-4j](http://www.networknt.com/style/light-hybrid-4j/) at runtime, performance is the most important aspect in the design.

## JSON Schema Draft Specification compatibility

Information on the compatibility support for each version, including known issues, can be found in the [Compatibility with JSON Schema versions](doc/compatibility.md) document.

## Upgrading to new versions

This library can contain breaking changes in minor version releases.

Information on notable or breaking changes when upgrading the library can be found in the [Upgrading to new versions](doc/upgrading.md) document.

Information on the latest version can be found on the [Releases](https://github.com/networknt/json-schema-validator/releases) page.

## Comparing against other implementations

The [JSON Schema Validation Comparison
](https://github.com/creek-service/json-schema-validation-comparison) project from Creek has an informative [Comparison of JVM based Schema Validation Implementations](https://www.creekservice.org/json-schema-validation-comparison/) which compares both the functional and performance characteristics of a number of different Java implementations. 
* [Functional comparison](https://www.creekservice.org/json-schema-validation-comparison/functional)
* [Performance comparison](https://www.creekservice.org/json-schema-validation-comparison/performance)

The [Bowtie](https://github.com/bowtie-json-schema/bowtie) project has a [report](https://bowtie.report/) that compares functional characteristics of different implementations, including non-Java implementations, but does not do any performance benchmarking.

## Why this library

#### Performance

This should be the fastest Java JSON Schema Validator implementation.

The following is the benchmark results from [JSON Schema Validator Perftest](https://github.com/networknt/json-schema-validator-perftest) project that uses the [Java Microbenchmark Harness](https://github.com/openjdk/jmh).

Note that the benchmark results are highly dependent on the input data workloads used for the validation.

In this case this workload is using the Draft 4 specification and largely tests the performance of the evaluating the `properties` keyword. You may refer to [Results of performance comparison of JVM based JSON Schema Validation Implementations](https://www.creekservice.org/json-schema-validation-comparison/performance) for benchmark results for more typical workloads

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
    <version>1.3.1</version>
</dependency>
```

#### Gradle:

```java
dependencies {
    implementation(group: 'com.networknt', name: 'json-schema-validator', version: '1.3.1');
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
// This creates a schema factory that will use Draft 2012-12 as the default if $schema is not specified in the schema data. If $schema is specified in the schema data then that schema dialect will be used instead and this version is ignored.
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

// Due to the mapping the schema will be retrieved from the classpath at classpath:schema/example-main.json. If the schema data does not specify an $id the absolute IRI of the schema location will be used as the $id.
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
    executionContext.getConfig().setFormatAssertionsEnabled(true);
});
```

### Validating a schema against a meta schema

The following example demonstrates how a schema is validated against a meta schema.

This is actually the same as validating inputs against a schema except in this case the input is the schema and the schema used is the meta schema.

```java
JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> 
    // This creates a mapping to load the meta schema from the library classpath instead of remotely
    // This is better for performance and the remote may choose not to service the request
    // For instance Cloudflare will block requests that have older Java User-Agent strings eg. Java/1.
    builder.schemaMappers(schemaMappers -> 
        schemaMappers.mapPrefix("https://json-schema.org", "classpath:").mapPrefix("http://json-schema.org", "classpath:"))
);

SchemaValidatorsConfig config = new SchemaValidatorsConfig();
// By default JSON Path is used for reporting the instance location and evaluation path
config.setPathType(PathType.JSON_POINTER);
// By default the JDK regular expression implementation which is not ECMA 262 compliant is used
// Note that setting this to true requires including the optional joni dependency
// config.setEcma262Validator(true);

// Due to the mapping the meta schema will be retrieved from the classpath at classpath:draft/2020-12/schema.
JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V202012), config);
String input = "{  \n"
    + "  \"type\": \"object\",  \n"
    + "  \"properties\": {    \n"
    + "    \"key\": { \n"
    + "      \"title\" : \"My key\", \n"
    + "      \"type\": \"invalidtype\" \n"
    + "    } \n"
    + "  }\n"
    + "}";
Set<ValidationMessage> assertions = schema.validate(input, InputFormat.JSON, executionContext -> {
    // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
    executionContext.getConfig().setFormatAssertionsEnabled(true);
});
```        

## Performance Considerations

When the library creates a schema from the schema factory, it creates a distinct validator instance for each location on the evaluation path. This means if there are different `$ref` that reference the same schema location, different validator instances are created for each evaluation path.

When the schema is created, the library will automatically preload all the validators needed and resolve references. At this point, no exceptions will be thrown if a reference cannot be resolved. If there are references that are cyclic, only the first cycle will be preloaded. If you wish to ensure that remote references can all be resolved, the `initializeValidators` method needs to be called on the `JsonSchema` which will throw an exception if there are references that cannot be resolved.

The `JsonSchema` created from the factory should be cached and reused. Not reusing the `JsonSchema` means that the schema data needs to be repeated parsed with validator instances created and references resolved.

Collecting annotations will adversely affect validation performance.

The earlier draft specifications contain less keywords that can potentially impact performance. For instance the use of the `unevaluatedProperties` or `unevaluatedItems` keyword will trigger annotation collection in the related validators, such as the `properties` or `items` validators.

This does not mean that using a schema with a later draft specification will automatically cause a performance impact. For instance, the `properties` validator will perform checks to determine if annotations need to be collected, and checks if the meta schema contains the `unevaluatedProperties` keyword and whether the `unevaluatedProperties` keyword exists adjacent the evaluation path.


## [Quick Start](doc/quickstart.md)

## [Validators](doc/validators.md)

## [Configuration](doc/config.md)

## [Specification Version](doc/specversion.md)

## [YAML Validation](doc/yaml.md)

## [Customizing Schema Retrieval](doc/schema-retrieval.md)

## [Customized MetaSchema](doc/cust-meta.md)

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



