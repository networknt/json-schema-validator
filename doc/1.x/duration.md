## Validating RFC 3339 durations

JSON Schema Draft 2019-09 and later uses RFC 3339 to define dates and times.
RFC 3339 bases its definition of duration of what is in the 1988 version of
ISO 1801, which is over 35 years old and has undergone many changes with
updates in 1991, 2000, 2004, 2019 and an amendment in 2022.

There are notable differences between the current version of ISO 8601 and
RFC 3339:
* ISO 8601-2:2019 permits negative durations</li>
* ISO 8601-2:2019 permits combining weeks with other terms (e.g. `P1Y13W`)

There are also notable differences in how RFC 3339 defines a duration compared
with how the Java Date/Time API defines it:
* `java.time.Duration` accepts fractional seconds; RFC 3339 does not
* `java.time.Period` does not accept a time component while RFC 3339 accepts both date and time components
* `java.time.Duration` accepts days but not years, months or weeks

By default, the duration validator performs a strict check that the value
conforms to RFC 3339. You can relax this constraint by setting strict to false.

```java
SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().strict("duration", false).build();
JsonSchema jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schema, config);
```

The relaxed check permits:
* Fractional seconds
* Negative durations
* Combining weeks with other terms
