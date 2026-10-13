# Error Reference

This document lists the errors the validator reports for the built-in keywords and formats, and explains how to identify an error in code.

Releases before 2.0.0 gave each error a numeric code, such as `1028` for `required`, defined in `ValidatorTypeCode`. Release 2.0.0 removed these codes. An error is now identified by its keyword and its message key.

## Fields of an error

`Schema.validate` returns a list of `com.networknt.schema.Error`. Each error has the following fields.

| Method | Description | Example |
|---|---|---|
| `getKeyword()` | The keyword that failed. | `required` |
| `getMessageKey()` | The key of the message in the `jsv-messages` resource bundle. More specific than the keyword, for example `format.email` or `contains.min`. | `required` |
| `getArguments()` | The values substituted into the message. Their meaning for each key is listed below. | `[a]` |
| `getProperty()` | The property name, for errors about a single property. `null` otherwise. | `a` |
| `getIndex()` | The array index, for errors about a single array item. `null` otherwise. | `1` |
| `getDetails()` | A map holding `property` or `index`. | `{property=a}` |
| `getInstanceLocation()` | Where in the instance the error is, as a JSON Pointer by default. | `/orders/0` |
| `getEvaluationPath()` | The path through the schema, including any `$ref`, that led to the keyword. | `/properties/orders/items/required` |
| `getSchemaLocation()` | The absolute location of the keyword in the schema. | `https://example.com/order#/required` |
| `getMessage()` | The localised message, without the location. | `required property 'a' not found` |
| `isCustomMessage()` | `true` if the message came from the schema's custom error message keyword. See [Custom Error Messages](error-message.md). | `false` |
| `toString()` | The instance location and the message. | `/orders/0: required property 'a' not found` |

## Identifying an error

Use the message key to identify the kind of error, for example to map errors to your own codes or to a page of your documentation. The message key is the same in every locale, while the message text changes with the locale.

```java
List<Error> errors = schema.validate(input, InputFormat.JSON);
for (Error error : errors) {
    String link = "https://example.com/docs/validation-errors#" + error.getMessageKey();
    System.out.println(error.getInstanceLocation() + ": " + error.getMessage() + " (see " + link + ")");
}
```

The keyword alone does not always tell errors apart. Every `format` error has the keyword `format`, but the message key names the format, such as `format.email` or `format.date`. `oneOf` reports `oneOf` when no schema matched and `oneOf.indexes` when more than one matched.

When a schema supplies a custom message through the configured error message keyword, `getMessage()` returns the custom message and `getMessageKey()` still returns the key, so the same code works for both. Use `isCustomMessage()` to tell them apart.

## Message keys

The default messages are in [jsv-messages.properties](../src/main/resources/jsv-messages.properties). In the arguments column, `{0}` and `{1}` refer to `getArguments()[0]` and `getArguments()[1]`. Some keys have more arguments than their default message uses; these are still available from `getArguments()`.

### Type and value

| Message key | Keyword | Reported when | Arguments | Details | Default message |
|---|---|---|---|---|---|
| `type` | `type` | The instance has a type the schema does not allow. | `{0}` the instance type, `{1}` the allowed type or types | | `{0} found, {1} expected` |
| `const` | `const` | The instance is not the constant value. | `{0}` the constant, `{1}` the instance value | | `must be the constant value '{0}'` |
| `enum` | `enum` | The instance is not one of the listed values. | `{0}` the list of values | | `does not have a value in the enumeration {0}` |
| `unionType` | `unionType` | The instance matches none of the types of a Draft 3 style union type. Only in dialects that add `unionType`. | `{0}` the instance type, `{1}` the allowed types | | `{0} found, {1} expected` |

### Numbers

