## Walking Schema

Provides a mechanism to traverse the schema which allows collecting information, handling cross cutting concerns like logging or instrumentation, or applying default values. This can be done in conjunction to validating the instance if required.

### Walk Listeners

Developers can define listeners that will be triggered before and after a schema is walked for the following events.

| Event    | Description                                                        |
| -------- | ------------------------------------------------------------------ |
| Keyword  | Triggered before and after each keyword validator is processed.    |
| Property | Triggered before an after each schema for a property is processed. |
| Item     | Triggered before an after each schema for an item is processed.    |

### Walk Event

| Name               | Description                                                                                                                                           |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `executionContext` | Contains details on the current execution. The `collectorContext` on the`executionContext` can be used to store additional details during processing. |
| `schema`           | The schema being processed.                                                                                                                   |
| `keyword`          | The keyword being processed.                                                                                                                  |
| `rootNode`         | The root node of the instance document.                                                                                                               |
| `instanceNode`     | The instance node being processed.                                                                                                            |
| `instanceLocation` | The location of the current instance node being processed.                                                                                            |
| `validator`        | The keyword validator being processed.                                                                                                                |
| `evaluationPath`   | The evaluation path.                                                                                                                          |

### Example

The following example shows how to register a `WalkListener` triggered for keywords using the `WalkConfig`.

```java
String schemaData = "{\r\n"
        + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
        + "  \"type\": \"object\",\r\n"
        + "  \"description\": \"Default Description\",\r\n"
        + "  \"properties\": {\r\n"
        + "    \"tags\": {\r\n"
        + "      \"type\": \"array\",\r\n"
        + "      \"items\": {\r\n"
        + "        \"$ref\": \"#/definitions/tag\"\r\n"
        + "      }\r\n"
        + "    }\r\n"
        + "  },\r\n"
        + "  \"definitions\": {\r\n"
        + "    \"tag\": {\r\n"
        + "      \"properties\": {\r\n"
        + "        \"name\": {\r\n"
        + "          \"type\": \"string\"\r\n"
        + "        },\r\n"
        + "        \"description\": {\r\n"
        + "          \"type\": \"string\"\r\n"
        + "        }\r\n"
        + "      }\r\n"
        + "    }\r\n"
        + "  }\r\n"
        + "}";

KeywordWalkHandler keywordWalkHandler = KeywordWalkHandler.builder()
        .keywordWalkListener(KeywordType.PROPERTIES.getValue(), new WalkListener() {
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                List<WalkEvent> propertyKeywords = walkEvent.getExecutionContext()
                        .getCollectorContext()
                        .computeIfAbsent("propertyKeywords", key -> new ArrayList<>());
                propertyKeywords.add(walkEvent);
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
            }
        })
        .build();
Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7).getSchema(schemaData);
String inputData = "{\r\n"
        + "  \"tags\": [\r\n"
        + "    {\r\n"
        + "      \"name\": \"image\",\r\n"
        + "      \"description\": \"An image\"\r\n"
        + "    },\r\n"
        + "    {\r\n"
        + "      \"name\": \"link\",\r\n"
        + "      \"description\": \"A link\"\r\n"
        + "    }\r\n"
        + "  ]\r\n"
        + "}";
Result result = schema.walk(inputData, InputFormat.JSON, true, executionContext -> executionContext
        .walkConfig(walkConfig -> walkConfig.keywordWalkHandler(keywordWalkHandler)));
assertTrue(result.getErrors().isEmpty());
List<WalkEvent> propertyKeywords = result.getCollectorContext().get("propertyKeywords");
assertEquals(3, propertyKeywords.size());
assertEquals("properties", propertyKeywords.get(0).getValidator().getKeyword());
assertEquals("", propertyKeywords.get(0).getInstanceLocation().toString());
assertEquals("/properties",
        propertyKeywords.get(0).getEvaluationPath().append(propertyKeywords.get(0).getKeyword()).toString());
assertEquals("/tags/0", propertyKeywords.get(1).getInstanceLocation().toString());
assertEquals("image", propertyKeywords.get(1).getInstanceNode().get("name").asText());
assertEquals("/properties/tags/items/$ref", propertyKeywords.get(1).getEvaluationPath().toString());
assertEquals("/properties/tags/items/$ref/properties",
        propertyKeywords.get(1).getEvaluationPath().append(propertyKeywords.get(1).getKeyword()).toString());
assertEquals("/tags/1", propertyKeywords.get(2).getInstanceLocation().toString());
assertEquals("/properties/tags/items/$ref/properties",
        propertyKeywords.get(2).getEvaluationPath().append(propertyKeywords.get(2).getKeyword()).toString());
assertEquals("link", propertyKeywords.get(2).getInstanceNode().get("name").asText());
```

### Applying Defaults

In some use cases we may want to apply defaults while walking the schema. To accomplish this, create an `ApplyDefaultsStrategy` when creating a `WalkConfig`. The input object is changed in place, even if validation fails, or a fail-fast or some other exception is thrown.

Here is the order of operations in walker.

* Apply defaults
* Run listeners
* Validate if `shouldValidateSchema` is true

```java
String schemaData = "{\r\n"
        + "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\r\n"
        + "  \"title\": \"Schema with default values \",\r\n"
        + "  \"type\": \"object\",\r\n"
        + "  \"properties\": {\r\n"
        + "    \"intValue\": {\r\n"
        + "      \"type\": \"integer\",\r\n"
        + "      \"default\": 15, \r\n"
        + "      \"minimum\": 20\r\n"
        + "    }\r\n"
        + "  },\r\n"
        + "  \"required\": [\"intValue\"]\r\n"
        + "}";
        
String inputData = "{}";
        
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft4());
Schema schema =  schemaRegistry.getSchema(schemaData);

JsonNode inputNode = JsonMapperFactory.getInstance().readTree(inputData);
Result result = schema.walk(inputNode, true, executionContext -> executionContext.walkConfig(
        walkConfig -> walkConfig.applyDefaultsStrategy(applyDefaultsStrategy -> applyDefaultsStrategy
                .applyArrayDefaults(true).applyPropertyDefaults(true).applyPropertyDefaultsIfNull(true))));
assertFalse(result.getErrors().isEmpty());
assertEquals("{\"intValue\":15}", inputNode.toString());
```