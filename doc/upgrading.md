## Upgrading to new versions

This library can contain breaking changes in `minor` version releases.

This contains information on the notable or breaking changes in each version.

### 1.3.1

This does not contain any breaking changes from 1.3.0

* Annotation collection and reporting has been implemented
* Keywords have been refactored to use annotations for evaluation to improve performance and meet functional requirements
* The list and hierarchical output formats have been implemented as per the [Specification for Machine-Readable Output for JSON Schema Validation and Annotation](https://github.com/json-schema-org/json-schema-spec/blob/main/jsonschema-validation-output-machines.md).
* The fail fast evaluation processing has been redesigned and fixed. This currently passes the [JSON Schema Test Suite](https://github.com/json-schema-org/JSON-Schema-Test-Suite) with fail fast enabled. Previously contains and union type may cause incorrect results.
* This also contains fixes for regressions introduced in 1.3.0

The following keywords were refactored to improve performance and meet the functional requirements.

In particular this converts the `unevaluatedItems` and `unevaluatedProperties` validators to use annotations to perform the evaluation instead of the current mechanism which affects performance. This also refactors `$recursiveRef` to not rely on that same mechanism.

* `unevaluatedProperties`
* `unevaluatedItems`
* `properties`
* `patternProperties`
* `items` / `additionalItems`
* `prefixItems` / `items`
* `contains`
* `$recursiveRef`

This also fixes the issue where the `unevaluatedItems` keyword does not take into account the `contains` keyword when performing the evaluation.

This also fixes cases where `anyOf` short-circuits to not short-circuit the evaluation if a adjacent `unevaluatedProperties` or `unevaluatedItems` keyword exists.

This should fix most of the remaining functional and performance issues.

#### Functional

| Implementations | Overall                                                                 | DRAFT_03                                                          | DRAFT_04                                                            | DRAFT_06                                                           | DRAFT_07                                                               | DRAFT_2019_09                                                        | DRAFT_2020_12                                                          |
|-----------------|-------------------------------------------------------------------------|-------------------------------------------------------------------|---------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------------------------------|----------------------------------------------------------------------|------------------------------------------------------------------------|
| NetworkNt       | pass: r:4703 (100.0%) o:2369 (100.0%)<br>fail: r:0 (0.0%) o:1 (0.0%)    |                                                                   | pass: r:600 (100.0%) o:251 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)  | pass: r:796 (100.0%) o:318 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) | pass: r:880 (100.0%) o:541 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)     | pass: r:1201 (100.0%) o:625 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%)  | pass: r:1226 (100.0%) o:634 (99.8%)<br>fail: r:0 (0.0%) o:1 (0.2%)     |

#### Performance

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

### 1.3.0

This adds support for Draft 2020-12

This adds support for the following keywords
* `$dynamicRef`
* `$dynamicAnchor`
* `$vocabulary`

This refactors the schema retrieval codes as the ID is based on IRI and not URI. 

Note that Java does not support IRIs. See https://cr.openjdk.org/%7Edfuchs/writeups/updating-uri/ for details.

The following are removed and replaced by `SchemaLoader` and `SchemaMapper`.
* `URIFactory` - No replacement. The resolve logic is in `AbsoluteIRI`.
* `URISchemeFactory` - No replacement as `URIFactory` isn't required anymore.
* `URISchemeFetcher` - No replacement. The `SchemaLoaders` are iterated and called.
* `URITranslator` - Replaced by `SchemaMapper`.
* `URLFactory` - No replacement as `URIFactory` isn't required anymore.
* `URLFetcher` - Replaced by `UriSchemaLoader`.
* `URNURIFactory` - No replacement as `URIFactory` isn't required anymore.

The `SchemaLoader` and `SchemaMapper` are configured in the `JsonSchemaFactory.Builder`. See [Customizing Schema Retrieval](schema-retrieval.md).

As per the specification. The `format` keyword since Draft 2019-09 no longer generates assertions by default.