| Message key | Keyword | Reported when | Arguments | Default message |
|---|---|---|---|---|
| `minimum` | `minimum` | The number is below the minimum. In Draft 4, also when it equals the minimum and `exclusiveMinimum` is `true`. | `{0}` the minimum | `must have a minimum value of {0}` |
| `maximum` | `maximum` | The number is above the maximum. In Draft 4, also when it equals the maximum and `exclusiveMaximum` is `true`. | `{0}` the maximum | `must have a maximum value of {0}` |
| `exclusiveMinimum` | `exclusiveMinimum` | The number is not greater than the limit. Draft 6 and later. | `{0}` the limit | `must have an exclusive minimum value of {0}` |
| `exclusiveMaximum` | `exclusiveMaximum` | The number is not less than the limit. Draft 6 and later. | `{0}` the limit | `must have an exclusive maximum value of {0}` |
| `multipleOf` | `multipleOf` | The number is not a multiple of the divisor. | `{0}` the divisor | `must be multiple of {0}` |

### Strings

| Message key | Keyword | Reported when | Arguments | Default message |
|---|---|---|---|---|
| `minLength` | `minLength` | The string is too short. Length is counted in code points. | `{0}` the minimum length | `must be at least {0} characters long` |
| `maxLength` | `maxLength` | The string is too long. | `{0}` the maximum length | `must be at most {0} characters long` |
| `pattern` | `pattern` | The string does not match the regular expression. | `{0}` the pattern | `does not match the regex pattern {0}` |
| `contentEncoding` | `contentEncoding` | The string is not valid in the encoding, for example not valid base64. Draft 7 only; later drafts treat it as an annotation. | `{0}` the encoding | `does not match content encoding {0}` |
| `contentMediaType` | `contentMediaType` | The string is not valid for the media type, for example not valid JSON. Draft 7 only. | `{0}` the media type | `is not a content media type` |

### Formats

Format errors are only reported when format assertions are on: either `formatAssertionsEnabled(true)` in the execution config, or a dialect that uses the format-assertion vocabulary. The keyword is always `format`.

| Message key | Arguments | Default message |
|---|---|---|
| `format.<name>` | `{0}` the format name, `{1}` the instance value | See below |
| `format` | `{0}` the format name, `{1}` the instance value | `does not match the {0} pattern` |
| `format.unknown` | `{0}` the format name | `has an unknown format '{0}'` |

The built-in formats have their own key: `format.date`, `format.date-time`, `format.duration`, `format.email`, `format.hostname`, `format.idn-email`, `format.idn-hostname`, `format.ipv4`, `format.ipv6`, `format.iri`, `format.iri-reference`, `format.json-pointer`, `format.regex`, `format.relative-json-pointer`, `format.time`, `format.uri`, `format.uri-reference`, `format.uri-template` and `format.uuid`. Their messages name the standard the value must follow, for example `does not match the email pattern must be a valid RFC 5321 Mailbox`. A custom `Format` chooses its key through `getMessageKey()`, which defaults to `format`.

`format.unknown` is reported for a format the validator does not know, but only when format assertions are on and `format` is configured as strict with `SchemaRegistryConfig.builder().strict("format", true)`, or when the dialect uses the format-assertion vocabulary.

### Arrays

