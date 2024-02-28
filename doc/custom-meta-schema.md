# Customizing Meta-Schema and Vocabulary

The meta-schema and vocabularies can be customized with appropriate configuration of the `JsonSchemaFactory` that is used to create instances of `JsonSchema`.

## Creating a custom keyword

A custom keyword can be implemented by implementing the `com.networknt.schema.Keyword` interface.

```java
public class EqualsKeyword implements Keyword {
    @Override
    public String getValue() {
        return "equals";
    }
    @Override
    public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath,
            JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
            throws JsonSchemaException, Exception {
        return new EqualsValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, this, validationContext, false);
    }
}
```

```java
public class EqualsValidator extends BaseJsonValidator {
    private static ErrorMessageType ERROR_MESSAGE_TYPE = new ErrorMessageType() {
        @Override
        public String getErrorCode() {
            return "equals";
        }
    };
    
    private final String value;
    public EqualsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, Keyword keyword,
            ValidationContext validationContext, boolean suppressSubSchemaRetrieval) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ERROR_MESSAGE_TYPE, keyword, validationContext,
                suppressSubSchemaRetrieval);
        this.value = schemaNode.textValue();
    }
    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        if (!node.asText().equals(value)) {
            return Collections
                    .singleton(message().message("{0}: must be equal to ''{1}''")
                            .arguments(value)
                            .instanceLocation(instanceLocation).instanceNode(node).build());
        };
        return Collections.emptySet();
    }
}
```

## Adding a keyword to a standard dialect

A custom keyword can be added to a standard dialect by customizing its meta-schema which is identified by its IRI.

The following adds a custom keyword to the Draft 2020-12 dialect.

```java
JsonMetaSchema dialect = JsonMetaSchema.getV202012();
JsonMetaSchema metaSchema = JsonMetaSchema.builder(dialect.getIri(), dialect)
        .addKeyword(new EqualsKeyword())
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.addMetaSchema(metaSchema));
```

## Creating a custom meta-schema

A custom meta-schema can be created by using a standard dialect as a base.

The following creates a custom meta-schema `https://www.example.com/schema` with a custom keyword using the Draft 2020-12 dialect as a base.

```java
JsonMetaSchema dialect = JsonMetaSchema.getV202012();
JsonMetaSchema metaSchema = JsonMetaSchema.builder("https://www.example.com/schema", dialect)
        .addKeyword(new EqualsKeyword())
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.addMetaSchema(metaSchema));
```

## Associating vocabularies to a dialect

Custom vocabularies can be associated with a particular dialect by configuring a `com.networknt.schema.VocabularyFactory` on its meta-schema.

```java
VocabularyFactory vocabularyFactory = iri -> {
    if ("https://www.example.com/vocab/equals".equals(iri)) {
        return new Vocabulary("https://www.example.com/vocab/equals", new EqualsKeyword());
    }
    return null;
};
JsonMetaSchema dialect = JsonMetaSchema.getV202012();
JsonMetaSchema metaSchema = JsonMetaSchema.builder(dialect.getIri(), dialect)
        .vocabularyFactory(vocabularyFactory)
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.addMetaSchema(metaSchema));
```

The following custom meta-schema `https://www.example.com/schema` will use the custom vocabulary `https://www.example.com/vocab/equals`.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://www.example.com/schema",
  "$vocabulary": {
    "https://www.example.com/vocab/equals": true,
    "https://json-schema.org/draft/2020-12/vocab/applicator": true,
    "https://json-schema.org/draft/2020-12/vocab/core": true
  },
  "allOf": [
    { "$ref": "https://json-schema.org/draft/2020-12/meta/applicator" },
    { "$ref": "https://json-schema.org/draft/2020-12/meta/core" }
  ]
}
```

Note that `"https://www.example.com/vocab/equals": true` means that if the vocabulary is unknown the meta-schema will fail to successfully load while `"https://www.example.com/vocab/equals": false` means that an unknown vocabulary will still successfully load.