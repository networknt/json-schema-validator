# Issue #1350 validation: 2.x

Issue: https://github.com/networknt/json-schema-validator/issues/1350

Local verification performed on 2026-10-23 against base `6036db7`. Remote CI is a separate gate; these results do not claim a remote run.

## Behavior and compatibility

Built-in readers apply the same selective decimal parsing to schemas and instances. Only floating tokens under `minItems`, `maxItems`, `minProperties`, `maxProperties`, `minLength`, `maxLength`, `minContains` and `maxContains` are candidates. A lossy decimal becomes an ordinary DecimalNode through the configured node factory. Losslessness compares the written decimal with `BigDecimal.valueOf(double)`, preserving the library's shortest-decimal numeric interpretation. Other numeric fields retain their configured parsing.

This removes the weak-identity side table, public internal helper and internal-package export exclusion. Copies, moved nodes and rebuilt schema trees preserve count values. `JsonNodeValue` equality and hashing are unchanged; decimals are never rounded just to match doubles.

Visible changes and limits:

- `getSchemaNode()`, library-parsed instances, count-named literal fields, defaults, examples and annotations can expose DecimalNodes. Default/example objects and annotation JSON retain their shape and exact numeric values.
- A JsonNode supplied by an external plain mapper already contains rounded doubles. Its source digits cannot be recovered; it can fail exact `const`/`enum` comparison where identical library-parsed text succeeds.
- Explicit `USE_BIG_DECIMAL_FOR_FLOATS` readers bypass selective decoding. Custom readers/factories retain their numeric policies; a factory deliberately discarding BigDecimal precision cannot preserve it. Location-aware and precision-preserving rebuilding factories are covered by tests.
- DecimalNode scale and serialization can differ between Jackson 2 and 3. Annotations expose the node's own Number value and type.
- Integral floating/decimal count messages use plain digits through 100 integer digits, calculated as precision minus scale using long arithmetic. Larger decimals use scientific form without expanding extreme exponents. Integer nodes retain their full-digit contract. Read, copied and moved versions of the same numeric node produce the same message.
- Tokens outside BigDecimal's parsing range retain ordinary parsing, including Infinity saturation for huge positive exponents and zero underflow for huge negative exponents. Representable values such as `1e400` remain exact decimals.

## Full local verification

Command: `mvn -B clean verify`, with `JAVA_HOME` and `PATH` selected for each JDK.

- JDKs: 8.0.432-amzn, 11.0.25-amzn, 17.0.13-amzn, 21.0.5-amzn.
- Per run: 10,311 unit cases, 15 existing skips, and one packaged-artifact check.
- Every run passed with zero failures/errors. No additional test exclusions or skips were introduced.
- Artifact checks assert that CountTokens and CountTokenNode are absent and the module has no obsolete internal package/export.

`Issue1350Test` and `Issue1350ReviewTest` cover JSON/YAML strings/streams, all eight count names, deep copies, moved/rebuilt nodes, identical `const`/`enum` literals, exact `uniqueItems` semantics, plain-mapper limitations, defaults, annotation JSON, location-aware nodes, explicit decimal readers, rebuilding factories and fixed message expectations. Formatting tests include `3000000000.0`, `3e9`, the lossy 30-digit integral value, and signed 100-digit boundaries. Existing precision, mutation and YAML separator tests remain enabled.

## Equality and hash probe

A new deterministic probe (seed 1350) checked 2,774 numeric nodes and 7,695,076 pairs in scalar, array and object shapes: 23,085,228 shape comparisons on this branch. It included parser-promoted decimals, all seven Jackson numeric node types, exact fractional/underflow/overflow cases, random coefficients/scales and long boundaries. The oracle compared finite node decimal values, with separate nonfinite comparison, and checked `same()`, key equality and equal-value hash agreement. Zero mismatches were found.

This was a smaller new probe, not a rerun of the earlier independent 770-million-pair harness. The independent 304-schema differential probe was not rerun by this implementation session.

## Instance parsing measurement

Warm local wall-clock measurements, not JMH or a throughput guarantee. JDK 17.0.13, `-Xms512m -Xmx2g`, eight warmups and fifteen samples, alternating mapper/reader order; medians reported. Both paths use the same default JsonMapperFactory mapper. Measurements ran sequentially after the Maven processes finished.

The arrays contain 400,000 floats. Unrelated-number input: 4,800,001 bytes. Count-named-object input: 10,000,001 bytes. Named values use canonical lossless text (`0.123456789`); genuinely lossy values still need decimal work.

| Input | Raw mapper median ms | Selective reader median ms | Ratio |
| --- | ---: | ---: | ---: |
| Unrelated floats | 23.642 | 25.062 | 1.060 |
| Count-named floats | 63.984 | 79.168 | 1.237 |

The unrelated-number overhead is small in absolute time; the deliberately count-heavy case costs more and should not be described as negligible.

## Scope checks and remaining gates

The source diff against the final preceding count-limit PR head (`28714cd`) was checked. `git diff --check` passed. GitNexus cannot scan these unindexed worktrees, so its canonical-checkout results do not qualify this diff. Source review and the full Maven suites provide local verification. Remote CI and independent probes remain separate review gates.