| Message key | Keyword | Reported when | Arguments | Details | Default message |
|---|---|---|---|---|---|
| `minItems` | `minItems` | The array has too few items. | `{0}` the minimum, `{1}` the actual count | | `must have at least {0} items but found {1}` |
| `maxItems` | `maxItems` | The array has too many items. | `{0}` the maximum, `{1}` the actual count | | `must have at most {0} items but found {1}` |
| `uniqueItems` | `uniqueItems` | Two items are equal. Numbers are compared by value, so `1` and `1.0` are equal. | none | | `must have only unique items in the array` |
| `items` | `items` | An item is beyond `prefixItems` and `items` is `false`. Draft 2020-12. | `{0}` the index | `index` | `index '{0}' is not defined in the schema and the schema does not allow additional items` |
| `additionalItems` | `additionalItems` | An item is beyond the `items` array and `additionalItems` is `false`. Draft 2019-09 and earlier. | `{0}` the index | `index` | `index '{0}' is not defined in the schema and the schema does not allow additional items` |
| `unevaluatedItems` | `unevaluatedItems` | An item was not evaluated by any other keyword and `unevaluatedItems` is `false`. | `{0}` the index | | `index '{0}' is not evaluated and the schema does not allow unevaluated items` |
| `contains` | `contains` | No item matches the `contains` schema. Draft 6 and 7. | `{0}` the required count (1), `{1}` the `contains` schema | | `does not contain an element that passes these validations: {1}` |
| `contains.min` | `minContains` | Fewer items than `minContains` match the `contains` schema. Draft 2019-09 and later. Also reported when `minContains` is absent and no item matches, because it defaults to 1. | `{0}` the minimum count, `{1}` the `contains` schema | | `must contain at least {0} element(s) that passes these validations: {1}` |
| `contains.max` | `maxContains` | More items than `maxContains` match the `contains` schema. Draft 2019-09 and later. | `{0}` the maximum count, `{1}` the `contains` schema | | `must contain at most {0} element(s) that passes these validations: {1}` |
| `minContains` | `minContains` | `minContains` is not a non-negative integer. | `{0}` the schema object that contains it | | `must be a non-negative integer in {0}` |
| `maxContains` | `maxContains` | `maxContains` is not a non-negative integer. | `{0}` the schema object that contains it | | `must be a non-negative integer in {0}` |

### Objects

| Message key | Keyword | Reported when | Arguments | Details | Default message |
|---|---|---|---|---|---|
| `required` | `required` | A required property is missing. One error per missing property. | `{0}` the property name | `property` | `required property '{0}' not found` |
| `minProperties` | `minProperties` | The object has too few properties. | `{0}` the minimum | | `must have at least {0} properties` |
| `maxProperties` | `maxProperties` | The object has too many properties. | `{0}` the maximum | | `must have at most {0} properties` |
| `additionalProperties` | `additionalProperties` | A property is not allowed by `properties` or `patternProperties`, and `additionalProperties` is `false`. One error per property. | `{0}` the property name | `property` | `property '{0}' is not defined in the schema and the schema does not allow additional properties` |
| `unevaluatedProperties` | `unevaluatedProperties` | A property was not evaluated by any other keyword and `unevaluatedProperties` is `false`. | `{0}` the property name | `property` | `property '{0}' is not evaluated and the schema does not allow unevaluated properties` |
| `propertyNames` | `propertyNames` | A property name does not match the `propertyNames` schema. | `{0}` the property name, `{1}` the message from the failed name check | `property` | `property '{0}' name is not valid: {1}` |
| `dependentRequired` | `dependentRequired` | A property is present but a property it depends on is missing. Draft 2019-09 and later. | `{0}` the missing property, `{1}` the property that is present | `property` | `has a missing property '{0}' which is dependent required because '{1}' is present` |
| `dependencies` | `dependencies` | A property is present but a property listed for it in `dependencies` is missing. Draft 7 and earlier. | `{0}` the list of dependencies | `property` | `has an error with dependencies {0}` |
| `notAllowed` | `notAllowed` | A property listed in `notAllowed` is present. Only in dialects that add `notAllowed`. | `{0}` the property name | `property` | `property '{0}' is not allowed but it is in the data` |
| `readOnly` | `readOnly` | A `readOnly` value is present and the execution config sets `readOnly(true)`. | none | | `is a readonly field, it cannot be changed` |
| `writeOnly` | `writeOnly` | A `writeOnly` value is present and the execution config sets `writeOnly(true)`. | none | | `is a write-only field, it cannot appear in the data` |

### Combining schemas