This can be changed by using a custom meta schema with the relevant `$vocabulary` or by setting the execution configuration to enable format assertions.

### 1.2.0

The following are a summary of the changes
* Paths are now specified using the `JsonNodePath`. The paths are `instanceLocation`, `schemaLocation` and `evaluationPath`. The meaning of these paths are as defined in the [specification](https://github.com/json-schema-org/json-schema-spec/blob/main/jsonschema-validation-output-machines.md).
* Schema Location comprises an absolute IRI component and a fragment that is a `JsonNodePath` that is typically a JSON pointer
* Rename `at` to `instanceLocation`. Note that for the `required` validator the error message `instanceLocation` does not point to the missing property to be consistent with the [specification](https://json-schema.org/draft/2020-12/json-schema-core#section-12.4.2).  The `ValidationMessage` now contains a `property` attribute if this is required.
* Rename `schemaPath` to `schemaLocation`. This should generally be an absolute IRI with a fragment particularly in later drafts.
* Add `evaluationPath`

`JsonValidator`
* Now contains `getSchemaLocation` and `getEvaluationPath` in the interface
* Implementations now need a constructor that takes in `schemaLocation` and `evaluationPath`
* The `validate` method uses `JsonNodePath` for the `instanceLocation`
* The `validate` method with just the `rootNode` has been removed

`JsonSchemaWalker`
* The `walk` method uses `JsonNodePath` for the `instanceLocation`

`WalkEvent`
* Rename `at` to `instanceLocation`
* Rename `schemaPath` to `schemaLocation`
* Add `evaluationPath`
* Rename `keyWordName` to `keyword`

`WalkListenerRunner`
* Rename `at` to `instanceLocation`
* Rename `schemaPath` to `schemaLocation`
* Add `evaluationPath`

`BaseJsonValidator`
* The `atPath` methods are removed. Use `JsonNodePath.append` to get the path of the child
* The `buildValidationMessage` methods are removed. Use the `message` builder method instead.

`CollectorContext`
* The `evaluatedProperties` and `evaluatedItems` are now `Collection<JsonNodePath>`

`JsonSchema`
* The validator keys are now using `evaluationPath` instead of `schemaPath`
* The `@deprecated` constructor methods have been removed

`ValidatorTypeCode`
* The `customMessage` has been removed. This made the `ValidatorTypeCode` mutable if the feature was used as the enum is a shared instance. The logic for determining the `customMessage` has been moved to the validator. 
* The creation of `newValidator` instances now uses a functional interface instead of reflection.

`ValidatorState`
* The `ValidatorState` is now a property of the `ExecutionContext`. This change is largely to improve performance. The `CollectorContext.get` method is particularly slow for this use case.

### 1.1.0

Removes use of `ThreadLocal` to store context and explicitly passes the context as a parameter where needed.

The following are the main API changes, typically to accept an `ExecutionContext` as a parameter

* `com.networknt.schema.JsonSchema`
* `com.networknt.schema.JsonValidator`
* `com.networknt.schema.Format`
* `com.networknt.schema.walk.JsonSchemaWalker`
* `com.networknt.schema.walk.WalkEvent`

`JsonSchema` was modified to optionally accept an `ExecutionContext` for the `validate`, `validateAndCollect` and `walk` methods. For methods where no `ExecutionContext` is supplied, one is created for each run in the `createExecutionContext` method in `JsonSchema`.

`ValidationResult` was modified to store the `ExecutionContext` of the run which is also a means of reusing the context, by passing this context information from the `ValidationResult` to following runs.

### 1.0.82

Up to version [1.0.81](https://github.com/networknt/json-schema-validator/blob/1.0.81/pom.xml#L99), the dependency `org.apache.commons:commons-lang3` was included as a runtime dependency. Starting with [1.0.82](https://github.com/networknt/json-schema-validator/releases/tag/1.0.82) it is not required anymore.