The error messages have been translated to several languages by contributors, defined in the `jsv-messages.properties` resource
bundle under https://github.com/networknt/json-schema-validator/tree/master/src/main/resources. To use one of the
available translations the simplest approach is to set your default locale before running the validation:

```
// Set the default locale to German (needs only to be set once before using the validator)
Locale.setDefault(Locale.GERMAN);
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
JsonSchema schema = factory.getSchema(source);
...
```

Note that the above approach changes the locale for the entire JVM which is probably not what you want to do if you are
using this in an application expected to support multiple languages (for example a localised web application). In this
case you should use the `SchemaValidatorsConfig` class before loading your schema:

```
// Set the configuration with a specific locale (you can create this before each validation)
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.setLocale(myLocale);
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
JsonSchema schema = factory.getSchema(source, config);
...
```

Besides setting the locale and using the default resource bundle, you may also specify your own to cover any languages you
choose without adapting the library's source, or to override default messages. In doing so you however you should ensure that your resource bundle covers all the keys defined by the default bundle. 

```
// Set the configuration with a custom message source
MessageSource messageSource = new ResourceBundleMessageSource("my-messages");
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.setMessageSource(messageSource);
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
JsonSchema schema = factory.getSchema(source, config);
...
```

It is possible to override specific keys from the default resource bundle. Note however that you will need to supply all the languages for that specific key as it will not fallback on the default resource bundle. For instance the jsv-messages-override resource bundle will take precedence when resolving the message key.

```
// Set the configuration with a custom message source
MessageSource messageSource = new ResourceBundleMessageSource("jsv-messages-override", DefaultMessageSource.BUNDLE_BASE_NAME);
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
config.setMessageSource(messageSource);
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
JsonSchema schema = factory.getSchema(source, config);
...
```

The following approach can be used to determine the locale to use on a per user basis using a language tag priority list.

```
SchemaValidatorsConfig config = new SchemaValidatorsConfig();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
JsonSchema schema = factory.getSchema(source, config);

// Uses the fr locale for this user
Locale locale = Locales.findSupported("it;q=0.9,fr;q=1.0");
ExecutionContext executionContext = jsonSchema.createExecutionContext();
executionContext.getExecutionConfig().setLocale(locale);
Set<ValidationMessage> messages = jsonSchema.validate(executionContext, rootNode);

// Uses the it locale for this user
locale = Locales.findSupported("it;q=1.0,fr;q=0.9");
executionContext = jsonSchema.createExecutionContext();
executionContext.getExecutionConfig().setLocale(locale);
messages = jsonSchema.validate(executionContext, rootNode);
...
```
