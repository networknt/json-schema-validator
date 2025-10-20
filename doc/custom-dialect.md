# Customizing Dialects, Vocabularies, Keywords and Formats

The dialects, vocabularies, keywords and formats can be customized with appropriate configuration of the `SchemaRegistry` that is used to create instances of `Schema`.

## Creating a custom keyword

A custom keyword can be implemented by implementing the `com.networknt.schema.keyword.Keyword` interface.

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
public class EqualsValidator extends BaseKeywordValidator {
    private final String value;

    EqualsValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, Keyword keyword,
            SchemaContext schemaContext) {
        super(keyword, schemaNode, schemaLocation, parentSchema, schemaContext);
        this.value = schemaNode.textValue();
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        if (!node.asText().equals(value)) {
            executionContext.addError(error().message("must be equal to ''{0}''")
                            .arguments(value)
                            .instanceLocation(instanceLocation).instanceNode(node).evaluationPath(executionContext.getEvaluationPath()).build());
        }
    }
}
```

## Adding a keyword to a standard dialect

A custom keyword can be added to a standard dialect by customizing its dialect which is identified by its id.

The following adds a custom keyword to the Draft 2020-12 dialect.

```java
Dialect dialect = Dialect.builder(Dialects.getDraft202012())
        .keyword(new EqualsKeyword())
        .build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
```

## Creating a custom dialect

A custom dialect can be created by using a standard dialect as a base.

The following creates a custom dialect `https://www.example.com/schema` with a custom keyword using the Draft 2020-12 dialect as a base.

```java
Dialect dialect = Dialect.builder("https://www.example.com/schema", Dialects.getDraft202012())
        .keyword(new EqualsKeyword())
        .build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
```

## Associating vocabularies to a dialect

Custom vocabularies can be associated with a particular dialect by configuring a `com.networknt.schema.vocabulary.VocabularyRegistry` on its dialect.

```java
VocabularyRegistry vocabularyRegistry = id -> {
    if ("https://www.example.com/vocab/equals".equals(id)) {
        return new Vocabulary("https://www.example.com/vocab/equals", new EqualsKeyword());
    }
    return null;
};
Dialect dialect = Dialect.builder(Dialects.getDraft202012())
        .vocabularyRegistry(vocabularyRegistry)
        .build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
```

The following custom meta-schema for the dialect `https://www.example.com/schema` will use the custom vocabulary `https://www.example.com/vocab/equals`.

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

Note that `"https://www.example.com/vocab/equals": true` means that if the vocabulary is unknown the meta-schema for the dialect will fail to successfully load while `"https://www.example.com/vocab/equals": false` means that an unknown vocabulary will still successfully load.

## Unknown keywords

By default unknown keywords are treated as annotations. This can be customized by configuring a `com.networknt.schema.keyword.KeywordFactory` on its dialect.

The following configuration will cause a `InvalidSchemaException` to be thrown if an unknown keyword is used.

```java
Dialect dialect = Dialect.builder(Dialects.getDraft202012())
        .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance())
        .build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
```

## Creating a custom format

A custom format can be implemented by implementing the `com.networknt.schema.format.Format` interface.

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

A custom format can be added to a standard dialect by customizing its dialect which is identified by its id.

The following adds a custom format to the Draft 2020-12 dialect.

```java
Dialect dialect = Dialect.builder(Dialects.getDraft202012())
        .format(new MatchNumberFormat(new BigDecimal("12345")))
        .build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
```

## Customizing the format keyword

The format keyword implementation to use can be customized by supplying a `FormatKeywordFactory` to the dialect that creates an instance of the subclass of `FormatKeyword`.

```java
Dialect dialect = Dialect.builder(Dialects.getDraft202012())
        .formatKeywordFactory(CustomFormatKeyword::new)
        .build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
```

## Unknown formats

By default unknown formats are ignored unless the format assertion vocabulary is used for that dialect. Note that the format annotation vocabulary with the configuration to enable format assertions is not equivalent to the format assertion vocabulary.

To ensure that errors are raised when unknown formats are used, the `SchemaRegistryConfig` can be configured to set `format` as strict.


## Loading dialects

Creating the `SchemaRegistry` with `withDefaultDialect` will create one that accepts all other standard dialects such as Draft 7 or Draft 2019-09 as the meta-schemas for those dialects will be automatically loaded. This will also attempt to load custom meta-schemas with custom vocabularies.

```java
SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
```

As Draft 2020-12 was specified, it will be used by default if `$schema` is not defined.

If this is undesirable, creating the `SchemaRegistry` with `withDialect` will create one that only accepts that particular dialect.

```java
SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getDraft202012());
```
