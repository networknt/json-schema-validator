# Jackson upgrade checks

See the [NetworkNT contribution guide](https://doc.networknt.com/contribute/) for
the general contribution workflow.

Re-verify `SelectiveTreeDeserializer` on **every Jackson dependency bump**, even
when the upgrade compiles without source changes. `JsonMapperFactory` and
`YamlMapperFactory` register this wrapper for `JsonNode`, `ObjectNode`, and
`ArrayNode`. It depends on Jackson's implementation-level `JsonNodeDeserializer`
factory and `DelegatingDeserializer` lifecycle, including contextualization,
null/absent handling, updates, and polymorphic type handling. Compilation alone
does not establish compatibility.

Run these commands from the repository root with the candidate Jackson version
set in `pom.xml`:

```sh
mvn -Dgroups=jackson-upgrade test
mvn verify
```

Keep the `jackson-upgrade` tag on these regression tests when renaming or moving
them, and apply it to new Jackson upgrade contracts.

Use the JDK matrix in the target branch's `.github/workflows/ci.yml`: Java
17/21/25 on `master` (Jackson 3), and Java 8/11/17/21 on `2.x` (Jackson 2).
Run each JDK sequentially in a checkout; builds share `target/`. Keep evidence
for the two branches separate. Record the Jackson/JDK versions, commands,
results, and any remaining remote CI gate in the PR. Do not exclude failing
tests to qualify an upgrade.

The `jackson-upgrade` JUnit-tagged suite exercises both public factory singletons
against an unmodified Jackson mapper of the same format. It checks declared tree root types, scalar
roots, null/empty/absent handling, update identity and nested merge behavior,
contextual merge overrides, per-reader number and error features, custom node
factories, decimal scale settings, polymorphic tree properties (including a
native subtype supplied by a custom module), error messages, source locations
and cause chains, and native
scalar/untyped/POJO binding. It deliberately retains Jackson's replacement
behavior for a property declared as generic `JsonNode`, even with `@JsonMerge`;
concrete `ObjectNode` and `ArrayNode` properties support in-place merging.

Lossy finite decimal promotion is the intentional difference from stock tree
reads. Those cases use explicit expected decimals and a stock reader with
`USE_BIG_DECIMAL_FOR_FLOATS` as the reference. Native scalar, `Object.class`, and
POJO binding must not acquire the selective tree policy. Other comparisons
include node classes, serialized shape, decimal scale, and signed-zero bits,
so matching numeric values alone cannot hide a representation change.

If a check fails after an upgrade, inspect the native Jackson behavior and the
wrapper separately. Revisit delegation and its configuration handling before
changing expected results; document any intentional compatibility change.
