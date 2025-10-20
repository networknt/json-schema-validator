### JSON Schema Walkers

There can be use-cases where we need the capability to walk through the given JsonNode allowing functionality beyond validation like collecting information,handling cross cutting concerns like logging or instrumentation, or applying default values. JSON walkers were introduced to complement the validation functionality this library already provides.

Currently, walking is defined at the validator instance level for all the built-in keywords.

### Walk methods

A new interface is introduced into the library that a Walker should implement. It should be noted that this interface also allows the validation based on shouldValidateSchema parameter.

```java
public interface JsonSchemaWalker {
    /**
     * 
     * This method gives the capability to walk through the given JsonNode, allowing
     * functionality beyond validation like collecting information,handling cross
     * cutting concerns like logging or instrumentation. This method also performs
     * the validation if {@code shouldValidateSchema} is set to true. <br>
     * <br>
     * {@link BaseJsonValidator#walk(ExecutionContext, JsonNode, JsonNode, JsonNodePath, boolean)} provides
     * a default implementation of this method. However validators that parse
     * sub-schemas should override this method to call walk method on those
     * sub-schemas.
     * 
     * @param executionContext     ExecutionContext
     * @param node                 JsonNode
     * @param rootNode             JsonNode
     * @param instanceLocation     JsonNodePath
     * @param shouldValidateSchema boolean
     * @return a set of validation messages if shouldValidateSchema is true.
     */
    Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema);
}

```

The JSONValidator interface extends this new interface thus allowing all the validator's defined in library to implement this new interface. BaseJsonValidator class provides a default implementation of the walk method. In this case the walk method does nothing but validating based on shouldValidateSchema parameter.

```java
    /**
     * This is default implementation of walk method. Its job is to call the
     * validate method if shouldValidateSchema is enabled.
     */
    @Override
    default Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        return shouldValidateSchema ? validate(executionContext, node, rootNode, instanceLocation)
                : Collections.emptySet();
    }
```

A new walk method added to the JSONSchema class allows us to walk through the JSONSchema.

```java
    public ValidationResult walk(JsonNode node, boolean validate) {
        return walk(createExecutionContext(), node, validate);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        // Walk through all the JSONWalker's.
        for (JsonValidator validator : getValidators()) {
            JsonNodePath evaluationPathWithKeyword = validator.getEvaluationPath();
            try {
                // Call all the pre-walk listeners. If at least one of the pre walk listeners
                // returns SKIP, then skip the walk.
                if (this.validationContext.getConfig().getKeywordWalkListenerRunner().runPreWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1), node, rootNode, instanceLocation,
                        this, validator)) {
                    Set<ValidationMessage> results = null;
                    try {
                        results = validator.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
                    } finally {
                        if (results != null && !results.isEmpty()) {
                            errors.addAll(results);
                        }
                    }
                }
            } finally {
                // Call all the post-walk listeners.
                this.validationContext.getConfig().getKeywordWalkListenerRunner().runPostWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1), node, rootNode, instanceLocation,
                        this, validator, errors);
            }
        }
        return errors;
    }
```
Following code snippet shows how to call the walk method on a JsonSchema instance.

```java
ValidationResult result = jsonSchema.walk(data, false);

```

walk method can be overridden for select validator's based on the use-case. Currently, walk method has been overridden in PropertiesValidator,ItemsValidator,AllOfValidator,NotValidator,PatternValidator,RefValidator,AdditionalPropertiesValidator to accommodate the walk logic of the enclosed schema's.

### Walk Listeners 

Walk listeners allows to execute a custom logic before and after the invocation of a JsonWalker walk method. Walk listeners can be modeled by a WalkListener interface.

```java
public interface JsonSchemaWalkListener {

	public WalkFlow onWalkStart(WalkEvent walkEvent);

	public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages);
}
```

Following is the example of a sample WalkListener implementation.

```java
private static class PropertiesKeywordListener implements JsonSchemaWalkListener {

        @Override
        public WalkFlow onWalkStart(WalkEvent keywordWalkEvent) {
            JsonNode schemaNode = keywordWalkEvent.getSchema().getSchemaNode();
            if (schemaNode.get("title").textValue().equals("Property3")) {
                return WalkFlow.SKIP;
            }
            return WalkFlow.CONTINUE;
        }

        @Override
        public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {

        }
    }
```
If the onWalkStart method returns WalkFlow.SKIP, the actual walk method execution will be skipped.

Walk listeners can be added by using the SchemaValidatorsConfig class.

