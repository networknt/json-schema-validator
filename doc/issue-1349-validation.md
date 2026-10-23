# Issue #1349 validation: 2.x

Issue: https://github.com/networknt/json-schema-validator/issues/1349

Local verification performed on 2026-10-23 against base `0ee8ffd`. The user selected exact numeric-value matching for loose numeric enum strings on both branches.

## Changes

- Numeric enum 100 accepts strings 100, 100.0, 1e2 and 1E+2 in typeLoose mode. Strict mode and literal string enum members retain their semantics.
- NumericStringValue is a package-private immutable key storing sign, significant coefficient digits and a long decimal power. JSON number text is scanned linearly, with a saturating long exponent; no arbitrary-size exponent BigInteger or expanded zero suffix is created. Saturation is beyond any BigDecimal coefficient/scale combination. Zero still requires a complete valid grammar before its exponent is ignored.
- Keys are prepared once per distinct converted enum value. Each string is parsed at most once for numeric lookup, with separate exact scalar-text matching. Strict mode reuses empty sets.
- Existing protected numeric/array conversion hooks remain in use; containers are not coerced from strings. Non-finite members preserve their previous exact text spellings and do not match huge finite exponent strings.
- The numeric key uses the node's existing decimalValue(); precision already lost by schema parsing is not recovered. JsonNodeValue equality/hashing are untouched.
- Changelog and doc/compatibility.md describe the intentional expansion in accepted spellings on both branches, JSON number grammar, signed zero, finite/nonfinite boundaries and migration guidance.

## Verification

Command: `mvn -B clean verify`, selecting each JDK with `JAVA_HOME` and `PATH`.


| Branch | JDK | Unit cases | Existing skips | Artifact checks | Result |
| --- | --- | ---: | ---: | ---: | --- |
| 2x | 11.0.25-amzn | 10322 | 15 | 1 | PASS |
| 2x | 17.0.13-amzn | 10322 | 15 | 1 | PASS |
| 2x | 21.0.5-amzn | 10322 | 15 | 1 | PASS |
| 2x | 8.0.432-amzn | 10322 | 15 | 1 | PASS |

- 2.x: 10,322 cases plus one artifact check on JDK 8/11/17/21; 15 existing skips.
- Issue1349Test covers all five supported dialects; accepted equivalent forms; malformed/whitespace/Unicode/leading-zero text; strict and literal-string behavior; containers, boolean text and equivalent members; exact integers above 2^53; signed zero; all finite Jackson numeric node types; explicit NaN/Infinity member spellings; BigDecimal minimum/maximum scales; 100,000-digit exponents; and long fractional mantissas.
- NumericStringValueTest checks one million pairs against BigDecimal comparison, 1,000 rescaled/engineering spellings with hash agreement, and 20,000 generated strings against an independent JSON-number regex.
- Updated pre-existing branch-specific loose enum tests to assert the selected numeric-value policy. All existing numeric equality and protected conversion-hook tests remain active.

## Limits

- These are local results; remote CI is a separate acceptance gate.
- GitNexus reported LOW as a lower-bound estimate for EnumValidator, with zero direct indexed callers/processes and warnings that interface dispatch is untraced. It cannot scan the two unindexed worktrees, so it does not qualify the final diff. Source review, regression tests, complete JDK matrices and git diff --check provide local evidence.
- No performance claims or reader-policy changes are included. Numeric coercion is limited to finite top-level enum members, plus the separately retained non-finite exact-text behavior.
