# Customizing Meta-Schemas, Vocabularies, Keywords and Formats

The meta-schemas, vocabularies, keywords and formats can be customized with appropriate configuration of the `JsonSchemaFactory` that is used to create instances of `JsonSchema`.

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
JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012())
        .keyword(new EqualsKeyword())
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
```

## Creating a custom meta-schema

A custom meta-schema can be created by using a standard dialect as a base.

The following creates a custom meta-schema `https://www.example.com/schema` with a custom keyword using the Draft 2020-12 dialect as a base.

```java
JsonMetaSchema dialect = JsonMetaSchema.getV202012();
JsonMetaSchema metaSchema = JsonMetaSchema.builder("https://www.example.com/schema", dialect)
        .keyword(new EqualsKeyword())
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
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
JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012())
        .vocabularyFactory(vocabularyFactory)
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
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

## Unknown keywords

By default unknown keywords are treated as annotations. This can be customized by configuring a `com.networknt.schema.KeywordFactory` on its meta-schema.

The following configuration will cause a `InvalidSchemaException` to be thrown if an unknown keyword is used.

```java
JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012())
        .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance())
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
```

## Creating a custom format

A custom format can be implemented by implementing the `com.networknt.schema.Format` interface.

```java
public class MatchNumberFormat implements Format {
    private final BigDecimal compare;
    
    public MatchNumberFormat(BigDecimal compare) {
        this.compare = compare;
    }
    @Override
    public boolean matches(ExecutionContext executionContext, ValidationContext validationContext, JsonNode value) {
        JsonType nodeType = TypeFactory.getValueNodeType(value, validationContext.getConfig());
        if (nodeType != JsonType.NUMBER && nodeType != JsonType.INTEGER) {
            return true;
        }
        BigDecimal number = value.isBigDecimal() ? value.decimalValue() : BigDecimal.valueOf(value.doubleValue());
        number = new BigDecimal(number.toPlainString());
        return number.compareTo(compare) == 0;
    }
    @Override
    public String getName() {
        return "matchnumber";
    }
}
```

## Adding a format to a standard dialect

A custom format can be added to a standard dialect by customizing its meta-schema which is identified by its IRI.

The following adds a custom format to the Draft 2020-12 dialect.

```java
JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012())
        .format(new MatchNumberFormat(new BigDecimal("12345")))
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
```

## Customizing the format keyword

The format keyword implementation to use can be customized by supplying a `FormatKeywordFactory` to the meta-schema that creates an instance of the subclass of `FormatKeyword`.

```java
JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012())
        .formatKeywordFactory(CustomFormatKeyword::new)
        .build();
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
```

## Unknown formats

By default unknown formats are ignored unless the format assertion vocabulary is used for that meta-schema. Note that the format annotation vocabulary with the configuration to enable format assertions is not equivalent to the format assertion vocabulary.

To ensure that errors are raised when unknown formats are used, the `SchemaValidatorsConfig` can be configured to set `format` as strict.


## Loading meta-schemas

By default meta-schemas that aren't explicitly configured in the `JsonSchemaFactory` will be automatically loaded.

This means that the following `JsonSchemaFactory` will still be able to process `$schema` with other dialects such as Draft 7 or Draft 2019-09 as the meta-schemas for those dialects will be automatically loaded. This will also attempt to load custom meta-schemas with custom vocabularies. Draft 2020-12 will be used by default if `$schema` is not defined.

```java
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
```

If this is undesirable, for instance to restrict the meta-schemas used only to those explicitly configured in the `JsonSchemaFactory` a `com.networknt.schema.JsonMetaSchemaFactory` can be configured.

```java
JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
        builder -> builder.metaSchemaFactory(DisallowUnknownJsonMetaSchemaFactory.getInstance()));
```
