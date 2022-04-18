# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added

### Changed

## 1.0.69 - 2022-04-18

### Added

- fixes #534 Adding Unevaluated properties keyword. Thanks @prashanthjos

### Changed

- fixes #554 removed unnecessary check. Thanks @harishvashistha
- fixes #555 Setting default value even if that value is null. Thanks @harishvashistha
- fixes #544 Fixing unevaluated properties with larger test base. Thanks @prashanthjos
- fixes #552 Add schemaPath to ValidationMessage. Thanks @ymszzq
- fixes #541 Allow fetching properties from map with comparator. Thanks @0x4a616e


## 1.0.68 - 2022-03-27

### Added

- fixes #534 Adding Unevaluated properties keyword. Thanks @prashanthjos

### Changed

- fixes #537 Fix oneOf bug. Thanks @RenegadeWizard and @sychlak
- fixes #511 Improve validation messages (German and default) Thanks @AndreasALoew
- fixes #539 Refactoring-code. Thanks @Sahil3198
- fixes #532 Invalid (non-string) $schema produces NullPointerException. Thanks @christi-square
- fixes #530 Fixed a typo in the validators documentation. Thanks @jontrost
- fixes #529 Updates to German translation. Thanks @rustermi

## 1.0.67 - 2022-03-05

### Changed

- fixes #525 Leap seconds are handled even better Thanks @aznan2 and @Matti Hansson
- fixes #524 Fix handling of leap seconds in date-time validation
- fixes #523 synched ipv4 and ipv6 and fix some gaps for the IP format
- fixes #522 synch the official test suite for draft v4 from schema.org
- fixes #509 NPE with oneOf and custom URI Fetcher or Factory
- fixes #508 Make date-time validation align with RFC3339 Thanks @aznan2 and @Matti Hansson
- fixes #519 Preserve # suffix during metaschema URI normalization Thanks @pondzix
- fixes #516 fix the additionalProperties in oneOf failed test cases
- fixes #505 AdditionalPropertiesOneOfFails test Thanks @huubfleuren
- fixes #510 try to reproduce the issue but failed
- fixes #511 Add German validation messages. Thanks @rustermi
- fixes #500 Support fragment references using $anchor @Whathecode

## 1.0.66 - 2022-01-24

### Changed

- fixes #496 Improve type validation of integrals. Thanks @christi-square
- fixes #497 Support fragment references using $anchor  @carolkao

## 1.0.65 - 2022-01-07

### Changed

- fixes #492 Sort ValidationMessage by its type. Thanks @jsu216
- fixes #490 Handle the situation when context class loader is null. Thanks @vti and @Viacheslav Tykhanovskyi
- fixes #489 Fix flakiness in CollectorContextTest. Thanks @pthariensflame
- upgrade to logback 1.2.7 to resolve some x-ray warnnings
- upgrade to undertow 2.2.14 to resolve some x-ray warnnings.
- fixes #488 Fix violations of Sonar rule 2142. Thanks @khaes-kth
- fixes #477 apply default in objects and arrays. Thanks @SiemelNaran
- fixes #485 FailFast should not cause exception on if. Thanks @gareth-robinson
- fixes #483 Add Java Syntax Highlighting to specversion.md. Thanks @JLLeitschuh
- fixes #482 upgrade to joni 2.1.41 to resolve a security concern

## 1.0.64 - 2021-11-10

### Changed

- fixes #480 Time format validation supports milliseconds. Thanks @@MatusSivak
- fixes #479 Add dependentRequired and dependentSchemas validators. Thanks @@kmalski

## 1.0.63 - 2021-10-21

### Changed

- fixes #470 OneOfValidator give incorrect message when the wrong json element is not the first one in the list. Thanks @jsu216
- fixes #472 fix i18n doesn't work with locale CHINA. Thanks @wyzfzu


## 1.0.62 - 2021-10-16

### Changed

- fixes #456 OneOf only validate the first sub schema. This was a defect introduced in 1.0.58 and everyone should upgrade to 1.0.62 if you are using 1.0.58 to 1.0.61. 

## 1.0.61 - 2021-10-09

### Changed

- fixes #461 1.0.60 Expects type To Be Array. Thanks @bartoszm
- fixes #459 Correcting the ref listeners config in WalkEvent class when fetching the getRefSchema. Thanks @prashanthjos

## 1.0.60 - 2021-09-22

### Changed

