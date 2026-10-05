# Validation execution limits

Implementation: [PR #1289](https://github.com/networknt/json-schema-validator/pull/1289) targets `master`. The `2.x` backport requires separate review and verification.

Issue: [#1276](https://github.com/networknt/json-schema-validator/issues/1276). The original documentation and characterization tests were added in [#1287](https://github.com/networknt/json-schema-validator/pull/1287) and [#1288](https://github.com/networknt/json-schema-validator/pull/1288).

## Problem and scope

Overlapping recursive alternatives can evaluate the same schema and instance locations through exponentially many paths. The issue's recursive `anyOf` fixture produced these internal error counts on the investigated branch heads:

| Nested arrays | Instance bytes | Internal errors |
| --- | --- | --- |
| 8 | 19 | 512 |
| 12 | 27 | 8,192 |
| 16 | 35 | 131,072 |

There are only two distinct schema-location/instance-location pairs for these errors. The bounded investigation established growth, rather than the reporter's OOM threshold or the Kestra example. [Issue1276Test](../src/test/java/com/networknt/schema/Issue1276Test.java) preserves the 36 unlimited baseline cases at depths 0, 4, and 8 alongside bounded-limit tests.

`failFast` is suspended while alternatives are evaluated. Boolean and flag output can discard diagnostics after their allocation. `cacheRefs` caches schemas rather than evaluation results. Disabling optional annotations does not disable annotations required by `unevaluatedItems` or `unevaluatedProperties`. None of these settings bounds recursive expansion.

Execution-step and evaluation-depth limits contain this expansion when enabled. Exhaustion means validation did not complete; it is not an assertion that the instance is invalid. Limits apply to validation and schema walking, including walking without assertions. Completed evaluations retain their existing validity, diagnostics, and annotations.

These limits do not provide a wall-clock timeout, heap quota, primitive-operation count, schema-compilation or parser limit, error truncation, or memoization. One keyword can scan a large collection, allocate annotations, execute an expensive regex, resolve a remote reference, or invoke application code. Dispatch accounting cannot interrupt that work. Applications still need appropriate parser/input limits, bounded retrieval and regex behavior, and process isolation where hard resource guarantees are required.

## Configuration

`ExecutionConfig`, its builder, and its copy builder support:

| Property | Type | Default | Meaning |
| --- | --- | --- | --- |
| `maxEvaluationSteps` | `long` | `0` | Maximum admitted schema-entry and keyword-dispatch steps; zero means unlimited. |
| `maxEvaluationDepth` | `int` | `0` | Maximum simultaneously active schema evaluation frames, including the root; zero means unlimited. |

Negative values are rejected when configuration is built. These are application policy options, not schema keywords. There is no universal finite default: valid workload sizes and recursion patterns vary.

Use the registry execution-context customizer for application defaults:

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

Per-call overrides run before evaluation starts:

```java
try {
    schema.validate(input, InputFormat.JSON, OutputFormat.BOOLEAN,
            context -> context.executionConfig(config -> config
                    .maxEvaluationSteps(10_000).maxEvaluationDepth(64)));
} catch (ValidationLimitExceededException e) {
    // No validity verdict was produced. Apply the application's reject,
    // quarantine, or retry policy. Use a fresh context for any retry.
}
```

The example values illustrate syntax; they are not safe defaults or capacity recommendations. Depth includes schema wrappers and references, rather than just JSON nesting. Calibrate against representative valid and invalid workloads.

Effective limits are snapshotted at the first schema entry after registry, output-format (where applicable), and per-call customization. Output-format configuration copies retain the limits. Changes inside callbacks cannot replenish, disable, or enable the active budget. If execution starts with both limits unset, nested calls stay unlimited; a later independent call uses the new policy.

### Subclass builders

The existing seven-argument protected constructor retains unlimited behavior. Its arguments contain no limit values, so an overriding subclass builder that still calls it cannot preserve inherited limit settings automatically.

Subclass builders supporting these options must use the overload accepting both limits or the protected constructor accepting `BuilderSupport<?>`. The latter snapshots and validates all inherited options:

```java
class CustomExecutionConfig extends ExecutionConfig {
    CustomExecutionConfig(CustomBuilder builder) {
        super(builder);
    }
}

class CustomBuilder extends ExecutionConfig.BuilderSupport<CustomBuilder> {
    @Override
    protected CustomBuilder self() {
        return this;
    }

    @Override
    public CustomExecutionConfig build() {
        return new CustomExecutionConfig(this);
    }
}
```

An existing overridden `build()` must be migrated explicitly; adding inherited setters does not change code in an external override. The library builder and copy builder preserve both limits.

## Accounting contract

| Admission boundary | Charge |
| --- | --- |
| Enter `Schema.validate(context, node, rootNode, location)` | One schema-entry step. |
| Enter `Schema.walk(context, node, rootNode, location, validate)` | One schema-entry step, even when `validate` is false. |
| Dispatch one keyword from either loop | One keyword-dispatch step. |

Convenience overloads do not incur another charge. A reference keyword and entry into its target schema are separate admissions. Empty and boolean schemas incur entry steps; boolean-schema validators also incur keyword steps. Revisiting a schema/instance pair is charged again. Siblings share the same monotonically consumed allowance; returning from a branch does not refund steps.

For walking, dispatch is charged before its pre-walk listener. Skipping the keyword still consumes that attempt. A denied dispatch starts neither its pre- nor post-walk listener lifecycle.

At schema entry, depth is checked before steps; the root has depth 1. The proposed frame is admitted before loading its validators or changing schema/path stacks. A capacity of `N` allows exactly `N` steps; attempt `N + 1` throws before that attempt's evaluation work. Rejected attempts do not count as admitted work. Active depth is decremented on every exit.

Bounded step counters never increment past their limit. With only a depth limit enabled, diagnostic step counts saturate at `Long.MAX_VALUE`. If both limits are zero, no accounting state is allocated and no step/depth counters or keyword-admission calls run. Validation and walking retain one method frame per schema entry.

Step totals describe dispatch boundaries, not CPU instructions or allocated errors. Internal changes may change totals; applications should not assume release-stable counts for arbitrary schemas. Very high configured depth limits and custom code can still overflow the Java stack.

## Ownership, reentrancy, and failure

Mutable accounting belongs to `ExecutionContext`, not a cached schema, registry, static field, or thread-local. A shared schema can run concurrently with separate contexts; each context is confined to one execution at a time.

Nested references, applicators, and listeners using the same context share an active limited execution. During that execution, reentrant convenience calls retain the caller's evaluation path rather than resetting it. Unlimited convenience calls retain their legacy root-path initialization. A callback creating a different context starts an independent execution.

Normal completion and ordinary fail-fast unwinding release accounting so a subsequent independent call gets a fresh budget. This does not change existing requirements for managing retained errors and annotations when reusing a context.

Limit exhaustion marks the context aborted before throwing `ValidationLimitExceededException`. It is independent of `FailFastAssertionException` and assertion `Error` objects. All built-in applicators and output formats propagate it unchanged. Alternatives, conditionals, `not`, and `contains` do not reinterpret exhaustion as a candidate failure or continue evaluating other branches.

Further admissions, outermost exits, and pre-format checks reject an aborted execution even if application code swallowed the exception. An exhausted caller-supplied context is terminal: retries require a new context.

Public `getErrors()` and `getAnnotations()` throw the original exception after abort, including during unwinding. Public `isEvaluationAborted()` and `getEvaluationAbort()` remain available for handling the incomplete outcome. Previously obtained errors/annotations are partial and must be discarded; the library cannot revoke references already handed to application code or undo callback side effects.

### Walk listener cleanup

Post-walk callbacks for admitted keywords retain their cleanup lifecycle while an abort unwinds. Listeners must check `event.getExecutionContext().isEvaluationAborted()` before interpreting their diagnostic slice. During an abort the supplied slice is empty because partial diagnostics are not a verdict; `getEvaluationAbort()` provides the reason and denied locations.

Completed walks retain their existing diagnostic-slice semantics. The schema captures the original error-list identity and slice start. Applicators restore temporary branch lists in `finally`; an extension that fails to restore its replacement list is rejected instead of silently clamping the slice offset onto a different list. Unfinished branch errors are not merged into their parent.

If a post-walk callback fails during a limit abort, the limit exception remains primary and the callback failure is suppressed. Callbacks can still block or perform expensive work; cleanup is not a wall-clock guarantee.

## Exception diagnostics

| Getter | Meaning |
| --- | --- |
| `getLimitKind()` | Nested `LimitKind.EVALUATION_STEPS` or `EVALUATION_DEPTH`. |
| `getLimit()` | Configured capacity for that kind. |
| `getAdmittedSteps()` | Steps admitted before refusal; saturating when unbounded. |
| `getActiveDepth()` | Frames already active at refusal. A denied schema entry would add one. |
| `getSchemaLocation()`, `getInstanceLocation()`, `getEvaluationPath()` | Locations of the denied attempt. |

Locations identify where execution stopped, not an invalid-instance assertion. The exception retains no schema tree, instance tree, error list, or annotation collection. Its bounded message does not eagerly render path strings. Java serialization uses a metadata proxy to preserve locations, path types and segments, stack traces, cause, and suppressed failures without making cached schema/path objects serializable.

## Validation and remaining work

[ExecutionLimitsTest](../src/test/java/com/networknt/schema/ExecutionLimitsTest.java) and [Issue1276Test](../src/test/java/com/networknt/schema/Issue1276Test.java) cover configuration, exact boundaries, depth precedence, overflow, recursive alternatives, output/fail-fast behavior, static/dynamic/recursive references, annotations, walk listeners, reentrancy, terminal contexts, cleanup, swallowed exceptions, serialization, and independent concurrent contexts. A separate JVM with a 1 MB stack exercises deep unlimited validation.

Compatibility verification runs full Maven checks on master's supported JDK 17, 21, and 25 matrix with limits unset in the existing schema suite.

Memoization remains separate: a schema/instance pair omits dynamic scope, annotation effects, evaluation-path diagnostics, listener effects, mutable inputs, and in-progress recursion. Any future cache needs its own semantics, execution lifetime, and size bound. Containment does not make an expensive validation complete successfully or eliminate its exponential growth. The `2.x` backport and instrumentation within expensive keyword loops also require separate work.