```java
SchemaValidatorsConfig.Builder schemaValidatorsConfig = SchemaValidatorsConfig.builder();
    schemaValidatorsConfig.keywordWalkListener(new AllKeywordListener());
    schemaValidatorsConfig.keywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener());
    schemaValidatorsConfig.keywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(),
            new PropertiesKeywordListener());
final JsonSchemaFactory schemaFactory = JsonSchemaFactory
        .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).metaSchema(metaSchema)
        .build();
this.jsonSchema = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig.build());
                
```

There are two kinds of walk listeners, keyword walk listeners and property walk listeners. Keyword walk listeners will be called whenever the given keyword is encountered while walking the schema and JSON node data, for example we have added Ref and Property keyword walk listeners in the above example. Property walk listeners are called for every property defined in the JSON node data.

Both property walk listeners and keyword walk listener can be modeled by using the same WalkListener interface. Following is an example of how to add a property walk listener.

```java
SchemaValidatorsConfig.Builder schemaValidatorsConfig = SchemaValidatorsConfig.builder();
schemaValidatorsConfig.propertyWalkListener(new ExamplePropertyWalkListener());
final JsonSchemaFactory schemaFactory = JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).metaSchema(metaSchema)
                .build();
this.jsonSchema = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig.build());
                
```

### Walk Events

An instance of WalkEvent is passed to both the onWalkStart and onWalkEnd methods of the WalkListeners implementations.

A WalkEvent instance captures several details about the node currently being walked along with the schema of the node, Json path of the node and other details.

Following snippet shows the details captured by WalkEvent instance.

```java
public class WalkEvent {
    private ExecutionContext executionContext;
    private JsonSchema schema;
    private String keyword;
    private JsonNode rootNode;
    private JsonNode instanceNode;
    private JsonNodePath instanceLocation;
    private JsonValidator validator;
    ...
}
```

### Sample Flow

Given an example schema as shown, if we write a property listener, the walk flow is as depicted in the image.

```json
{
 
    "title": "Sample Schema",
    "definitions" : {
      "address" :{
       "street-address": {
            "title": "Street Address",
            "type": "string"
        },
        "pincode": {
            "title": "Body",
            "type": "integer"
        }
      }
    },
    "properties": {
        "name": {
            "title": "Title",
            "type": "string",
            "maxLength": 50
        },
        "body": {
            "title": "Body",
            "type": "string"
        },
        "address": {
            "title": "Excerpt",
            "$ref": "#/definitions/address"
        }
       
    },
    "additionalProperties": false
}
```

![img](walk_flow.png)<!-- .element height="50%" width="50%" -->


Few important points to note about the flow.

1. onWalkStart and onWalkEnd are the methods defined in the property walk listener
2. Anywhere during the flow, onWalkStart can return a WalkFlow.SKIP to stop the walk method execution of a particular "property schema".
3. onWalkEnd will be called even if the onWalkStart returns a WalkFlow.SKIP.
4. Walking a property will check if the keywords defined in the "property schema" has any keyword listeners, and they will be called in the defined order. 
   For example in the above schema when we walk through the "name" property if there are any keyword listeners defined for "type" or "maxlength" , they will be invoked in the defined order.
5. Since we have a property listener defined, When we are walking through a property that has a "$ref" keyword which might have some more properties defined, 
   Our property listener would be invoked for each of the property defined in the "$ref" schema. 
6. As mentioned earlier anywhere during the "Walk Flow", we can return a  WalkFlow.SKIP from onWalkStart method to stop the walk method of a particular "property schema" from being called. 
   Since the walk method will not be called any property or keyword listeners in the "property schema" will not be invoked.


### Applying defaults

In some use cases we may want to apply defaults while walking the schema.
To accomplish this, create an ApplyDefaultsStrategy when creating a SchemaValidatorsConfig.
The input object is changed in place, even if validation fails, or a fail-fast or some other exception is thrown.

Here is the order of operations in walker.
1. apply defaults
1. run listeners
1. validate if shouldValidateSchema is true

Suppose the JSON schema is
```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "Schema with default values ",
  "type": "object",
  "properties": {
    "intValue": {
      "type": "integer",
      "default": 15, 
      "minimum": 20
    }
  },
  "required": ["intValue"]
}
```

A JSON file like
```json
{
}
```

would normally fail validation as "intValue" is required.
But if we apply defaults while walking, then required validation passes, and the object is changed in place.

```java
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        SchemaValidatorsConfig.Builder schemaValidatorsConfig = SchemaValidatorsConfig.builder();
        schemaValidatorsConfig.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true));
        JsonSchema jsonSchema =  schemaFactory.getSchema(SchemaLocation.of("classpath:schema.json"), schemaValidatorsConfig.build());

        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data.json"));
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages(), Matchers.empty());
        assertEquals("{\"intValue\":15}", inputNode.toString());
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("$.intValue: must have a minimum value of 20."));
```
