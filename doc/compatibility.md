## Compatibility with JSON Schema versions

[![Supported Dialects](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fsupported_versions.json)](https://bowtie.report/#/implementations/java-networknt-json-schema-validator)
[![Draft 2020-12](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft2020-12.json)](https://bowtie.report/#/dialects/draft2020-12)
[![Draft 2019-09](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft2019-09.json)](https://bowtie.report/#/dialects/draft2019-09)
[![Draft 7](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft7.json)](https://bowtie.report/#/dialects/draft7)
[![Draft 6](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft6.json)](https://bowtie.report/#/dialects/draft6)
[![Draft 4](https://img.shields.io/endpoint?url=https%3A%2F%2Fbowtie.report%2Fbadges%2Fjava-com.networknt-json-schema-validator%2Fcompliance%2Fdraft4.json)](https://bowtie.report/#/dialects/draft4)

The `pattern` and `format` `regex` validator by default uses the JDK regular expression implementation which is not ECMA-262 compliant and is thus not compliant with the JSON Schema specification. The library can however be configured to use a ECMA-262 compliant regular expression implementation such as `GraalJS` or `Joni`.

Annotation processing and reporting are implemented. Note that the collection of annotations will have an adverse performance impact.

This implements the Flag, List and Hierarchical output formats defined in the [Specification for Machine-Readable Output for JSON Schema Validation and Annotation](https://github.com/json-schema-org/json-schema-spec/blob/8270653a9f59fadd2df0d789f22d486254505bbe/jsonschema-validation-output-machines.md).

The implementation supports the use of custom keywords, formats, vocabularies and meta-schemas.

### Date-time Format

When format assertions are enabled, `date-time` requires `T` or `t` between the date and time,
as defined by the [RFC 3339 date-time grammar](https://www.rfc-editor.org/rfc/rfc3339.html#section-5.6).
A space separator is not valid for this JSON Schema format. For Draft 2019-09 and later,
`format` is an annotation by default; set `formatAssertionsEnabled` to `true` to validate it.

### Known Issues

There are currently no known issues with the required functionality from the specification.

The following are the tests results after running the [JSON Schema Test Suite](https://github.com/json-schema-org/JSON-Schema-Test-Suite) as at 13 Oct 2025 using version 2.0.0. As the test suite is continously updated, this can result in changes in the results subsequently.

| Implementations | Overall                                                              | DRAFT_03 | DRAFT_04                                                           | DRAFT_06                                                           | DRAFT_07                                                           | DRAFT_2019_09                                                       | DRAFT_2020_12                                                       |
| --------------- | -------------------------------------------------------------------- | -------- | ------------------------------------------------------------------ | ------------------------------------------------------------------ | ------------------------------------------------------------------ | ------------------------------------------------------------------- | ------------------------------------------------------------------- |
| NetworkNt       | pass: r:4840 (100.0%) o:2421 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) |          | pass: r:610 (100.0%) o:255 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) | pass: r:829 (100.0%) o:322 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) | pass: r:913 (100.0%) o:554 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) | pass: r:1227 (100.0%) o:639 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) | pass: r:1261 (100.0%) o:651 (100.0%)<br>fail: r:0 (0.0%) o:0 (0.0%) |

### Legend

| Symbol | Meaning               |
|:------:|:----------------------|
|   🟢   | Fully implemented     |
|   🟡   | Partially implemented |
|   🔴   | Not implemented       |
|   🚫   | Not defined           |

### Keywords Support

| Keyword                    | Draft 4 | Draft 6 | Draft 7 | Draft 2019-09 | Draft 2020-12 |
|:---------------------------|:-------:|:-------:|:-------:|:-------------:|:-------------:|
| $anchor                    | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| $defs                      | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| $dynamicAnchor             | 🚫 | 🚫 | 🚫 | 🚫 | 🟢 |
| $dynamicRef                | 🚫 | 🚫 | 🚫 | 🚫 | 🟢 |
| $id                        | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| $recursiveAnchor           | 🚫 | 🚫 | 🚫 | 🟢 | 🚫 |
| $recursiveRef              | 🚫 | 🚫 | 🚫 | 🟢 | 🚫 |
| $ref                       | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| $vocabulary                | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| additionalItems            | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| additionalProperties       | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| allOf                      | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| anyOf                      | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| const                      | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| contains                   | 🚫 | 🟢 | 🟢 | 🟢 | 🟢 |
| contentEncoding            | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| contentMediaType           | 🚫 | 🚫 | 🟢 | 🟢 | 🟢 |
| contentSchema              | 🚫 | 🚫 | 🚫 | 🟢 | 🟢 |
| definitions                | 🟢 | 🟢 | 🟢 | 🚫 | 🚫 |
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

In accordance with the specification, unknown keywords are treated as annotations. This is customizable by configuring a unknown keyword factory on the respective meta-schema.

#### Content Encoding

Since Draft 2019-09, the `contentEncoding` keyword does not generate assertions.

#### Content Media Type

Since Draft 2019-09, the `contentMediaType` keyword does not generate assertions.

#### Content Schema

The `contentSchema` keyword does not generate assertions.

#### Pattern

By default the `pattern` keyword uses the JDK regular expression implementation validating regular expressions. 

This is not ECMA-262 compliant and is thus not compliant with the JSON Schema specification. This is however the more likely desired behavior as other logic will most likely be using the default JDK regular expression implementation to perform downstream processing.

The library can be configured to use a ECMA-262 compliant regular expression validator which is implemented using [GraalJS](https://github.com/oracle/graaljs) or [Joni](https://github.com/jruby/joni). This can be configured by setting `setRegularExpressionFactory` to the respective `GraalJSRegularExpressionFactory` or `JoniRegularExpressionFactory` instances.

This also requires adding the `org.graalvm.js:js` or `org.jruby.joni:joni` dependency.

```xml
<dependency>
    <!-- Used to validate ECMA 262 regular expressions -->
    <!-- Approximately 50 MB in dependencies -->
    <!-- GraalJSRegularExpressionFactory -->
    <groupId>org.graalvm.js</groupId>
    <artifactId>js</artifactId>
    <version>${version.graaljs}</version>
</dependency>

<dependency>
    <!-- Used to validate ECMA 262 regular expressions -->
    <!-- Approximately 2 MB in dependencies -->
    <!-- JoniRegularExpressionFactory -->
    <groupId>org.jruby.joni</groupId>
    <artifactId>joni</artifactId>
    <version>${version.joni}</version>
</dependency>
```

#### Format

Since Draft 2019-09 the `format` keyword only generates annotations by default and does not generate assertions.

This can be configured on a schema basis by using a meta schema with the appropriate vocabulary.

| Version               | Vocabulary                                                    | Value             |
|:----------------------|---------------------------------------------------------------|-------------------|
| Draft 2019-09         | `https://json-schema.org/draft/2019-09/vocab/format`          | `true`            |
| Draft 2020-12         | `https://json-schema.org/draft/2020-12/vocab/format-assertion`| `true`/`false`    | 

This behavior can be overridden to generate assertions by setting the `setFormatAssertionsEnabled` option to `true`.

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

##### Unknown Formats

When the format assertion vocabularies are used in a meta schema, in accordance to the specification, unknown formats will result in assertions. If the format assertion vocabularies are not used, unknown formats will only result in assertions if the assertions are enabled and if `strict("format", true)` for `SchemaRegistryConfig`.

##### Footnotes
1. Note that the validation are only optional for some of the keywords/formats.
2. Refer to the corresponding JSON schema for more information on whether the keyword/format is optional or not.

## Loose numeric enum strings

With `SchemaRegistryConfig.typeLoose(true)`, finite numeric enum members match string instances by exact decimal value. For `{"enum":[100]}`, `"100"`, `"100.0"`, `"1e2"` and `"1E+2"` all match on both branches. With loose typing disabled, these strings do not match numeric `100`.

This deliberately broadens the old behavior: master formerly used normalized decimal text (for example `"1E+2"`), while 2.x used plain text (`"100"`). Applications relying on rejection of equivalent spellings should disable loose typing or constrain the allowed strings explicitly.

Strings must follow JSON number syntax: optional minus, an integer without leading zeros, an optional fraction with digits after the decimal point, and an optional exponent with digits. Whitespace, a leading plus, hexadecimal forms, underscores and incomplete fractions/exponents do not match a numeric member. Exponent signs and leading exponent zeros are allowed. Positive and negative zero compare equally, including zero with arbitrarily long exponents. Nonzero exponents outside any representable enum value simply do not match; parsing never expands exponent-form numbers or constructs an arbitrarily large exponent integer.

Only top-level numeric enum members receive this conversion. A literal string member such as `"1E+2"` remains an exact string, and arrays/objects are not coerced from strings or traversed for loose numeric coercion. Existing scalar matching for booleans, null and explicitly supplied non-finite numbers is retained; non-finite numeric members keep their existing exact text spellings rather than accepting huge finite exponent strings.

The comparison uses each numeric enum node's existing decimal value, consistent with numeric value equality. It does not recover digits already rounded by a caller's mapper or the configured schema reader. Protected enum conversion hooks remain in use.

For example, a schema tree supplied by an external plain mapper can already have rounded `{"enum":[9007199254740993.0]}` to a double-valued member. Loose matching then accepts `"9007199254740992"`, not `"9007199254740993"`. Built-in readers and the public mapper factories now preserve this finite decimal spelling, so they match the written value. Use those readers, configure an external mapper with `USE_BIG_DECIMAL_FOR_FLOATS`, or supply an exact DecimalNode when the original digits matter.


## RFC 3339 date-time boundaries

The `date-time` format requires a four-digit full date, `T`/`t`, hours/minutes/seconds, and `Z`/`z` or a complete signed `HH:MM` offset. Both validation paths accept the unknown-local-offset spelling `-00:00`, offsets through ±23:59, and one or more fractional-second digits without a nine-digit limit. Space separators, empty fractions, missing seconds, hour-only offsets and trailing text are rejected.

For example, `2004-09-25T09:55:11-00:00`, `2020-01-01T12:00:00.1234567890Z` and `2020-01-01T12:00:00+23:59` are valid. `2020-01-01 12:00:00Z`, `2020-01-01T12:00:00.Z` and `2020-01-01T12:00:00+24:00` are invalid. This changes the Ethlo-present behavior for the first four boundaries to agree with RFC 3339 and the fallback.

Leap-second accuracy remains dependency-specific: Ethlo checks the historical insertion table; the no-Ethlo fallback permits possible leap seconds at the end of June/December UTC without verifying an actual insertion. Offsets crossing midnight are evaluated against the UTC date, including when the local year differs. Ordinary valid date-times at years 0000 and 9999 remain accepted even if their UTC date crosses the four-digit-year boundary. Validation does not rewrite or truncate the input string.

These include **breaking validation changes with Ethlo present**: space-separated strings such as `2020-01-01 12:00:00Z` and offset-less leap seconds such as `2016-12-31T23:59:60` were accepted before and are now rejected. Supply `T`/`t` and an explicit `Z`/`z` or signed `HH:MM` offset. The no-Ethlo fallback already rejected both forms.

The historical calendars are intentionally not made identical. Ethlo's current table rejects the real leap seconds on 1979-12-31 and 1987-12-31 and accepts 1971-12-31 and 1988-12-31, which did not have insertions. The fallback accepts all four as possible December-end leap seconds. These inherited differences are retained and pinned by tests; the change aligns syntax and offset/range handling, not historical leap-table data.

## Invalid `multipleOf` divisors

See [Selective exact decimal tree reading](#selective-exact-decimal-tree-reading) for the underflow/overflow distinction: default readers preserve `multipleOf: 1e-400` exactly, while `multipleOf: 1e400` still overflows to Infinity and is ignored.

The specification requires `multipleOf` to be a number strictly greater than 0. Schemas that break this are not rejected when they are loaded, and the library keeps the following behavior in all supported drafts:

- A zero divisor (`0`, `0.0`, `-0.0`) is ignored, so every number is valid.
- A negative divisor uses its absolute value: with `"multipleOf": -2`, `4` and `-4` are valid and `5` is not.

Divisors and bounds outside the range of a double depend on how the schema numbers are represented:

- With the default readers, a divisor that overflows a double (such as `1e400`) becomes infinite and is ignored. A bound beyond the double range compares as an infinite bound: `minimum: 1e400` rejects `5`, and `maximum: 1e400` accepts it. Finite underflow such as `1e-400` is preserved by selective decimal reading, so `multipleOf: 1e-400` rejects `5e-401`. A tree supplied by an external plain double-parsing mapper can still have rounded that divisor to zero, in which case it is ignored.
- With exact decimals, that is nodes supplied as `BigDecimal` or a reader configured with `USE_BIG_DECIMAL_FOR_FLOATS`, the value keeps its exact size. `multipleOf: 1e400` then rejects `1`, `multipleOf: 1e-400` rejects `5e-401`, and bounds such as `1e400` compare as finite decimals.

`minimum` and `maximum`, and the numeric `exclusiveMinimum` and `exclusiveMaximum` of draft 6 and later, accept any number. The invalid schemas above are reported by validating the schema against its meta-schema, for example the `$schema` URI of the dialect. Rejecting them while loading a schema, or defining an exact policy for non-finite divisors and bounds, would be a behavior change that needs its own decision.


## Selective exact decimal tree reading

`JsonMapperFactory.getInstance()` and `YamlMapperFactory.getInstance()` now return `DecimalNode` values for lossy finite floating-point spellings when reading trees, subject to explicit mapper settings and custom node factories.

Built-in JSON/YAML readers and `JsonMapperFactory`/`YamlMapperFactory` tree reads preserve finite floating-point literals whose written decimal differs from `BigDecimal.valueOf(parsedDouble)`. This is the library's shortest-decimal interpretation of a double, not its exact binary expansion. Common lossless values such as `0.1` and `1.5` remain DoubleNodes; `9007199254740993.0`, `0.99999999999999999999` and finite underflow such as `1e-400` become DecimalNodes. Integers keep their native reader policy.

The policy is uniform across schemas, instances, `const`/`enum` literal data, defaults, examples and numeric fields regardless of name. Thus identical text compares equally, while finite values that used to collapse to the same double can now compare differently. This intentionally changes validation of decimal-spelled bounds and `multipleOf` operands to their written values. `getSchemaNode()`, annotations, applied defaults and JSON output can expose exact decimals; DecimalNode scale and printing can differ between Jackson 2 and 3.

Lossless `0.0` and `-0.0` retain their DoubleNode type, sign and serialization. They still compare equally for `const` and bounds. Explicit `USE_BIG_DECIMAL_FOR_FLOATS` settings keep their existing all-decimal policy, including their existing zero representation. Native tree updates/null handling and custom node factories are delegated to Jackson. A factory deliberately rebuilding decimals as doubles can discard precision. Typed scalar/POJO binding (for example a `Double` property) retains Jackson's native behavior.

To preserve the existing divisor/bound policy, tokens that overflow double range (such as `1e400`) retain ordinary Infinity behavior outside the eight count names. Count-named values retain their prior exact promotion behavior, including `1e400`. Explicit NaN/Infinity handling is unchanged. Tokens outside BigDecimal's parsing range retain ordinary parsing; native Jackson input-size constraints still apply. This change does not recover digits from an externally supplied, already-rounded JsonNode or alter invalid-divisor/bound policy.

The public mapper factories install this behavior for JsonNode/ObjectNode/ArrayNode tree binding, not for raw streaming numeric getters or typed scalar values. Caller-supplied mappers used through NodeReader receive selective tree parsing while their explicit features and node factories are respected. Public factory readers and updates retain their native contextual, null and merge behavior.
