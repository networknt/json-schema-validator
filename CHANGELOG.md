# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added

### Changed

## 1.0.46 - 2020-12-30

### Added

### Changed

- fixes #362 Date-time validation fails depending on local time zone Thanks @ennoruijters
- fixes #361 Validation of oneOf depends on schema order @Thanks ennoruijters
- fixes #360 add four project links to the README.md
- fixes #354 OneOf validator is not throwing valid error if any of the child nodes has invalid schemas Thanks @prubdeploy
- fixes #351 Add anchor and deprecated as NonValidationKeywords for v2019-09 draft Thanks @anicolasgar
- fixes #340 YAML source location handling Thanks @ascertrobw

## 1.0.45 - 2020-11-21

### Added

### Changed

- fixes #350 Add builder method that accepts iterable Thanks @wheelerlaw
- fixes #347 NPE at JsonSchema.combineCurrentUriWithIds(JsonSchema.java:90) Thanks @wheelerlaw
- fixes #346 Update docs about javaSemantics flag Thanks @oguzhanunlu
- fixes #345 optimize imports in the src folder
- fixes #343 Improve type validation of numeric values Thanks @oguzhanunlu
- fixes #341 Add contentMediaType, contentEncoding and examples as a NonValidationKeyword Thanks @jonnybbb
- fixes #337 JSON Schema Walk Changes Thanks @prashanthjos

## 1.0.44 - 2020-10-20

### Added

### Changed
- fixes #336 Adding walk capabilities to networknt. Thanks @prashanthjos
- fixes #332 Bump junit from 4.12 to 4.13.1
- fixes #329 JRuby Joni dependency and its dependencies
- fixes #328 Add $comment as a NonValidationKeyword for v7 and v2019 drafts. Thanks @kmalski
- fixes #324 Generate module-info, fix build on JDK11 Thanks @handcraftedbits
- fixes #323 FIX: potential duplicate log entry due to race condition Thanks @kkonrad
- fixes #319 resolve a java doc warning in CollectorContext

## 1.0.43 - 2020-08-10

### Added

### Changed

- fixes #317 Compatible with Jackson 2.9.x. Thanks @pan3793
- fixes #315 implement propertyNames validator for v6, v7 and v2019-09

## 1.0.42 - 2020-06-30

### Added

### Changed

- fixes #311 Split the PatternValidator into 2 classes. Thanks @Buuhuu

## 1.0.41 - 2020-06-25

### Added

### Changed

- fixes #307 Make runtime dependency to org.jruby.joni:joni optional. Thanks @Buuhuu
- fixes #305 Automatically determine schema version from schema file. Thanks @Subhajitdas298
- fixes #297 ValidationContext using is not correct in UUIDValidator. Thanks @qiunju


## 1.0.40 - 2020-05-27

### Added

### Changed

- fixes #294 fixes unknownMetaSchema error with normalized URI

## 1.0.39 - 2020-04-28

### Added

### Changed

- fixes #289 Adding getAll method on CollectorContext class. Thanks @prashanthjos

## 1.0.38 - 2020-04-12

### Added

### Changed

- fixes #281 EmailValidator use ValidatorTypeCode Datetime

## 1.0.37 - 2020-04-06

### Added

### Changed

- fixes #280 NullPointerException in regex pattern validation if no SchemaValidatorsConfig is passed. Thanks @waizuwolf

## 1.0.36 - 2020-03-22

### Added

### Changed

- fixes #273 make the getInstance() deprecated
- fixes #258 Cyclic dependencies result in StackOverflowError. Thanks @francesc79

## 1.0.35 - 2020-03-13

### Added

### Changed

- fixes #272 Use ECMA-262 validator when requested. Thanks @eirnym

## 1.0.34 - 2020-03-12

### Added

### Changed

- fixes #268 Collector Context changes to handle simple Objects. Thanks @prashanthjos
- fixes #266 reformat the code and resolve javadoc warnnings

## 1.0.33 - 2020-03-09

### Added

### Changed

- fixes #264 Handling JSONPointer (URI fragment identifier) with no base uri. Thanks @rzukowski
- fixes #255 Dereferencing subschemas by $id with $ref in the same file does not seem to work. Thanks @rzukowski

## 1.0.32 - 2020-03-07

### Added

### Changed

- fixes #260 Changes for adding collector context. Thanks @prashanthjos

## 1.0.31 - 2020-02-21

### Added

### Changed

- fixes #226 Implements contains. Thanks @Asamsig

## 1.0.30 - 2020-02-11

### Added

### Changed

- fixes #244 Android 6 support. Thanks @msattel
- fixes #247 Resolve schema id from the schema document (for v6 and above). Thanks @martin-sladecek
- fixes #243 Improve accuracy of rounding with multipleOf. Thanks @seamusv
- fixes #242 add customized fetcher and meta schema doc

## 1.0.29 - 2019-12-16

### Added

### Changed

- Update description in pom.xml to match readme.md. Thanks @reftel 
- fixes #232 update meta schema URI to https
- fixes #229 move the remotes to resource from draftv4
- fixes #228 support boolean schema in the dependencies validator
- enable const validator test for v6
- fixes #224 support boolean schema for the item validator
- fixes #222 add document for URL mapping

## 1.0.28 - 2019-11-25

### Added

### Changed

- fixes #219 Fix for oneOf when not all properties are matched. Thanks @aznan2

## 1.0.27 - 2019-11-18

### Added

### Changed

- fixes #216 Fix remote ref to follow redirects. Thanks @andersonf
- fixes #214 the if-then-else.json is failed in test for V7 and V2019-09. Thanks @andersonf
- fixes #54 support for draft V6, V7 and V2019-09
- fixes #211 move the current test cases from tests to draft4 folder in the resource

## 1.0.26 - 2019-11-07

### Added

### Changed

- fixes #208 error when same ref name in different ref files. Thanks @andersonf

## 1.0.25 - 2019-11-06

### Added

### Changed

- fixes #206 IF-THEN-ELSE Conditional (Draft 7). Thanks @andersonf

## 1.0.24 - 2019-10-28

### Added

### Changed

- fixes #203 String for Number should fail with the default SchemaValidatorsConfig

## 1.0.23 - 2019-10-28

### Added

### Changed

- fixes #199 More than a million validation errors crashes the application. Thanks @khiftikhar


## 1.0.22 - 2019-10-22

### Added

### Changed

- fixes #200 Use with obfuscation.Thanks @complex1ty

## 1.0.21 - 2019-10-17

### Added

### Changed

- fixes #192 upgrade jackson to 2.9.10
- fixes #190 OneOfValidator cannot validate object with multiple properties.Thanks @ddobrin
- fixes #188 couldnot validate the email format in json schema
- fixes #187 SchemaValidatorsConfig not propagated

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
