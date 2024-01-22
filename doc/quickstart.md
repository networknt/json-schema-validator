## Quick Start

To use the validator, we need to have both the `JsonSchema` object and `JsonNode` object constructed. 
There are many ways to do that. 
Here is base test class, that shows several ways to construct these from `String`, `Stream`, `Url`, and `JsonNode`. 
Please pay attention to the `JsonSchemaFactory` class as it is the way to construct the `JsonSchema` object.

```java
public class BaseJsonSchemaValidatorTest {

    private ObjectMapper mapper = new ObjectMapper();

    protected JsonNode getJsonNodeFromClasspath(String name) throws IOException {
        InputStream is1 = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);
        return mapper.readTree(is1);
    }

    protected JsonNode getJsonNodeFromStringContent(String content) throws IOException {
        return mapper.readTree(content);
    }

    protected JsonNode getJsonNodeFromUrl(String url) throws IOException {
        return mapper.readTree(new URL(url));
    }

    protected JsonSchema getJsonSchemaFromClasspath(String name) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);
        return factory.getSchema(is);
    }

    protected JsonSchema getJsonSchemaFromStringContent(String schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        return factory.getSchema(schemaContent);
    }

    protected JsonSchema getJsonSchemaFromUrl(String uri) throws URISyntaxException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        return factory.getSchema(SchemaLocation.of(uri));
    }

    protected JsonSchema getJsonSchemaFromJsonNode(JsonNode jsonNode) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        return factory.getSchema(jsonNode);
    }

    // Automatically detect version for given JsonNode
    protected JsonSchema getJsonSchemaFromJsonNodeAutomaticVersion(JsonNode jsonNode) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(jsonNode));
        return factory.getSchema(jsonNode);
    }

}
```
And the following is one of the test cases in one of the test classes that extend from the above base class. As you can see, it constructs `JsonSchema` and `JsonNode` from `String`.

```java
class Sample extends BaseJsonSchemaValidatorTest {

    void test() {    
        JsonSchema schema = getJsonSchemaFromStringContent("{\"enum\":[1, 2, 3, 4],\"enumErrorCode\":\"Not in the list\"}");
        JsonNode node = getJsonNodeFromStringContent("7");
        Set<ValidationMessage> errors = schema.validate(node);
        assertThat(errors.size(), is(1));
    
        // With automatic version detection
        JsonNode schemaNode = getJsonNodeFromStringContent(
            "{\"$schema\": \"http://json-schema.org/draft-06/schema#\", \"properties\": { \"id\": {\"type\": \"number\"}}}");
        JsonSchema schema = getJsonSchemaFromJsonNodeAutomaticVersion(schemaNode);
        
        schema.initializeValidators(); // by default all schemas are loaded lazily. You can load them eagerly via
                                       // initializeValidators() 
        
        JsonNode node = getJsonNodeFromStringContent("{\"id\": \"2\"}");
        Set<ValidationMessage> errors = schema.validate(node);
        assertThat(errors.size(), is(1));
    }

}

```
