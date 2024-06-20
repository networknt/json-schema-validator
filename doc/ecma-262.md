# Regular Expressions

For the `pattern` and `format` `regex` validators there are 3 built in options in the library.

A custom implementation can be made by implementing `com.networknt.schema.regex.RegularExpressionFactory` to return a custom implementation of `com.networknt.schema.regex.RegularExpression`.

| Regular Expression Factory                       | Description                                        |
|--------------------------------------------------|----------------------------------------------------|
| `JDKRegularExpressionFactory`                    | Uses Java's standard `java.util.regex` and calls the `find()` method. Note that `matches()` is not called as that attempts to match the entire string, implicitly adding anchors. This is the default implementation and does not require any additional libraries. |
| `JoniRegularExpressionFactory`                   | Uses `org.joni.Regex` with `Syntax.ECMAScript`. This requires adding the `org.jruby.joni:joni` dependency which will require about 2MB.                                                                                                                             |
| `GraalJSRegularExpressionFactory`                | Uses GraalJS with `new RegExp(pattern, 'u')`. This requires adding the `org.graalvm.js:js` dependency which will require about 50MB.                                                                                                                                |

## Specification

The use of Regular Expressions is specified in JSON Schema at https://json-schema.org/draft/2020-12/json-schema-core#name-regular-expressions.

```
Keywords MAY use regular expressions to express constraints, or constrain the instance value to be a regular expression. These regular expressions SHOULD be valid according to the regular expression dialect described in ECMA-262, section 21.2.1 [ecma262].

Regular expressions SHOULD be built with the "u" flag (or equivalent) to provide Unicode support, or processed in such a way which provides Unicode support as defined by ECMA-262.

Furthermore, given the high disparity in regular expression constructs support, schema authors SHOULD limit themselves to the following regular expression tokens:

individual Unicode characters, as defined by the JSON specification [RFC8259];
simple character classes ([abc]), range character classes ([a-z]);
complemented character classes ([^abc], [^a-z]);
simple quantifiers: "+" (one or more), "*" (zero or more), "?" (zero or one), and their lazy versions ("+?", "*?", "??");
range quantifiers: "{x}" (exactly x occurrences), "{x,y}" (at least x, at most y, occurrences), {x,} (x occurrences or more), and their lazy versions;
the beginning-of-input ("^") and end-of-input ("$") anchors;
simple grouping ("(...)") and alternation ("|").
Finally, implementations MUST NOT take regular expressions to be anchored, neither at the beginning nor at the end. This means, for instance, the pattern "es" matches "expression".
```

## Considerations when selecting implementation

If strict compliance with the regular expression dialect described in ECMA-262 is required. Then only the `GraalJS` implementation meets that criteria.

The `Joni` implementation is configured to attempt to match the ECMA-262 regular expression dialect. However this dialect isn't directly maintained by its maintainers as it doesn't come from its upstream `Oniguruma`. The current implementation has known issues matching inputs with newlines and not respecting `^` and `$` anchors.

The `JDK` implementation is the default and uses `java.util.regex` with the `find()` method.

As the implementations are used when validating regular expressions, using `format` `regex`, one consideration is how the regular expression is used. For instance if the system that consumes the input is implemented in Javascript then the `GraalJS` implementation will ensure that this regular expression will work. If the system that consumes the input is implemented in Java then the `JDK` implementation may be better.

## Configuration of implementation

The following test case shows how to pass a config object to use the `GraalJS` factory.

```java
public class RegularExpressionTest {
    @Test
    public void testInvalidRegexValidatorECMA262() throws Exception {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance())
                .build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchema schema = factory.getSchema("{\r\n"
                + "  \"format\": \"regex\"\r\n"
                + "}", config);
        Set<ValidationMessage> errors = schema.validate("\"\\\\a\"", InputFormat.JSON, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertFalse(errors.isEmpty());
    }
}
```

## Performance

The following is the relative performance of the different implementations.

```
Benchmark                                               Mode  Cnt        Score       Error   Units
RegularExpressionBenchmark.graaljs                     thrpt    6   362696.226 ± 15811.099   ops/s
RegularExpressionBenchmark.graaljs:gc.alloc.rate       thrpt    6     2584.386 ±   112.708  MB/sec
RegularExpressionBenchmark.graaljs:gc.alloc.rate.norm  thrpt    6     7472.003 ±     0.001    B/op
RegularExpressionBenchmark.graaljs:gc.count            thrpt    6      130.000              counts
RegularExpressionBenchmark.graaljs:gc.time             thrpt    6      144.000                  ms
RegularExpressionBenchmark.jdk                         thrpt    6  2776184.321 ± 41838.479   ops/s
RegularExpressionBenchmark.jdk:gc.alloc.rate           thrpt    6     1482.565 ±    22.343  MB/sec
RegularExpressionBenchmark.jdk:gc.alloc.rate.norm      thrpt    6      560.000 ±     0.001    B/op
RegularExpressionBenchmark.jdk:gc.count                thrpt    6       74.000              counts
RegularExpressionBenchmark.jdk:gc.time                 thrpt    6       78.000                  ms
RegularExpressionBenchmark.joni                        thrpt    6  1810229.581 ± 35230.798   ops/s
RegularExpressionBenchmark.joni:gc.alloc.rate          thrpt    6     1463.887 ±    28.483  MB/sec
RegularExpressionBenchmark.joni:gc.alloc.rate.norm     thrpt    6      848.003 ±     0.001    B/op
RegularExpressionBenchmark.joni:gc.count               thrpt    6       73.000              counts
RegularExpressionBenchmark.joni:gc.time                thrpt    6       77.000                  ms
```