| Message key | Keyword | Reported when | Arguments | Default message |
|---|---|---|---|---|
| `oneOf` | `oneOf` | No schema in `oneOf` matched. The errors from each schema are reported as well. | `{0}` the number of matching schemas (0), `{1}` an empty string | `must be valid to one and only one schema, but {0} are valid` |
| `oneOf.indexes` | `oneOf` | More than one schema in `oneOf` matched. | `{0}` the number of matching schemas, `{1}` the indexes of the matching schemas, separated by `, ` | `must be valid to one and only one schema, but {0} are valid with indexes '{1}'` |
| `not` | `not` | The instance matches the `not` schema. | `{0}` the `not` schema | `must not be valid to the schema {0}` |
| `false` | `false` | The schema is `false`, so nothing is valid against it. | `{0}` the name of the keyword or property that holds the `false` schema, or `null` when the root schema is `false` | `schema for '{0}' is false` |

`allOf`, `anyOf`, `if`/`then`/`else`, `dependentSchemas`, `properties`, `patternProperties`, `prefixItems`, `items` (with a schema), `$ref`, `$dynamicRef` and `$recursiveRef` do not report errors of their own. When the instance fails them, the errors of the keywords inside them are reported, and `getEvaluationPath()` shows the path through them. For example, `{"properties": {"a": {"type": "string"}}}` with `{"a": 1}` reports a `type` error at instance location `/a` with evaluation path `/properties/a/type`. If `a` is defined through `"$ref": "#/$defs/s"`, the evaluation path is `/properties/a/$ref/type` and the schema location ends in `#/$defs/s/type`.

The resource bundle also has keys named `$ref`, `allOf`, `anyOf`, `dependentSchemas`, `patternProperties`, `prefixItems` and `properties`. No built-in keyword reports these keys. They are kept so that custom message sources written for earlier releases still load.

### OpenAPI discriminator

These are only reported by the OpenAPI 3.0 and 3.1 dialects.

| Message key | Keyword | Reported when | Arguments | Default message |
|---|---|---|---|---|
| `discriminator.missing_discriminating_value` | `discriminator` | The discriminator property is missing from the instance. | `{0}` the property name | `required property '{0}' for discriminator not found` |
| `discriminator.oneOf.no_match_found` | `discriminator` | No `oneOf` schema matches the discriminator value. | `{0}` the discriminator value | `no matching schema found for discriminator value '{0}'` |
| `discriminator.anyOf.no_match_found` | `discriminator` | No `anyOf` schema matches the discriminator value. | `{0}` the discriminator value | `no matching schema found for discriminator value '{0}'` |

## Errors in a schema

Problems in the schema itself are not returned from `validate`. They are thrown as a `SchemaException` or one of its subclasses when the schema is loaded or first used, and `getError()` on the exception returns the `Error`.

| Exception | Keyword | Reported when | Message key | Message |
|---|---|---|---|---|
| `InvalidSchemaRefException` | `$ref`, `$dynamicRef` or `$recursiveRef` | A reference cannot be resolved. | `internal.unresolvedRef` | `Reference {0} cannot be resolved` |
| `SchemaException` | `$recursiveRef` | The value is not `#`. | `internal.invalidRecursiveRef` | `The value of a $recursiveRef must be '#' but is '{0}'` |
| `SchemaException` | `$recursiveAnchor` | The value is not a boolean. | `internal.invalidRecursiveAnchor` | `The value of a $recursiveAnchor must be a Boolean literal but is {0}` |
| `InvalidSchemaException` | `id` or `$id` | The configured `SchemaIdValidator` rejects an identifier. | `id` | `'{0}' is not a valid {1}` |
| `InvalidSchemaException` | | An unknown keyword is used and the dialect uses `DisallowUnknownKeywordFactory`. | the message pattern | `Keyword '{0}' is unknown and must be configured on the meta-schema or vocabulary` |
| `SchemaException` | `allOf`, `anyOf` or `oneOf` | The keyword's value is not an array. | `type` | `{0} found, {1} expected` |

The `internal.*` keys are not in the resource bundle, so these messages are always in English. An error created without a message key, such as the unknown keyword error, uses its message pattern as the key.

## Messages in other languages

The messages are translated into several languages. The message keys and arguments are the same in every language. See [Multiple Language](multiple-language.md) for how to choose a locale or supply your own messages.
