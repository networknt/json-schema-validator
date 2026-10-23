# Issue #1362: decimal tree parser overhead

The precision wrapper now invalidates its cached decision when advancing, including
optimized advancement and `skipChildren()`. It no longer allocates token locations.
Short fixed-point tokens with at most 15 digits bypass decimal/string conversion;
the restricted length keeps them away from binary64 underflow and overflow.
Other tokens compare the parser text buffer with the shortest double spelling
before allocating a token String or BigDecimal.

Jackson token counts were evaluated and rejected: they are approximate and can be
unavailable unless stream constraints enable counting. No counter assumption is
required by this implementation.

## Local validation

Full `mvn test` on Corretto 17 passed, including unchanged Issue1357Test,
Issue1350Test and Issue1350ReviewTest. New tests exercise cache invalidation via
optimized field-name, scalar-value and subtree-skipping methods.
The existing external decimal oracle passed unchanged: 4,215 tokens, 13,590
parsed cases, three equality/hash shapes, zero mismatches on each branch.
The new JMH source compiles on both branches; the master benchmark jar packages
and all three methods pass a short nonforked smoke run. The 2.x focused
Issue1362/1357/1350/1350Review suite also passes on JDK 8 (46 cases).
The initial 2.x full run on JDK 25 hit the existing GraalJS Unsafe compatibility
failure; JDK 17 passed without exclusions.
GitNexus still indexes the older CountParser implementation, so its low-risk
result is stale and is not qualification evidence for this change.

## Measurement

Corretto 25.0.3, 1 GiB fixed heap, eight warmups and best of eight reads.
Native means an unmodified Jackson JsonMapper/YAMLMapper; selective means
NodeReader.readTree using the default policy. These are tree-read timings,
not end-to-end Schema.validate timings or a before/after comparison.
JSON uses 200,000 elements. YAML uses 20,000 to stay below its default
3,145,728-code-point limit for every payload. Seed: 1362.
This lightweight harness is indicative; the checked-in JMH benchmark supplies
forked measurements and also covers the end-to-end validation path.

| Format | Payload | Elements | Native ms | Selective ms | Ratio |
|---|---|---:|---:|---:|---:|
| JSON | integer | 200000 | 3.738 | 3.652 | 0.977 |
| JSON | plain-float | 200000 | 8.456 | 9.470 | 1.120 |
| JSON | machine-double | 200000 | 34.726 | 47.570 | 1.370 |
| JSON | 19-digit | 200000 | 60.127 | 101.694 | 1.691 |
| JSON | 40-digit | 200000 | 66.664 | 141.989 | 2.130 |
| YAML | integer | 20000 | 4.454 | 4.739 | 1.064 |
| YAML | plain-float | 20000 | 8.661 | 9.228 | 1.066 |
| YAML | machine-double | 20000 | 14.895 | 16.571 | 1.113 |
| YAML | 19-digit | 20000 | 15.428 | 20.214 | 1.310 |
| YAML | 40-digit | 20000 | 18.766 | 26.256 | 1.399 |

Plain and machine-double reads meet the roughly 1.5x target for JSON and YAML
in this measurement. The master branch still has an unmet JSON machine-double
target, so the overall issue should remain open.

## Repeat with JMH

```sh
mvn -Pbenchmark -DskipTests package
java -Xms1g -Xmx1g -jar target/benchmarks.jar DecimalTreeBenchmark -p format=JSON -p count=200000
java -Xms1g -Xmx1g -jar target/benchmarks.jar DecimalTreeBenchmark -p format=YAML -p count=20000
```

Run on one idle machine/JDK at a time. Compare `nativeTree` and `selectiveTree`
for the same parameters. `validate` uses a permissive schema to isolate the
input parsing contribution in the validation path.
