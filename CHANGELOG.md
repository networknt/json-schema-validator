# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added

### Changed

## 1.0.20 - 2019-09-10

### Added

### Changed

- fixes #183 Validation error when field is nullable and consumer sends in a null value. Thanks @ddobrin
- fixes #185 Validation issue in oneOf when elements have optional fields. Thanks @ddobrin

## 1.0.19 - 2019-08-13

### Added

### Changed

- fixes #182 Jackson-databind vulnerability version update
- fixes #180 Stack overflow when using recursive references, $ref. Thanks @davidvisiedo
- fixes #96 stackOverflowError loading schema file. Thanks @davidvisiedo
- fixes #44 Validator hang on validation. Thanks @davidvisiedo
- fixes #28 Validator hangs on large json data files. Thanks @davidvisiedo
- fixes #13 Cannot get the validation result with self-reference schema. Thanks @davidvisiedo
- fixes #177 OneOf Validator Incorrectly Failing. Thanks @jawaff

## 1.0.18 - 2019-07-29

### Added

### Changed

- fixes #173 AnyOfValidator ignores all previous validations errors if any of the type does not match. Thanks @grssam

## 1.0.17 - 2019-07-20

### Added

### Changed

- fixes #174 Insights into performance gains of tuning min/max validators. Thanks @kosty
- fixes #171 Support minimum/maximum on quoted numerals. Thanks @kosty

## 1.0.16 - 2019-06-24

### Added

### Changed

- fixes #166 Allow using URN and not just URLs. Thanks @jawaff

## 1.0.15 - 2019-06-14

### Added

### Changed

- fixes #160 when schema type is integer but max/min value is a float point number. Thanks @BalloonWen

## 1.0.14 - 2019-06-06

### Added

### Changed

- fixes #163 update typeLoose to false as before merging the PR 141
- fixes #162 bump up java version to 1.8
- fixes #141 Improved Ref Validator. Thanks @jawaff

## 1.0.13 - 2019-06-05

### Added

### Changed

- fixes #158 date-time format should consider colon in timezone optional. Thanks @chuwy

## 1.0.12 - 2019-05-30

### Added

### Changed

- fixes #155 Fix date-time validation. Thanks @jiachen1120

## 1.0.11 - 2019-05-28

### Added

### Changed

- fixes #151 add validation for string type uuid. Thanks @chenyan71

## 1.0.10 - 2019-05-22

### Added

### Changed

- fixes #138 validation of date fields. Thanks @jiachen1120

## 1.0.9 - 2019-05-21

### Added

### Changed

- fixes #147 Fails to validate MIN and MAX when number type is converted to BigInteger. Thanks @jiachen1120

## 1.0.8 - 2019-05-17

### Added

### Changed

- fixes #145 Fix bug parsing array query params when only one item present. Thanks @jiachen1120
- fixes #142 validation for enum object type. Thanks @jiachen1120
- fixes #136 Maps of URLs can have performance impacts. Thanks @rhwood
- fixes #134 $ref external schema references do not use URL mappings. Thanks @rhwood

## 1.0.7 - 2019-04-29

### Added

### Changed

- fixes #140 Convert double to BigDecimal in MultipleOfValidator to make the validation more accurate. Thanks @jiachen1120

## 1.0.6 - 2019-04-10

### Added

### Changed

- fixes #132 minimum/maximum validation of integral numbers prone to overflow. Thanks @kosty
- fixes #123 Add link to Javadocs. Thanks @rhwood

## 1.0.5 - 2019-04-01

### Added

### Changed

- fixes #127 update license copyright and add NOTICE
- fixes #125 feat: Add URL mappings. Thanks @rhwood

## 1.0.4 - 2019-03-14

### Added

### Changed

- fixes #119 Almost JSON-spec compliant validation of numeric values. Thanks @kosty
- fixes #120 Update the version in the README.md file. Thanks @chenyan71

## 1.0.3 - 2019-02-10

### Added

### Changed

- fixes #116 Fail to validate numeric and Integer in TypeValidator. Thanks @jiachen1120

## 1.0.2 - 2019-02-05

### Added

### Changed

- fixes #114 LocalDateTime validation error. Thanks @chenyan71
- fixes #113 Fixed validation for path parameters and query parameters. Thanks @jiachen1120

## 1.0.1 - 2019-01-10

### Added

### Changed
- fixes #112 AnyOfValidator: only return expectedTypeList if not empty. Thanks @c14s
- fixes #111 Validation failure for optional field in a schema - in the PropertiesValidator. Thanks @ddobrin

## 0.1.26 - 2018-12-24

### Added

### Changed
- fixes #110 Validation Error when using OneOf in OpenAPI specs. Thanks @ddobrin

## 0.1.25 - 2018-12-12

### Added

### Changed
- fixes #108 v0.1.24 error on array union type. Thanks @nitin456
- fixes #107 Fix for perfomance issue Thanks @nitin456
- fixes #106 Fix for enable loose type validator for REST Thanks @BalloonWen

## 0.1.24 - 2018-11-21

### Added

### Changed
- fixes #105 temporary fix to performance issue. Thanks @nitin456

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
