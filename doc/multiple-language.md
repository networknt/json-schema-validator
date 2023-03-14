The error messages have been translated to several languages by contributors. Here is the code to use German.

```
Locale.setDefault(Locale.GERMAN);
JsonSchemaFactory jsonFactoryInstance = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(jsonFactoryInstance).build();
```

To add other languages, we need to add properties file under this folder. 

https://github.com/networknt/json-schema-validator/tree/master/src/main/resources

