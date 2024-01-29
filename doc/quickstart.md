## Quick Start

To use the validator, we need to have the `JsonSchema` loaded and cached.

For simplicity the following test loads a schema from a `String` or `JsonNode`. Note that loading a schema in this manner is not recommended as a relative `$ref` will not be properly resolved as there is no base IRI.

The preferred method of loading a schema is by using a `SchemaLocation` and by configuring the appropriate `SchemaMapper` and `SchemaLoader` on the `JsonSchemaFactory`.

```java
package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import com.networknt.schema.serialization.JsonMapperFactory;

public class SampleTest {
    @Test
    void schemaFromString() throws JsonMappingException, JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        /*
         * This should be cached for performance.
         * 
         * Loading from a String is not recommended as there is no base IRI to use for
         * resolving relative $ref.
         */
        JsonSchema schemaFromString = factory
                .getSchema("{\"enum\":[1, 2, 3, 4],\"enumErrorCode\":\"Not in the list\"}");
        Set<ValidationMessage> errors = schemaFromString.validate("7", InputFormat.JSON);
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromJsonNode() throws JsonMappingException, JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonNode schemaNode = JsonMapperFactory.getInstance().readTree(
                "{\"$schema\": \"http://json-schema.org/draft-06/schema#\", \"properties\": { \"id\": {\"type\": \"number\"}}}");
        /*
         * This should be cached for performance.
         * 
         * Loading from a JsonNode is not recommended as there is no base IRI to use for
         * resolving relative $ref.
         *
         * Note that the V4 from the factory is the default version if $schema is not
         * specified. As $schema is specified in the data, V6 is used.
         */
        JsonSchema schemaFromNode = factory.getSchema(schemaNode);
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromNode.initializeValidators();
        Set<ValidationMessage> errors = schemaFromNode.validate("{\"id\": \"2\"}", InputFormat.JSON);
        assertEquals(1, errors.size());
    }
}
```
