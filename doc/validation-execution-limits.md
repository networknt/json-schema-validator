# Design: Validation execution limits

Status: implemented for `master`; the `2.x` backport requires separate review and verification. The sections below record the design contract and its scope.

Target: `master`, followed by a separately verified `2.x` backport.

Issue: [#1276](https://github.com/networknt/json-schema-validator/issues/1276). The documentation and characterization tests were added in [#1287](https://github.com/networknt/json-schema-validator/pull/1287) and [#1288](https://github.com/networknt/json-schema-validator/pull/1288). This design was checked against `master` at `51dab24`.

## Problem and evidence

Overlapping recursive alternatives can evaluate the same schema and instance locations through exponentially many evaluation paths. The `anyOf` reproduction in #1276 produces the following internal error counts on both evaluated branch heads:

| Nested arrays | Instance bytes | Internal errors |
| --- | --- | --- |
| 8 | 19 | 512 |
| 12 | 27 | 8,192 |
| 16 | 35 | 131,072 |

There are only two distinct schema-location/instance-location pairs for these errors. The bounded investigation confirmed growth, not the reporter's OOM threshold or the Kestra example. The merged [Issue1276Test](../src/test/java/com/networknt/schema/Issue1276Test.java) contains 36 cases at depths 0, 4, and 8. It counts leaf failures as they occur for recursive `anyOf` and `oneOf`, three output formats, and fail-fast on/off.

Existing configuration does not bound this behavior:

- `failFast` is temporarily disabled while alternatives are evaluated. A rejected candidate does not establish the result of the applicator.
- Boolean and flag output still incur internal validation and error allocation. `oneOf` can discard child diagnostics before returning without avoiding the work that produced them.
- `cacheRefs` caches referenced schemas, not evaluation results.
- Disabling optional annotation reporting does not bound evaluation and does not disable annotations required by `unevaluatedItems` or `unevaluatedProperties`.

The security model trusts schemas, but a trusted schema can contain this structure while processing untrusted instances. Small input byte limits and a larger heap do not solve the problem.

## Decision and scope

First introduce **opt-in execution-step and evaluation-depth limits**, enforced independently of fail-fast and output format. Exhaustion throws a dedicated exception indicating that validation did not complete. Develop memoization separately after containment is available.

This phase must:

1. Stop recursive expansion after a deterministic amount of instrumented evaluation work.
2. Stop excessive active schema recursion before descending into another evaluation frame.
3. Share one budget across an entire execution, including alternatives and references.
4. Preserve existing validation results and diagnostics when no limit is reached.
5. Apply to validation and schema walking, including walking without assertions.
6. Preserve current behavior by default and support the Java/Jackson versions of each release line.

This phase does **not** provide a wall-clock timeout, a heap quota, a complete count of primitive operations, schema-compilation limits, input-parser limits, error truncation, or a memoization cache. In particular, one keyword can scan a large collection, allocate many annotations, run an expensive regex, resolve a remote reference, or invoke application code. A dispatch budget cannot interrupt that work. Applications still need parser/input limits, bounded retrieval and regex behavior, and process isolation where hard resource guarantees are required.

## Configuration

Add the following immutable properties to `ExecutionConfig`, its builder, and its copy builder:

| Property | Type | Default | Meaning |
| --- | --- | --- | --- |
| `maxEvaluationSteps` | `long` | `0` | Maximum admitted schema-entry and keyword-dispatch steps; zero means unlimited. |
| `maxEvaluationDepth` | `int` | `0` | Maximum simultaneously active schema evaluation frames, including the root; zero means unlimited. |

Negative values are configuration errors and must fail at build time. Limits are application policy, not schema keywords, and must not be configurable by an untrusted schema. No universal finite default is proposed: valid workload sizes and recursion patterns vary, and changing the default would reject workloads that currently complete.

Use the existing registry execution-context customizer for application defaults rather than duplicating properties in `SchemaRegistryConfig`. Existing per-call customizers can override them before execution begins. Preserve the existing ordering: registry context creation, output-format customization where applicable, then per-call customization. Output-format implementations must preserve these properties when copying configuration.

Registry defaults can be configured with the following API:

```java
SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
        SpecificationVersion.DRAFT_2020_12,
        builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder()
                .executionContextCustomizer((context, schemaContext) ->
                        context.executionConfig(config -> config
                                .maxEvaluationSteps(10_000)
                                .maxEvaluationDepth(64)))
                .build()));
```

Per-call overrides are applied before evaluation starts:

```java
try {
    schema.validate(input, InputFormat.JSON, OutputFormat.BOOLEAN,
            context -> context.executionConfig(config -> config
                    .maxEvaluationSteps(10_000).maxEvaluationDepth(64)));
} catch (ValidationLimitExceededException e) {
    // No validity verdict was produced. Apply your application's reject,
    // quarantine, or retry policy; use a fresh context for any retry.
}
```

The exception exposes `getLimitKind()`, `getLimit()`, `getAdmittedSteps()`,
`getActiveDepth()`, `getSchemaLocation()`, `getInstanceLocation()`, and
`getEvaluationPath()`. `LimitKind` is the nested enum in
`ValidationLimitExceededException`. Locations describe the denied attempt;
the bounded exception message does not render the paths or retain input trees.
An exhausted caller-supplied context remains terminal even if its configuration
is changed or an extension catches the original exception.

The numbers above illustrate syntax; they are not safe defaults or a capacity recommendation. Evaluation depth includes schema wrappers and `$ref` traversal and is not equal to JSON nesting depth. Operators should calibrate limits against representative valid and invalid workloads.

Snapshot the effective limits at the first schema-entry boundary after customization. Subsequent configuration changes inside callbacks must not replenish or disable an active budget. Application customizers remain trusted policy code; these options are not a security boundary against malicious application extensions.

Preserve existing protected constructor signatures by delegation to new overloads with unlimited defaults. Copy builders and all existing convenience APIs must retain their prior behavior. Do not introduce Java APIs unavailable to `2.x`.

## Accounting contract

A step is one of the following admissions:

| Boundary | Charge |
| --- | --- |
| Enter `Schema.validate(context, node, rootNode, location)` | One schema-entry step. |
| Enter `Schema.walk(context, node, rootNode, location, validate)` | One schema-entry step, even when `validate` is false. |
| Dispatch one keyword validator from either loop | One keyword-dispatch step. |

Do not also charge convenience overloads that delegate to those boundaries. A reference keyword dispatch and entry into its target schema are separate steps. Empty and boolean schemas still incur entry steps. Revisiting a schema/instance pair through a different path is charged again. Siblings consume the same monotonically decreasing allowance; completion of a branch does not refund steps.

For walking, charge the keyword step **before** invoking its pre-walk listener. A listener that skips the keyword still consumes that dispatch attempt. Post-walk callbacks for an admitted keyword follow existing lifecycle rules; a denied dispatch does not start that keyword's listener lifecycle. A new schema evaluation invoked by a listener using the active context shares its budget.

Admission rules:

1. At schema entry, check the proposed active depth before incrementing it; the root has depth 1. Check depth before steps so simultaneous exhaustion has a deterministic reason.
2. Check step capacity before loading validators for a new schema entry, or before invoking an admitted keyword's listener/validator.
3. A limit of `N` allows exactly `N` steps. Attempt `N + 1` throws before its work or path-stack mutation begins. A rejected attempt is not counted as completed/admitted work.
4. Increment active depth only after admission and decrement it in `finally` on every exit.
5. Use overflow-safe comparisons. Do not increment a bounded counter past its limit; any diagnostic counter for an unlimited budget must saturate rather than wrap.

These are counts of dispatch boundaries, not estimated CPU instructions or error objects. Their exact totals may change when evaluation internals change; tests should establish boundary semantics with small controlled fixtures rather than promise stable totals for arbitrary schemas across releases.

Repeated recursion in #1276 necessarily crosses these boundaries, so the step limit contains that expansion. A depth limit additionally addresses very deep, narrow recursion and recursive references that do not descend through the instance. It is not a proof against `StackOverflowError` for an arbitrarily high configured limit or arbitrary custom validator code.

## Execution ownership and lifecycle

Store mutable accounting in a private per-execution state owned by `ExecutionContext`, never in a cached `Schema`, registry, static field, or thread-local cache. A schema can be used concurrently with separate contexts; an individual context remains confined to one execution at a time.

Use an outermost-entry guard shared by validation and walking. It creates the state when no execution is active, snapshots configuration, and tracks nested admissions. It must cover low-level public entry points as well as formatted/convenience APIs. Nested `$ref`, dynamic reference, applicator, and listener calls using that context join the active execution; they must not reset counters. Root-path initialization must not accidentally establish a new budget during reentrant calls.

On normal completion or ordinary fail-fast unwinding, release active accounting in `finally`. A subsequent independent call gets a fresh budget. This does not add a new promise that callers may retain old errors/annotations and reuse a context without following existing lifecycle requirements.

On limit exhaustion, mark the execution aborted before throwing. Further admissions on that active state must throw even if a custom validator catches the first exception. Before formatting a result, check the aborted state again: swallowed exhaustion must never yield a success or failure verdict. Low-level outermost exits need the same check. Make an exhausted caller-supplied context terminal for evaluation; callers must create a new context for retry. This avoids treating partially accumulated annotations, discriminator state, or errors as a complete reusable result.

Custom callbacks that create a different context start a different execution. The budget cannot control arbitrary application code or force independently created contexts to share accounting; document this extension boundary.

## Failure contract

Introduce `ValidationLimitExceededException extends RuntimeException`, independent of `FailFastAssertionException` and the ordinary assertion `Error` model. It must propagate unchanged through all built-in applicators and output modes.

Proposed immutable diagnostic fields:

| Field | Meaning |
| --- | --- |
| `limitKind` | `EVALUATION_STEPS` or `EVALUATION_DEPTH`. |
| `limit` | Configured capacity for that kind. |
| `admittedSteps` | Steps admitted before refusal; saturating if counting is unbounded. |
| `activeDepth` | Frames already active at refusal. |
| `schemaLocation`, `instanceLocation`, `evaluationPath` | Location of the denied attempt, captured without admitting it. |

The attempted depth is `activeDepth + 1` for a schema-entry denial. Locations identify where evaluation stopped, not an invalid instance assertion. Do not attach the instance tree, schema tree, complete error list, or annotation collection to the exception. Use a bounded message; avoid eagerly formatting potentially large path strings into it. Normal exception stack traces may be retained for this exceptional condition.

Observable behavior is uniform:

- Default/list/hierarchical output does not return a partial diagnostic result.
- Boolean output does not return `true` or `false` for an incomplete evaluation.
- Flag output does not return a validity flag for an incomplete evaluation.
- Walk APIs do not return a completed result after exhaustion.
- `not`, `if`, `anyOf`, `oneOf`, and `contains` must not reinterpret exhaustion as a candidate failure, invert it, or continue trying alternatives.

Callers decide whether to reject, quarantine, or retry with a different policy. The library must not turn an operational limit into schema invalidity. An application that catches broad `RuntimeException` still needs to distinguish this outcome explicitly.

## Exception-safe unwinding

The current traversal has `finally` blocks for path stacks, evaluation schemas, and unevaluated flags. Some applicators, notably `AnyOfValidator` and `OneOfValidator`, restore their branch-local error list only on ordinary return paths. An execution-limit exception can exit before that restoration.

As part of implementation, audit every temporary error-list replacement, fail-fast override, path push, annotation scope, discriminator scope, and walk callback. Restore structural state in `finally` without merging unfinished branch errors into the parent. Preserve existing success/failure merge behavior for completed evaluations. Audit `not`, `if`/`then`/`else`, `contains`, both reference mechanisms, and all walk variants, not only the reproduction's call path.

Already produced errors and annotations are partial and must not be published as a result. Marking the context terminal avoids requiring rollback of every annotation and application callback side effect. Accounting/path restoration is still required to avoid retaining nested frames and branch-local lists unnecessarily.

Existing walk post-listeners may execute during unwinding and may inspect error slices. Ensure those slices remain valid after branch restoration. If a post-listener throws during a limit abort, retain the limit exception as the primary outcome and attach the callback failure as suppressed; do not mask exhaustion. Callbacks can still block or perform expensive work, so this cleanup rule is not a wall-clock guarantee.

## Implementation locations

| Area | Planned responsibility |
| --- | --- |
| [ExecutionConfig](../src/main/java/com/networknt/schema/ExecutionConfig.java) | Immutable options, validation, copy-builder and constructor compatibility. |
| [ExecutionContext](../src/main/java/com/networknt/schema/ExecutionContext.java) | Limit snapshot, counters, shared lifecycle, abort marker, terminal-context guard. |
| [Schema](../src/main/java/com/networknt/schema/Schema.java) | Schema/keyword admission, validation/walk integration, pre-format abort checks and unwinding. |
| New `ValidationLimitExceededException` | Distinct incomplete-execution outcome and bounded metadata. |
| Applicators and walk handling | Restore temporary state and propagate aborts without changing completed evaluation semantics. |
| Output formats | Preserve configuration, never convert exhaustion into a validity result. |

The unlimited path should avoid per-step allocations and unnecessary path construction. No cache or shared mutable state is needed. Run impact analysis before editing implementation symbols; this document is not an assertion that changes to these central classes have a small blast radius.

## Validation and acceptance

Use deterministic assertions and bounded fixtures; do not require heap exhaustion or use elapsed-time thresholds as unit-test correctness criteria.

| Test group | Required evidence |
| --- | --- |
| Configuration | Zero/unset means unlimited; negative values rejected; copy builders retain limits; legacy constructor behavior preserved. |
| Exact boundaries | Hand-counted empty/boolean and simple keyword schemas; exactly `N` steps allowed; next attempt rejected before work; root depth 1; depth precedence and overflow safety. |
| Recursive alternatives | Extend #1276 fixtures for both applicators: low limits abort, generous limits preserve validity/diagnostics, no branch reset, and no work continues after refusal. |
| Output and fail-fast | Default, Boolean, flag, list, hierarchical, both fail-fast settings; all propagate the distinct exception; output formatting is not invoked after abort. |
| Composition | `allOf`, `not`, conditionals, `contains`, static/dynamic/recursive references where supported; exhaustion never acts as an ordinary assertion. |
| Annotations | `unevaluatedItems`/`unevaluatedProperties`, optional annotation collection, and branch validity remain correct on completed evaluations. |
| Traversal | Validate and all walk entry points, including walk without validation, skipped keywords, pre/post-listeners, and a nested call using the same context. |
| Lifecycle | No resets inside alternatives; terminal context after exhaustion; fresh context succeeds; completed executions get fresh accounting without changing existing context semantics. |
| Cleanup | Inject refusal at several nested boundaries; path/depth/error-list/fail-fast restoration; post-listener failures cannot mask the limit exception. |
| Extension behavior | A custom validator swallowing the exception cannot produce a formatted verdict; separate contexts are independent. |
| Concurrency | Shared schema, separate contexts and different limits do not share counters or abort state. |
| Compatibility | Full Maven verification and existing schema-suite expectations with limits unset on each branch's supported JDK matrix. |

Keep the existing unlimited characterization cases as baseline evidence until a later optimization intentionally changes them. Add bounded-limit assertions alongside them; do not rewrite the current expected exponential counts as evidence that containment eliminates repeated work.

Measure overhead separately with representative shallow valid schemas, invalid schemas, reference-heavy schemas, and annotation-heavy schemas. Compare unlimited and enabled-but-not-exhausted runs after warmup. Record throughput/allocation results for review; do not claim a universal performance threshold from a single machine. A release decision should consider the unlimited-path overhead as well as bounded recursive behavior.

## Why memoization is separate

Resource limits bound an execution's admitted expansion; they do not make an expensive validation complete successfully. Memoization may later avoid repeated evaluation, but needs its own semantic design:

- A schema/instance pair alone omits dynamic reference scope and possibly execution configuration relevant to validity.
- Annotation effects used by `unevaluated*` must be preserved, not just a Boolean verdict.
- Evaluation-path-specific diagnostics need rebasing or a shared representation. Expanding every diagnostic path can remain exponential even with cached validity.
- Walk listeners, collectors, discriminator handling, and custom validators can have observable effects that cannot simply be skipped.
- Mutable input trees and in-progress recursive evaluations complicate identity and cycle handling. An in-progress cache entry must not be treated as valid.
- Cache lifetime and size must be bounded per execution; a global cache could retain application data or grow without limit.

A follow-up investigation can start with validity-only evaluation for a precisely defined subset and fall back when those conditions do not hold. It must prove correctness before expanding coverage. Cache hits should still consume defined admission steps so enabled limits remain meaningful. Neither a blanket pair cache nor deduplicating the final error list is an acceptable substitute for this containment phase.

## Delivery plan

1. Review this design, especially accounting units, unlimited defaults, depth semantics, exception type, and terminal-context behavior. No runtime change is authorized or implemented by adding this document.
2. Implement containment on `master` with focused lifecycle/semantic tests and unlimited-mode regression coverage. Include exception-safe cleanup in the same deliverable; do not ship a counter without propagation and cleanup guarantees.
3. Run full verification, review overhead, and document the actual API and application handling. Update the issue with the supported guarantee and remaining limitations.
4. Backport the reviewed implementation to `2.x`, preserving its Java 8/Jackson 2 compatibility and verifying its supported JDK matrix independently. Do not change the backport's CI configuration as an incidental part of this work.
5. Track memoization, bounded diagnostic representation, and instrumentation within expensive keyword loops as separate follow-up work. Reassess whether #1276 can close only after the agreed containment scope is delivered; documentation alone and opt-in limits alone must not be described as eliminating exponential validation.

Open review decisions are the public option/exception names and whether a future release should offer a named finite-limit preset. The initial proposal is unlimited defaults with explicit application configuration, no automatic retry, and no change to schema validity semantics.
