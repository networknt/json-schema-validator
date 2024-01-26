## Upgrading to new versions

This contains information on the notable or breaking changes in each version.

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

The `SchemaLoader` and `SchemaMapper` are configured in the `JsonSchemaFactory.Builder`.

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