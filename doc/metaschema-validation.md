If you have an use case to validate custom schemas against the one of the JSON schema draft version, here is the code that you can do it. 

```
  public static final Function<ObjectNode, Set<SchemaValidationMessage>> validateAgainstMetaSchema =
      schema -> {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
         JsonSchema metaSchema = factory.getSchema(getSchemaUri());
        return metaSchema.validate(schema).stream()
            .map((validation) -> new SchemaValidationMessage(validation.getMessage()))
            .collect(Collectors.toSet());
      };

```

This should now work but does not support all the keywords because the JsonMetaSchema of SpecVersion.VersionFlag.V201909 is lacking these features.

You can fix the issue by resolving the vocabularies to a local resource file and re-do the JsonMetaSchema for 2019 based on that. 



