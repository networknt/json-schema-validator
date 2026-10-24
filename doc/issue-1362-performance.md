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

### Historical standalone-harness results

The table below comes from the separate best-of-eight timing harness used for
#1362; that harness is not checked into this repository. It does **not** come
from the checked-in `DecimalTreeBenchmark` JMH benchmark. These are historical
results from before the #1368 character-buffer reuse change.

Corretto 25.0.3, 1 GiB fixed heap, eight warmups and best of eight reads.
Native means an unmodified Jackson JsonMapper/YAMLMapper; selective means
NodeReader.readTree using the default policy. These are tree-read timings,
not end-to-end Schema.validate timings or a before/after comparison.
JSON uses 200,000 elements. YAML uses 20,000 to stay below its default
3,145,728-code-point limit for every payload. Seed: 1362.
`DecimalTreeBenchmark` defaults to **20,000 elements for both formats**. The
standalone harness used **200,000 for JSON and 20,000 for YAML**. JMH uses
forked average-time measurements, while the table uses best-of-eight timings,
so its output is not a reproduction of this table. JMH also covers the
end-to-end validation path; that path is not measured in the table.

| Format | Payload | Elements | Native ms | Selective ms | Ratio |
|---|---|---:|---:|---:|---:|
| JSON | integer | 200000 | 3.153 | 3.006 | 0.954 |
| JSON | plain-float | 200000 | 5.230 | 6.400 | 1.224 |
| JSON | machine-double | 200000 | 8.677 | 19.873 | 2.290 |
| JSON | 19-digit | 200000 | 10.517 | 40.812 | 3.881 |
| JSON | 40-digit | 200000 | 14.528 | 61.938 | 4.263 |
| YAML | integer | 20000 | 3.428 | 3.308 | 0.965 |
| YAML | plain-float | 20000 | 7.311 | 7.612 | 1.041 |
| YAML | machine-double | 20000 | 10.092 | 12.011 | 1.190 |
| YAML | 19-digit | 20000 | 9.643 | 14.709 | 1.525 |
| YAML | 40-digit | 20000 | 11.586 | 18.178 | 1.569 |

**Remaining acceptance gap:** JSON machine doubles are about 2.3x native tree
reads on master, exceeding the requested roughly 1.5x. Plain JSON floats and
plain/machine YAML floats meet the target in this measurement. The remaining
cost includes shortest-double formatting; precision has not been weakened to
meet a timing threshold. Issue #1362 should remain open pending that work.

## Run the checked-in JMH benchmark

These commands measure the current checkout. The JSON command overrides the
20,000-element default to match the historical JSON payload size; the YAML
command retains 20,000 elements. The measurement protocols remain different.

```sh
mvn -Pbenchmark -DskipTests package
java -Xms1g -Xmx1g -jar target/benchmarks.jar DecimalTreeBenchmark -p format=JSON -p count=200000
java -Xms1g -Xmx1g -jar target/benchmarks.jar DecimalTreeBenchmark -p format=YAML -p count=20000
```

Run on one idle machine/JDK at a time. Compare `nativeTree` and `selectiveTree`
for the same parameters. `validate` uses a permissive schema to isolate the
input parsing contribution in the validation path.
