# Obtaining YAML Line Numbers

## Scenario 1 - finding YAML line numbers from the JSON tree

A great feature of json-schema-validator is it's ability to validate YAML documents against a JSON Scheme. The manner in which this is done though, by pre-processing the YAML into a tree of [JsonNode](https://fasterxml.github.io/jackson-databind/javadoc/2.10/com/fasterxml/jackson/databind/JsonNode.html) objects, breaks the connection back to the original YAML source file. Very commonly, once the YAML has been validated against the schema, there may be additional processing and checking for semantic or content errors or inconsistency in the JSON tree. From an end user point of view, the ideal is to report such errors using line and column references back to the original YAML, but this information is not readily available from the processed JSON tree. 

### Scenario 1, solution part 1 - capturing line details during initial parsing

One solution is to use a custom [JsonNodeFactory](https://fasterxml.github.io/jackson-databind/javadoc/2.10/com/fasterxml/jackson/databind/node/JsonNodeFactory.html) that returns custom JsonNode objects which are created during initial parsing, and which record the original YAML locations that were being parsed at the time they were created. The example below shows this

```java
    public static class MyNodeFactory extends JsonNodeFactory
    {
        YAMLParser yp;
        
        public MyNodeFactory(YAMLParser yp) 
        {
            super();
            this.yp = yp;
        }
        
        public ArrayNode arrayNode()
        {
            return new MyArrayNode(this, yp.getTokenLocation(), yp.getCurrentLocation());            
        }
        
        public BooleanNode booleanNode(boolean v)
        {
            return new MyBooleanNode(v, yp.getTokenLocation(), yp.getCurrentLocation());            
        }
        
        public NumericNode numberNode(int v)
        {
            return new MyIntNode(v, yp.getTokenLocation(), yp.getCurrentLocation());            
        }
        
        public NullNode nullNode()
        {
            return new MyNullNode(yp.getTokenLocation(), yp.getCurrentLocation());            
        }
        
        public ObjectNode objectNode()                
        {
            return new MyObjectNode(this, yp.getTokenLocation(), yp.getCurrentLocation());            
        }
        
        public TextNode textNode(String text)
        {
            return (text != null) ? new MyTextNode(text, yp.getTokenLocation(), yp.getCurrentLocation()) : null;            
        }        
    }
```

The example above includes a basic, but usable subset of all possible JsonNode types - if your YAML needs them, than you should also consider the others i.e. `byte`, `byte[]`, `raw`, `short`, `long`, `float`, `double`, `BigInteger`, `BigDecimal`

There are some important other things to note from the example:

* Even in a reduced set, `ObjectNode` and `NullNode` should be included
* The current return for methods that receive a null parameter value seems to be null rather than `NullNode` (based on inspecting the underlying `valueOf()` methods in the various `JsonNode` sub classes). Hence the implementation of the `textNode()` method above.

The actual work here is really being done by the YAMLParser - it holds the location of the token being parsed, and the current location in the file. The first of these gives us a line and column number we can use to flag where an error or problem was found, and the second (if needed) can let us calculate a span to the end of the error e.g. if we wanted to highlight or underline the text in error.

### Scenario 1, solution part 2 - augmented `JsonNode` subclassess

We can be as simple or fancy as we like in the `JsonNode` subclassses, but basically we need 2 pieces of information from them:

* An interface so when we are post processing the JSON tree, we can recognize nodes that retain line number information
* An interface that lets us extract the relevant location information

Those could be the same thing of course, but in our case we separated them as shown in the following example

```java
    public interface LocationProvider
    {
        LocationDetails getLocationDetails();
    }

    public interface LocationDetails
    {
        default int getLineNumber()     { return 1; }
        default int getColumnNumber()   { return 1; }
        default String getFilename()    { return ""; }
    }    

    public static class LocationDetailsImpl implements LocationDetails
    {
        final JsonLocation currentLocation;
        final JsonLocation tokenLocation;
        
        public LocationDetailsImpl(JsonLocation tokenLocation, JsonLocation currentLocation) 
        {
            this.tokenLocation = tokenLocation;
            this.currentLocation = currentLocation;
        }        
        
        @Override
        public int getLineNumber()      { return (tokenLocation != null) ? tokenLocation.getLineNr() : 1; };
        @Override
        public int getColumnNumber()    { return (tokenLocation != null) ? tokenLocation.getColumnNr() : 1; };
        @Override
        public String getFilename()     { return (tokenLocation != null) ? tokenLocation.getSourceRef().toString() : ""; };        
    }    
    
    public static class MyNullNode extends NullNode implements LocationProvider
    {        
        final LocationDetails locDetails;
        
        public MyNullNode(JsonLocation tokenLocation, JsonLocation currentLocation) 
        {
            super();
            locDetails = new LocationDetailsImpl(tokenLocation, currentLocation);
        }        

        @Override
        public LocationDetails getLocationDetails() 
        {
            return locDetails;
        }
    }
    
    public static class MyTextNode extends TextNode implements LocationProvider
    {        
        final LocationDetails locDetails;
        
        public MyTextNode(String v, JsonLocation tokenLocation, JsonLocation currentLocation) 
        {
            super(v);
            locDetails = new LocationDetailsImpl(tokenLocation, currentLocation);
        }        

        @Override
        public LocationDetails getLocationDetails()     { return locDetails;}
    }

    public static class MyIntNode extends IntNode implements LocationProvider
    {        
        final LocationDetails locDetails;
        
        public MyIntNode(int v, JsonLocation tokenLocation, JsonLocation currentLocation) 
        {
            super(v);
            locDetails = new LocationDetailsImpl(tokenLocation, currentLocation);
        }        

        @Override
        public LocationDetails getLocationDetails()     { return locDetails;}
    }
    
    public static class MyBooleanNode extends BooleanNode implements LocationProvider
    {        
        final LocationDetails locDetails;
        
        public MyBooleanNode(boolean v, JsonLocation tokenLocation, JsonLocation currentLocation) 
        {
            super(v);
            locDetails = new LocationDetailsImpl(tokenLocation, currentLocation);
        }        

        @Override
        public LocationDetails getLocationDetails()     { return locDetails;}
    }

    public static class MyArrayNode extends ArrayNode implements LocationProvider
    {        
        final LocationDetails locDetails;
        
        public MyArrayNode(JsonNodeFactory nc, JsonLocation tokenLocation, JsonLocation currentLocation) 
        {
            super(nc);
            locDetails = new LocationDetailsImpl(tokenLocation, currentLocation);
        }        

        @Override
        public LocationDetails getLocationDetails()     { return locDetails;}
    }
    
    public static class MyObjectNode extends ObjectNode implements LocationProvider
    {        
        final LocationDetails locDetails;
        
        public MyObjectNode(JsonNodeFactory nc, JsonLocation tokenLocation, JsonLocation currentLocation) 
        {
            super(nc);
            locDetails = new LocationDetailsImpl(tokenLocation, currentLocation);
        }        

        @Override
        public LocationDetails getLocationDetails()     { return locDetails;}
    }
```

### Scenario 1, solution part 3 - using the custom `JsonNodeFactory`

With the pieces we now have, we just need to tell the YAML library to make of use them, which involves a minor and simple modification to the normal sequence of processing.

```java
    this.yamlFactory = new YAMLFactory();

    try (YAMLParser yp = yamlFactory.createParser(f);)
    {    
        ObjectReader rdr = mapper.reader(new MyNodeFactory(yp));
        JsonNode jsonNode = rdr.readTree(yp);
        Set<ValidationMessage> msgs = mySchema.validate(jsonNode);

        if (msgs.isEmpty())
        {
            for (JsonNode item : jsonNode.get("someItem"))
            {
                processJsonItems(item);
            }
        }
        else
        {
            //  ... we'll look at how to get line locations for ValidationMessage cases in Scenario 2
        }

    }
    // a JsonProcessingException seems to be the base exception for "gross" errors e.g.
    // missing quotes at end of string etc. 
    catch (JsonProcessingException jpEx)
    {
        JsonLocation loc = jpEx.getLocation();
        // ... do something with the loc details
    }
```
Some notes on what is happening here:

* We instantiate our custom JsonNodeFactory with the YAMLParser reference, and the line locations get recorded for us as the file is parsed.
* If any exceptions are thrown, they will already contain a JsonLocation object that we can use directly if needed
* If we get no validation messages, we know the JSON tree matches the schema and we can do any post processing we need on the tree. We'll see how to report any issues with this in the next part
* We'll look at how to get line locations for ValidationMessage errors in Scenario 2

### Scenario 1, solution part 4 - extracting the line details

Having got everything prepared, actually getting the line locations is rather easy


```java
    void processJsonItems(JsonNode item) 
    {        
        Iterator<Map.Entry<String, JsonNode>> iter = item.fields();
        
        while (iter.hasNext())
        {
            Map.Entry<String, JsonNode> node = iter.next();            
            extractErrorLocation(node.getValue());        
        }
    }

    void extractErrorLocation(JsonNode node) 
    {
        if (node == null || !(node instanceof LocationProvider))    { return; }

        //Note: we also know the "span" of the error section i.e. from token location to current location (first char after the token)                
        //      if we wanted at some stage we could use this to highlight/underline all of the text in error        
        LocationDetails dets = ((LocationProvider) node).getLocationDetails();
        // ... do something with the details e.g. report an error/issue against the YAML line
    }
```

So that's pretty much it - as we are processing the JSON tree, if there is any point we want to report something about the contents, we can do so with a reference back to the original YAML line number.

There is still a problem though, what if the validation against the schema fails?

## Scenario 2 - ValidationMessage line locations

Any failures validation against the schema come back in the form of a set of `ValidationMessage` objects. But these also do not contain original YAML source line information, and there's no easy way to inject it as we did for Scenario 1. Luckily though, there is a trick we can use here!

Within the `ValidationMessage` object is something called the 'path' of the error, which we can access with the `getPath()` method. The syntax of this path by default is close to being [JSONPath](https://datatracker.ietf.org/doc/draft-ietf-jsonpath-base/), but can be set explicitly to be
either [JSONPath](https://datatracker.ietf.org/doc/draft-ietf-jsonpath-base/) or [JSONPointer](https://www.rfc-editor.org/rfc/rfc6901.html) expressions. In our case as we already use [Jackson](https://github.com/FasterXML/jackson) which supports node lookups based on JSONPointer expressions,
we will set the path expressions to be JSONPointers. This is achieved by configuring the reported path type through the `SchemaValidatorsConfig` before we read our schema:

```java
    SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
    JsonSchema jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schema, config);
```

Having set paths to be JSONPointer expressions we can use those pointers for locating the appropriate `JsonNode` instances. The following couple of methods illustrate this process:

```java
    JsonNode findJsonNode(ValidationMessage msg, JsonNode rootNode)
    {
        // Construct the JSONPointer.
        JsonPointer pathPtr = JsonPointer.valueOf(msg.getPath());
        // Now see if we can find the node.
        JsonNode node = rootNode.at(pathPtr);
        return node;
    }

    LocationDetails getLocationDetails(ValidationMessage msg, JsonNode rootNode)
    {
        LocationDetails retval = null;
        JsonNode node = findJsonNode(msg, rootNode);
        if (node != null && node instanceof LocationProvider)
        {
            retval = ((LocationProvider) node).getLocationDetails();
        }        
        return retval;
    }
```

## Summary

Although not trivial, the steps outlined here give us a way to track back to the original source YAML for a variety of possible reporting cases:

* JSON processing exceptions (mostly already done for us)
* Issues flagged during validation of the YAML against the schema
* Anything we need to report with source information during post processing of the validated JSON tree
