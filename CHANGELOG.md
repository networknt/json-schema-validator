# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added

### Changed

## 0.1.23 - 2018-10-02

### Added

### Changed
- fixes #103 Boolean type validation for the string type is incorrect

## 0.1.22 - 2018-09-11

### Added

### Changed
- fixes #101 enhance TypeValidator trying to convert type from TEXT

## 0.1.21 - 2018-08-14

### Added

### Changed
- fixes #94 Fix min/max error message of integer fields displayed as doubles. Thanks @NicholasAzar
- fixes #93 Adding support for nullable fields. Thanks @NicholasAzar

## 0.1.20 - 2018-07-30

### Added

### Changed
- fixes #85 Update version in maven dependnecy sample. Thanks @banterCZ
- fixes #89 Added example for custom keywords in tests. Thanks @Klas Kalaß
- fixes #90 Remove unused dependency to slf4j-ext due to security issue. Thanks @Thorbias
- fixes #91 update one test case to ensure compatibility of Java 6
- fixes #92 rollback type validator for null value as it is against spec.

## 0.1.19 - 2018-04-07

### Added

### Changed
- fixes #84 remove Java 8 optional to ensure that this library can be Java 6 compatible. Thanks @johnygeorge
- fixes #81 java.lang.NoClassDefFoundError: Failed resolution of: Ljava/util/Optional. Thanks @johnygeorge
- fixes #83 upgrade to undertow 1.4.23.Final in sync with other repo

## 0.1.18 - 2018-04-04

### Added

### Changed
- Fixes #80 upgrade to jackson 2.9.5 and undertow 1.4.20.Final
- Fixes #77 One of was broken - it did not fail when there were no valid schemas. Thanks @kkalass
- Fixes #76 Make remaining JsonSchema constructors public. Thanks @kkalass

## 0.1.17 - 2018-03-09

### Added

### Changed
- Fixes #72 build JAR with OSGi support. Thanks @lichtin
- Fixes #71 Github Quickstart section out-of-date. Thanks @lichtin

## 0.1.16 - 2018-03-03

### Added

### Changed
- Fixes #62 Correct behavior when both allOf and type are present. Thanks @ehrmann
- Fixes #70 Minor optimizations. Thanks @ehrmann

## 0.1.15 - 2018-02-16

### Added

### Changed
- Fixes #65 enhance day validation regex for date format. Thanks @chenyan71


## 0.1.14 - 2018-02-14

### Added

### Changed
- Fixes #64 Add simple tests for ValidatorTypeCode. Thanks @ehrmann
- Fixes #61 Restore validator type code from value. Thanks @ehrmann

## 0.1.13 - 2017-12-10

### Added

### Changed
- Fixes #53 Optimization for OneOf. Thanks @kkalass
- Fixes #52 References that cannot be resolved should be treated as an error. Thanks @kkalass
- Fixes #51 Resolve sub schema node only if really needed. Thanks @kkalass

## 0.1.12 - 2017-11-23

### Added

### Changed
- Fixes #50 Support custom meta schemas with custom keywords and formats. Thanks @kkalass
- Fixes #49 Use LinkedHashSets for ValidationMessages. Thanks @ehrmann 
- Fixes #48 Remove unnecessary todo. Thanks @ehrmann
- Fixes #47 Change access modifiers in ValidationMessage. Thanks @ehrmann
- Fixes #45 Added test case for loading schemas from classpath. Thanks @kenwa


## 0.1.11 - 2017-10-18
### Added
- Fixes #43 Load reference schemas from classpath is supported. Thanks @kenwa 

### Changed

## 0.1.10 - 2017-07-22
### Added

### Changed
- Release the library in Java 6 as there are still developer using it. Thanks @basinilya

## 0.1.9 - 2017-07-03
### Added

### Changed
- Fixes #37 adding relative $ref url. Thanks @eskabetxe

## 0.1.8 - 2017-06-17
### Added

### Changed
- Recursive load fix #36 Thanks @thekensta

## 0.1.7 - 2017-04-26
### Added

### Changed
- Fixes #25 Enable Undertow server to test remote schemas
- Add test with id schema as url Thanks @eskabetxe
- If schema not valid to oneOf, added all errors. Thanks @eskabetxe

## 0.1.6 - 2017-04-03
### Added

### Changed
- Fixes #20 added default messages to empty messages on ValidatorTypeCode. Thanks @eskabetxe
- Fixes #22 only check subschema if distinct from schema, and minor changes. Thanks @eskabetxe
- Fixes #24 update dependencies versions. Thanks @eskabetxe

## 0.1.5 - 2017-03-25
### Added

### Changed
- Fixes #19 make undertow test scope

## 0.1.4 - 2017-02-06
### Added

### Changed
- Fixes #6 Match subsequence instead of entire input sequence. Thanks @mspiegel

## 0.1.3 - 2016-11-03
### Added

### Changed
- Sycn with official test suites and documented failed test cases.
- Fixes #4 MinLength and MaxLength validator for unicode string. Thanks for @dola to point me to the right direction.

## 0.1.2 - 2016-10-20
### Added

### Changed
- Broken escaping in pattern for uris [#1](https://github.com/networknt/json-schema-validator/issues/1)


## 0.1.1 - 2016-08-16
### Added
- First version
