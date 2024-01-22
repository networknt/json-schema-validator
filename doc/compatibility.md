
### Legend

| Symbol | Meaning               |
|:------:|:----------------------|
|   🟢   | Fully implemented     |
|   🟡   | Partially implemented |
|   🔴   | Not implemented       |
|   🚫   | Not defined           |

### Compatibility with JSON Schema versions

| Keyword                    | Draft 4 | Draft 6 | Draft 7 | Draft 2019-09 | Draft 2020-12 |
|:---------------------------|:-------:|:-------:|:-------:|:-------------:|:-------------:|
| $anchor                    | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| $dynamicAnchor             | 🚫 | 🚫 | 🚫 | 🚫 | 🟢 |
| $dynamicRef                | 🚫 | 🚫 | 🚫 | 🚫 | 🟢 |
| $id                        | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| $recursiveAnchor           | 🚫 | 🚫 | 🚫 | 🟢 | 🚫 |
| $recursiveRef              | 🚫 | 🚫 | 🚫 | 🟢 | 🚫 |
| $ref                       | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| $vocabulary                | 🚫 | 🚫 | 🚫 | 🔴 | 🔴 |
| additionalItems            | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| additionalProperties       | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| allOf                      | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| anyOf                      | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| const                      | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| contains                   | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| contentEncoding            | 🚫 | 🚫 | 🔴 | 🔴 | 🔴 |
| contentMediaType           | 🚫 | 🚫 | 🔴 | 🔴 | 🔴 |
| contentSchema              | 🚫 | 🚫 | 🚫 | 🔴 | 🔴 |
| definitions                | 🟢 | 🟢 | 🟢 | 🚫 | 🚫 |
| defs                       | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| dependencies               | 🟢 | 🟢 | 🟢 | 🚫 | 🚫 |
| dependentRequired          | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| dependentSchemas           | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| enum                       | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| exclusiveMaximum (boolean) | 🟢 | 🚫 | 🚫 | 🚫 | 🚫 |
| exclusiveMaximum (numeric) | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| exclusiveMinimum (boolean) | 🟢 | 🚫 | 🚫 | 🚫 | 🚫 |
| exclusiveMinimum (numeric) | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| if-then-else               | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| items                      | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| maxContains                | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| minContains                | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| maximum                    | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| maxItems                   | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| maxLength                  | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| maxProperties              | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| minimum                    | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| minItems                   | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| minLength                  | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| minProperties              | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| multipleOf                 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| not                        | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| oneOf                      | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| pattern                    | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| patternProperties          | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| prefixItems                | 🚫 | 🚫 | 🚫 | 🚫 | 🟢 |
| properties                 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| propertyNames              | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| readOnly                   | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| required                   | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| type                       | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| unevaluatedItems           | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| unevaluatedProperties      | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| uniqueItems                | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| writeOnly                  | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |

### Semantic Validation (Format)

| Format                | Draft 4 | Draft 6 | Draft 7 | Draft 2019-09 | Draft 2020-12 |
|:----------------------|:-------:|:-------:|:-------:|:-------------:|:-------------:|
| date                  | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| date-time             | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| duration              | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| email                 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| hostname              | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| idn-email             | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| idn-hostname          | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| ipv4                  | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| ipv6                  | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| iri                   | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| iri-reference         | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| json-pointer          | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| relative-json-pointer | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| regex                 | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| time                  | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| uri                   | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| uri-reference         | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| uri-template          | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| uuid                  | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |

### Footnotes
1. Note that the validation are only optional for some of the keywords/formats.
2. Refer to the corresponding JSON schema for more information on whether the keyword/format is optional or not.

