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


This is a Java implementation of the [JSON Schema Core Draft v4, v6, v7, v2019-09 and v2020-12(partial)](http://json-schema.org/latest/json-schema-core.html) specification for JSON schema validation. In addition, it also works for OpenAPI 3.0 request/response validation with some [configuration flags](doc/config.md). For users who want to collect information from a JSON node based on the schema, the [walkers](doc/walkers.md) can help. The default JSON parser is the [Jackson](https://github.com/FasterXML/jackson) that is the most popular one. As it is a key component in our [light-4j](https://github.com/networknt/light-4j) microservices framework to validate request/response against OpenAPI specification for [light-rest-4j](http://www.networknt.com/style/light-rest-4j/) and RPC schema for [light-hybrid-4j](http://www.networknt.com/style/light-hybrid-4j/) at runtime, performance is the most important aspect in the design. 

## Why this library

#### Performance

It is the fastest Java JSON Schema Validator as far as I know. Here is the testing result compare with the other two open-source implementations. It is about 32 times faster than the Fge and five times faster than the Everit.

- fge: 7130ms
- everit-org: 1168ms
- networknt: 223ms

You can run the performance tests for three libraries from [https://github.com/networknt/json-schema-validator-perftest](https://github.com/networknt/json-schema-validator-perftest)

#### Parser

It uses Jackson that is the most popular JSON parser in Java. If you are using Jackson parser already in your project, it is natural to choose this library over others for schema validation. 

#### YAML Support

The library works with JSON and YAML on both schema definitions and input data. 

#### OpenAPI Support

The OpenAPI 3.0 specification is using JSON schema to validate the request/response, but there are some differences. With a configuration file, you can enable the library to work with OpenAPI 3.0 validation. 

#### Dependency

Following the design principle of the Light Platform, this library has minimum dependencies to ensure there are no dependency conflicts when using it. 

Here are the dependencies:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${version.jackson}</version>
</dependency>

<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>${version.slf4j}</version>
</dependency>
```

**Note**: Up to version [1.0.81](https://github.com/networknt/json-schema-validator/blob/1.0.81/pom.xml#L99), the dependency `org.apache.commons:commons-lang3` was included as a runtime dependency. Starting with [1.0.82](https://github.com/networknt/json-schema-validator/releases/tag/1.0.82) it is not required anymore.

#### Community

This library is very active with a lot of contributors. New features and bug fixes are handled quickly by the team members. Because it is an essential dependency of the [light-4j](https://github.com/networknt/light-4j) framework in the same GitHub organization, it will be evolved and maintained along with the framework. 

## Prerequisite

The library supports Java 8 and up. If you want to build from the source code, you need to install JDK 8 locally. To support multiple version of JDK, you can use [SDKMAN](https://www.networknt.com/tool/sdk/)

## Dependency

This package is available on Maven central. 

Maven: 

```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.0.87</version>

    <!-- Only required for versions < 1.0.82. See README.md -->
    <exclusions>
        <exclusion>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

Gradle:

```java
dependencies {
    implementation(group: 'com.networknt', name: 'json-schema-validator', version: '1.0.87');
}
```

For the latest version, please check the [release](https://github.com/networknt/json-schema-validator/releases) page. 

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


## Known issues

I have just updated the test suites from the [official website](https://github.com/json-schema-org/JSON-Schema-Test-Suite) as the old ones were copied from another Java validator. Now there are several issues that need to be addressed. All of them are edge cases, in my opinion, but need to be investigated. As my old test suites were inherited from another Java JSON Schema Validator, I guess other Java Validator would have the same issues as these issues are in the Java language itself.

[#7](https://github.com/networknt/json-schema-validator/issues/7)

[#5](https://github.com/networknt/json-schema-validator/issues/5)

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



