# Locale

The error messages have been translated to several languages by contributors, defined in the `jsv-messages.properties` resource
bundle under https://github.com/networknt/json-schema-validator/tree/master/src/main/resources.

## Global Configuration

To use one of the available translations the simplest approach is to set your default locale before running the validation:

```java
// Set the default locale to German (needs only to be set once before using the validator)
Locale.setDefault(Locale.GERMAN);
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
JsonSchema schema = schemaRegistry.getSchema(schemaData);
...
```

Note that the above approach changes the locale for the entire JVM which is probably not what you want to do if you are
using this in an application expected to support multiple languages (for example a localised web application). In this
case you should use the `SchemRegistryConfig` class before loading your schema:

## Per-Request Configuration

```java
// Set the configuration with a specific locale for each request
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
Schema schema = schemaRegistry.getSchema(schemaData);
List<Error> errors = schema.validate(input, InputFormat.JSON, executionContext -> {
    executionContext.executionConfig(executionConfig -> executionConfig.locale(Locale.GERMAN));
});
...
```

The following approach can be used to determine the locale to use on a per user basis using a language tag priority list.

```java
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
Schema schema = schemaRegistry.getSchema(schemaData);

// Uses the fr locale for this user
List<Error> errors = jsonSchema.validate(input, executionContext -> {
    Locale locale = Locales.findSupported("it;q=0.9,fr;q=1.0"); // fr
    executionContext.executionConfig(executionConfig -> executionConfig.locale(locale));
});

// Uses the it locale for this user
errors = jsonSchema.validate(input, executionContext -> {
    Locale locale = Locales.findSupported("it;q=1.0,fr;q=0.9"); // it
    executionContext.executionConfig(executionConfig -> executionConfig.locale(locale));
});
...
```

## Message Source

Besides setting the locale and using the default resource bundle, you may also specify your own to cover any languages you
choose without adapting the library's source, or to override default messages. In doing so you however you should ensure that your resource bundle covers all the keys defined by the default bundle. 

```java
// Set the configuration with a custom message source
MessageSource messageSource = new ResourceBundleMessageSource("my-messages");
SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder().messageSource(messageSource).build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(), builder -> builder.schemaRegistryConfig(schemaRegistryConfig));
JsonSchema schema = schemaRegistry.getSchema(schemaData);
...
```

It is possible to override specific keys from the default resource bundle. Note however that you will need to supply all the languages for that specific key as it will not fallback on the default resource bundle. For instance the jsv-messages-override resource bundle will take precedence when resolving the message key.

```java
// Set the configuration with a custom message source

MessageSource messageSource = new ResourceBundleMessageSource("jsv-messages-override", DefaultMessageSource.BUNDLE_BASE_NAME);
SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder().messageSource(messageSource).build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(), builder -> builder.schemaRegistryConfig(schemaRegistryConfig));
JsonSchema schema = schemaRegistry.getSchema(schemaData);
...
```


