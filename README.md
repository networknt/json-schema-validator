[Stack Overflow](https://stackoverflow.com/questions/tagged/light-4j) |
[Google Group](https://groups.google.com/forum/#!forum/light-4j) |
[Gitter Chat](https://gitter.im/networknt/light-rest-4j) |
[Subreddit](https://www.reddit.com/r/lightapi/) |
[Youtube](https://www.youtube.com/channel/UCHCRMWJVXw8iB7zKxF55Byw) |
[Documentation](https://doc.networknt.com/library/json-schema-validator/) |
[Javadocs](https://www.javadoc.io/doc/com.networknt/json-schema-validator) |
[Contribution Guide](https://doc.networknt.com/contribute/) |

[![Build Status](https://travis-ci.org/networknt/json-schema-validator.svg?branch=master)](https://travis-ci.org/networknt/json-schema-validator) [![codecov.io](https://codecov.io/github/networknt/json-schema-validator/coverage.svg?branch=master)](https://codecov.io/github/networknt/json-schema-validator?branch=master)


This is a Java implementation of the [JSON Schema Core Draft v4, v6, v7 and v2019-09](http://json-schema.org/latest/json-schema-core.html) specification for JSON schema validation. In addition, it also works for OpenAPI 3.0 request/response validation with some [configuration flags](doc/config.md). The default JSON parser is the [Jackson](https://github.com/FasterXML/jackson) that is the most popular one. As it is a key component in our [light-4j](https://github.com/networknt/light-4j) microservices framework to validate request/response against OpenAPI specification for [light-rest-4j](http://www.networknt.com/style/light-rest-4j/) and RPC schema for [light-hybrid-4j](http://www.networknt.com/style/light-hybrid-4j/) at runtime, performance is the most important aspect in the design. 

## Why this library

#### Performance

It is the fastest Java JSON Schema Validator as far as I know. Here is the testing result compare with the other two open-source implementations. It is about 32 times faster than the Fge and five times faster than the Everit.

fge: 7130ms

everit-org: 1168ms

networknt: 223ms

You can run the performance tests for three libraries from [https://github.com/networknt/json-schema-validator-perftest](https://github.com/networknt/json-schema-validator-perftest)

#### Parser

It uses Jackson that is the most popular JSON parser in Java. If you are using Jackson parser already in your project, it is natural to choose this library over others for schema validation. 

#### YAML Support

The library works with JSON and YAML on both schema definitions and input data. 

#### OpenAPI Support

The OpenAPI 3.0 specification is using JSON schema to validate the request/response, but there are some differences. With a configuration file, you can enable the library to work with OpenAPI 3.0 validation. 

#### Dependency

Following the design principle of the Light Platform, this library has minimum dependencies to ensure there are no dependency conflicts when using it. 

Here are the dependencies. 

```
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

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>${version.common-lang3}</version>
</dependency>
```

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
    <version>1.0.43</version>
</dependency>
```

Gradle:

```
dependencies {
    compile(group: "com.networknt", name: "json-schema-validator", version: "1.0.43");
}
```

For the latest version, please check the [release](https://github.com/networknt/json-schema-validator/releases) page. 

## [Quick Start](doc/quickstart.md)

## [Validators](doc/validators.md)

## [Configuration](doc/config.md)

## [Specification Version](doc/specversion.md)

## [YAML Validation](doc/yaml.md)

## [Schema Mapping](doc/schema-map.md)

## [Customized URIFetcher](doc/cust-fetcher.md)

## [Customized MetaSchema](doc/cust-meta.md)

## [Collector Context](doc/collector-context.md)

## [JSON Schema Walkers and WalkListeners](doc/walkers.md)

## [ECMA-262 Regex](doc/ecma-262.md)

## Known issues

I have just updated the test suites from the [official website](https://github.com/json-schema-org/JSON-Schema-Test-Suite) as the old ones were copied from another Java validator. Now there are several issues that need to be addressed. All of them are edge cases, in my opinion, but need to be investigated. As my old test suites were inherited from another Java JSON Schema Validator, I guess other Java Validator would have the same issues as these issues are in the Java language itself.

[#7](https://github.com/networknt/json-schema-validator/issues/7)

[#5](https://github.com/networknt/json-schema-validator/issues/5)

## Contributors

Thanks to the following people who have contributed to this project. If you are using this library, please consider to be a sponsor for one of the contributors. 

[@stevehu](https://github.com/sponsors/stevehu)

[@jiachen1120](https://github.com/jiachen1120)

[@BalloonWen](https://github.com/BalloonWen)

[@eskabetxe](https://github.com/eskabetxe)

[@ddobrin](https://github.com/ddobrin)

[@ehrmann](https://github.com/ehrmann)

[@rhwood](https://github.com/rhwood)

[@nitin1891](https://github.com/nitin1891)

[@jawaff](https://github.com/jawaff)

[@kosty](https://github.com/kosty)

[@chenyan71](https://github.com/chenyan71)

[@chrisken](https://github.com/chrisken)

[@NicholasAzar](https://github.com/NicholasAzar)

[@basinilya](https://github.com/basinilya)

For all contributors, please visit https://github.com/networknt/json-schema-validator/graphs/contributors

If you are a contributor, please join the [GitHub Sponsors](https://github.com/sponsors) and switch the link to your sponsors dashboard via a PR.

## Sponsors


### Individual Sponsors


### Corporation Sponsors



