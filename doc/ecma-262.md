For the pattern validator, we now have two options for regex in the library. The default one is `java.util.regex`; however, you can use the ECMA-262 standard library `org.jruby.joni` by configuration. 

As we know, the JSON schema is designed based on the Javascript language and its regex. The Java internal implementation has some differences which don't comply with the standard. For most users, these edge cases are not the issue as they are not using them anyway. Even when they are using it, they are expecting the Java regex result as the application is built on the Java platform. For users who want to ensure that they are using 100% standard patter validator, we have provided an option to override the default regex library with `org.jruby.joni` that is complying with the ECMA-262 standard.

### Which one to choose?

If you want a faster regex lib and don't care about the slight difference between Java and Javascript regex, then you don't need to do anything. The default regex lib is the `java.util.regex`.

If you want to ensure full compliance, use the `org.jruby.joni`. It is 1.5 times slower then `java.util.regex`. Depending on your use case, it might not be an issue. 

### How to switch?

Here is the test case that shows how to pass a config object to use the ECMA-262 library.

```java
@Test(expected = JsonSchemaException.class)
public void testInvalidPatternPropertiesValidatorECMA262() throws Exception {
    SchemaValidatorsConfig config = new SchemaValidatorsConfig();
    config.setEcma262Validator(true);
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
    JsonSchema schema = factory.getSchema("{\"patternProperties\":6}", config);

    JsonNode node = getJsonNodeFromStringContent("");
    Set<ValidationMessage> errors = schema.validate(node);
    Assert.assertEquals(errors.size(), 0);
}
```


