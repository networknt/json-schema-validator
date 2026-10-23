# Issue #1316 validation: 2.x

Issue: https://github.com/networknt/json-schema-validator/issues/1316

Local verification on 2026-10-23 against base `8e3a99c`. These results do not claim remote CI; that is a separate gate.

## Scope

The fallback fixes from #1335/#1336 are already merged. This change addresses the remaining Ethlo-present defects and preserves those earlier fixes.

| Reported requirement | Final behavior |
| --- | --- |
| Space separator | Rejected; T/t required |
| Unknown local offset -00:00 | Accepted |
| More than nine fraction digits | Accepted; original string unchanged |
| Empty fraction | Rejected |
| Offsets beyond ±18:00 | Accepted through ±23:59; hours 24 or minutes 60 rejected |
| Missing seconds / hour-only offsets | Rejected |

Both dependency paths use the existing full-date/full-time grammar checks. Ordinary timestamps do not require OffsetDateTime conversion. For leap seconds, validated time fields determine the UTC date; Ethlo receives only a canonical UTC leap-second candidate. Its offset and fraction representation limits therefore do not reject otherwise valid written strings.

Ethlo's historical table remains authoritative when the dependency is present. The no-Ethlo fallback still accepts possible June/December UTC leap seconds without checking actual insertion history. That existing accuracy distinction is retained and documented. Ordinary timestamps with four-digit written years remain valid even when their UTC date crosses into year -1 or 10000. No instance string is rewritten.

No public APIs, dependencies, TimeFormat or DateFormat semantics were changed. The public JavaTimeOffsetDateTime entry point remains available; a shared private validator parses the date/time and computes the UTC leap date once, passing that date to Ethlo for the optional table check.

## Full local JDK matrix

Command: `mvn -B clean verify`, selecting JAVA_HOME and PATH for each SDKMAN JDK.

| JDK | Unit cases | Existing skips | Artifact checks | Result |
| --- | ---: | ---: | ---: | --- |
| 11.0.25-amzn | 10,364 | 15 | 1 | PASS |
| 17.0.13-amzn | 10,364 | 15 | 1 | PASS |
| 21.0.5-amzn | 10,364 | 15 | 1 | PASS |
| 8.0.432-amzn | 10,364 | 15 | 1 | PASS |

All runs passed with zero failures/errors. No tests were excluded or waived.

## Regression tests

- DateTimeFormatTest exercises the issue cases through schema validation and the public entry points, including lowercase forms, 10–30 fraction digits, both large-offset signs, -00:00, malformed syntax and year boundaries.
- Leap-second cases include long fractions and ±23:59, offset-driven UTC date/year rollover, and rejection of unrecorded possible leap dates with Ethlo present.
- The child-classloader test blocks com.ethlo.* and validates through a real Draft 7 schema, including long fractions, -00:00, large offsets, space rejection and malformed/leap-date cases.
- Issue575Test previously classified ±23:00 and -00:00 as invalid. Those exact values are now valid coverage; invalid coverage retains ±24:00 and adds -00:60. Valid cases explicitly enable format assertions under Draft 2019-09.

## Broader grammar and UTC probe

253,216 cases passed with zero mismatches on this branch. The probe covered 27 historical-date examples across all 2,879 minute offsets from -23:59 through +23:59, with three fraction forms. It also checked 20,000 generated calendar/time/offset forms, explicit grammar boundaries, four-digit year endpoints crossing UTC year boundaries, and a 100,000-digit fraction.

The grammar/UTC oracle uses a separate RFC-shaped regex and LocalDate/LocalDateTime checks rather than calling TimeFormat or DateFormat. Expected historical leap acceptance uses the unchanged Ethlo leap_second_dates.csv resource. This verifies representation and UTC normalization while preserving the dependency's calendar policy; it is not an independent audit of historical leap data.

An initial comparison against a separate historical-date list exposed existing resource quirks (for example, 1979-12 is absent). This fix does not replace or correct that dependency table. The final probe checks that its existing decisions are preserved across the supported offset/fraction spellings.

## Scope checks and remaining gates

`git diff --check` passed. GitNexus reported LOW as a lower-bound estimate for DateTimeFormat (one direct indexed caller, zero processes, one module) but could not scan the unindexed worktrees. Source review, regression coverage and complete JDK matrices provide local verification. No performance claim is made; remote CI remains a separate gate.

Open #1331 overlaps the separator portion of this implementation. Its branch and PR are untouched; maintainers should reconcile that overlap when merging the complete fix.

## Review follow-up

The changelog now separates breaking validation changes: with Ethlo present, both space separators and offset-less leap-second strings such as `2016-12-31T23:59:60` change from accepted to rejected. Migration guidance requires `T`/`t` and an explicit offset. Two new cases pin offset-less leap-second rejection.

The existing Ethlo table differences are now explicit in compatibility documentation and four regression cases: 1979-12-31 and 1987-12-31 are real insertions rejected by the table, while 1971-12-31 and 1988-12-31 are accepted despite not having insertions. The fallback accepts all four as possible December-end leap seconds, including through actual no-Ethlo class loading. These inherited calendar discrepancies are not fixed by this syntax/offset change.

Shared private validation now parses the date once and computes its UTC leap date once. Ethlo receives the already computed date; it no longer re-substrings or re-parses the original date or reads a sibling class's private constant. The public entry points, historical table policy and optional dependency loading remain unchanged.

The PR titles are narrowed to syntax and offset handling rather than general consistency. The separator fix was originally proposed by [curforever](https://github.com/curforever) in #1331. Preferred maintainer integration is to merge that contribution first and then rebase the broader PRs, preserving original authorship. If maintainers instead combine the work, contributor credit must be preserved explicitly. The PRs reference #1316 without automatic closure. The merge-order/credit decision remains a maintainer action; #1331 has not been modified or closed.

The local JDK matrix and 253,216-case probe were rerun after the cleanup. The probe still uses the unchanged Ethlo resource for historical-calendar expectations and found zero mismatches on both branches.
