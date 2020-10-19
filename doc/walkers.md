### JSON Schema Walkers

There can be use-cases where we need the capability to walk through the given JsonNode allowing functionality beyond validation like collecting information,handling cross cutting concerns like logging or instrumentation. JSON walkers were introduced to complement the validation functionality this library already provides.

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
     * {@link BaseJsonValidator#walk(JsonNode, JsonNode, String, boolean)} provides
     * a default implementation of this method. However keywords that parse
     * sub-schemas should override this method to call walk method on those
     * subschemas.
     * 
     * @param node                 JsonNode
     * @param rootNode             JsonNode
     * @param at                   String
     * @param shouldValidateSchema boolean
     * @return a set of validation messages if shouldValidateSchema is true.
     */
    Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema);
}

```

The JSONValidator interface extends this new interface thus allowing all the validator's defined in library to implement this new interface. BaseJsonValidator class provides a default implementation of the walk method. In this case the walk method does nothing but validating based on shouldValidateSchema parameter.

```java
/**
     * This is default implementation of walk method. Its job is to call the
     * validate method if shouldValidateSchema is enabled.
     */
    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
        if (shouldValidateSchema) {
            validationMessages = validate(node, rootNode, at);
        }
        return validationMessages;
    }
    
```

A new walk method added to the JSONSchema class allows us to walk through the JSONSchema.

```java
 public ValidationResult walk(JsonNode node, boolean shouldValidateSchema) {
        // Create the collector context object.
        CollectorContext collectorContext = new CollectorContext();
        // Set the collector context in thread info, this is unique for every thread.
        ThreadInfo.set(CollectorContext.COLLECTOR_CONTEXT_THREAD_LOCAL_KEY, collectorContext);
        Set<ValidationMessage> errors = walk(node, node, AT_ROOT, shouldValidateSchema);
        // Load all the data from collectors into the context.
        collectorContext.loadCollectors();
        // Collect errors and collector context into validation result.
        ValidationResult validationResult = new ValidationResult(errors, collectorContext);
        return validationResult;
    }
    
    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
        // Walk through all the JSONWalker's.
        for (Entry<String, JsonValidator> entry : validators.entrySet()) {
            JsonWalker jsonWalker = entry.getValue();
            String schemaPathWithKeyword = entry.getKey();
            try {
                // Call all the pre-walk listeners. If all the pre-walk listeners return true
                // then continue to walk method.
                if (keywordWalkListenerRunner.runPreWalkListeners(schemaPathWithKeyword, node, rootNode, at, schemaPath,
                        schemaNode, parentSchema)) {
                    validationMessages.addAll(jsonWalker.walk(node, rootNode, at, shouldValidateSchema));
                }
            } finally {
                // Call all the post-walk listeners.
                keywordWalkListenerRunner.runPostWalkListeners(schemaPathWithKeyword, node, rootNode, at, schemaPath,
                        schemaNode, parentSchema, validationMessages);
            }
        }
        return validationMessages;
    }
```
Following code snippet shows how to call the walk method on a JsonSchema instance.

```
ValidationResult result = jsonSchema.walk(data,false);

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
            JsonNode schemaNode = keywordWalkEvent.getSchemaNode();
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
SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
    schemaValidatorsConfig.addKeywordWalkListener(new AllKeywordListener());
    schemaValidatorsConfig.addKeywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener());
    schemaValidatorsConfig.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(),
            new PropertiesKeywordListener());
final JsonSchemaFactory schemaFactory = JsonSchemaFactory
        .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
        .build();
this.jsonSchema = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig);
                
```

There are two kinds of walk listeners, keyword walk listeners and property walk listeners. Keyword walk listeners will be called whenever the given keyword is encountered while walking the schema and JSON node data, for example we have added Ref and Property keyword walk listeners in the above example. Property walk listeners are called for every property defined in the JSON node data.

Both property walk listeners and keyword walk listener can be modeled by using the same WalkListener interface. Following is an example of how to add a property walk listener.

```java
SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
schemaValidatorsConfig.addPropertyWalkListener(new ExamplePropertyWalkListener());
final JsonSchemaFactory schemaFactory = JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
                .build();
this.jsonSchema = schemaFactory.getSchema(getSchema(), schemaValidatorsConfig);
                
```

### Walk Events

An instance of WalkEvent is passed to both the onWalkStart and onWalkEnd methods of the WalkListeners implementations.

A WalkEvent instance captures several details about the node currently being walked along with the schema of the node, Json path of the node and other details.

Following snippet shows the details captured by WalkEvent instance.

```java
public class WalkEvent {

    private String schemaPath;
    private JsonNode schemaNode;
    private JsonSchema parentSchema;
    private String keyWordName;
    private JsonNode node;
    private JsonNode rootNode;
    private String at;

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