- fixes #451 walk method for AnyOfValidator not implemented. Thanks @bartoszm
- fixes #450 changed from isIntegralNumber to canConvertToExactIntegral to support. Thanks @mohsin-sq
- fixes #449 Refactor JSON Schema Test Suite tests. Thanks @olegshtch
- fixes #448 Test CI with JDK 11. Thanks @olegshtch
- fixes #447 Bump JUnit version to 5.7.2. Thanks @olegshtch

## 1.0.59 - 2021-09-11

### Changed

- fixes #445 JsonValidator: mark preloadJsonSchema as default. Thanks @DaNizz97
- fixes #443 $ref caching issue. Thanks @prashanthjos
- fixes #426 Adding custom ValidatorTypeCodes. Thanks @adilath18

## 1.0.58 - 2021-08-23

### Added
- 
- fixes #439 add i18n support for ValidationMessage. Thanks @leaves615
- fixes #438 Adding custom message support in the schema. Thanks @adilath18

### Changed

- fixes #436 Relaxation of the discriminator validation. Thanks FWiesner
- fixes #435 Added exampleSetFlag to nonValidationKeyword. Thanks @ShubhamRwt
- fixes #428 A schema with nullable oneOf does not work as expect. Thanks @rongyj
- fixes #429 Update collector-context.md. Thanks @Petapath
- fixes #425 Cannot distinguish the "TextNode" and the "ArrayNode" with single value for oneOf. Thanks @rongyj

## 1.0.57 - 2021-07-09

### Added

### Changed

- fixes #423 make sure additionalPropertiesSchema is not null in AdditionalPropertiesValidator. Thanks @flozano
- fixes #421 Wrong validation of MultipleOfValidator. Thanks @ubergrohman
- fixes #418 201909 false flag keywords additonalItems and then. Thanks @pgalbraith


## 1.0.56 - 2021-07-02

### Added

### Changed

- fixes #416 Circular $ref occurrences with schema.initializeValidators() lead to StackOverflowError. Thanks @FWiesner
- fixes #414 Simplify the uri format validation regexp. Thanks @vmaurin

## 1.0.55 - 2021-06-23

### Added

### Changed

- fixes #411 uri format regexp is fixed to support empty fragment and query string. Thanks @vmaurin

## 1.0.54 - 2021-06-22

### Added

### Changed

- fixes #408 uri format regexp is validating invalid URI. Thanks @vmaurin
- fixes #406 Behavior change of $ref resolution. Thanks @FWiesner

## 1.0.53 - 2021-05-19

### Added

### Changed

- fixes #400 Introduce forceHttps flag in JsonSchemaFactory.Builder. Thanks @hisener

## 1.0.52 - 2021-04-13

### Added

### Changed

- fixes #398 Two issues with OpenAPI 3 discriminators. Thanks @FWiesner
- fixes #396 Implement propertyNames in terms full schema validation. Thanks @JonasProgrammer

## 1.0.51 - 2021-03-30

### Added

### Changed

- fixes #392 NPE due to concurrency bug. Thanks @Keymaster65
- fixes #391 override default EmailValidator, if set custom email format. Thanks @whirosan
- fixes #390 Add discriminator support. Thanks @FWiesner

## 1.0.50 - 2021-03-18

### Added

### Changed

- fixes #387 Resolve the test case errors for TypeFactoryTest
- fixes #385 Fixing concurrency and compilation issues. Thanks @prashanthjos
- fixes #383 Nested oneOf gives incorrect validation error. Thanks @JonasProgrammer
- fixes #379 Add lossless narrowing convertion. Thanks @hkupty 
- fixes #378 Upgrade Jackson to 2.12.1 and Undertow to 2.2.4.Final

## 1.0.49 - 2021-02-17

### Added

### Changed

- fixes #375 PropertyNames to return validator value on error. Thanks @Eivyses
- fixes #335 Fixed parallel processing. @Thanks @mweber03

## 1.0.48 - 2021-02-04

### Added

### Changed

- fixes #326 pattern validation for propertyNames. @Thanks @LeifRilbeATG
- fixes #366 Fast fail issue with One Of Validator. Thanks @Krishna-capone

## 1.0.47 - 2021-01-16

### Added

### Changed

- fixes #368 Fixing Walk Listeners Issues. @Thanks prashanthjos
- fixes #363 Date-time validation fails depending on local time zone. Thanks @ennoruijters

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
