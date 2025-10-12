package com.networknt.schema.path;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.dialect.Dialects;

/**
 * Tests for evaluation path.
 */
public class EvaluationPathTest {
    @Test
    void baseUriChange() {
        String schemaData = "[\r\n"
                + "    {\r\n"
                + "        \"schema\": {\r\n"
                + "            \"id\": \"http://localhost:1234/\",\r\n"
                + "            \"items\": {\r\n"
                + "                \"id\": \"baseUriChange/\",\r\n"
                + "                \"items\": {\"$ref\": \"folderInteger.json\"}\r\n"
                + "            }\r\n"
                + "        }\r\n"
                + "    }\r\n"
                + "]";
        
        String folderIntegerSchemaData = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";
        
        Map<String, String> schemas = new HashMap<>();
        schemas.put("http://www.example.org/refRemote.json", schemaData);
        schemas.put("http://localhost:1234/baseUriChange/folderInteger.json", folderIntegerSchemaData);
        
        String instanceData = "[[1,2,3,4,\"5\"]]";
        
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4,
                builder -> builder.schemas(schemas));
        Schema schemaWithIdFromUri = schemaRegistry.getSchema(SchemaLocation.of("http://www.example.org/refRemote.json#/0/schema"));
        assertEquals("http://localhost:1234/#", schemaWithIdFromUri.getSchemaLocation().toString());
        List<Error> errors = schemaWithIdFromUri.validate(instanceData, InputFormat.JSON);
        assertEquals(1, errors.size());
        // Previously the evaluation path was part of the state of the schema so the initial path might not be correct as it matches the schema location fragment 
        // assertEquals("/0/schema/items/items/$ref/type", errors.get(0).getEvaluationPath().toString());
        assertEquals("/items/items/$ref/type", errors.get(0).getEvaluationPath().toString());
        assertEquals("http://localhost:1234/baseUriChange/folderInteger.json#/type", errors.get(0).getSchemaLocation().toString());
        assertEquals("/0/4", errors.get(0).getInstanceLocation().toString());
        assertEquals("type", errors.get(0).getKeyword());
    }
    
    @Test
    void openapi() {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getOpenApi30());
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of(
                "classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/requestBody/content/application~1json/schema"));
        String invalid = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"meow\": \"meeeooow\"\r\n"
                + "}";
        
        assertEquals("classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/requestBody/content/application~1json/schema", schema.getSchemaLocation().toString());
        List<Error> errors = schema.validate(invalid, InputFormat.JSON);
        assertEquals(2, errors.size());
        assertEquals("oneOf", errors.get(0).getKeyword());
        // Previously the evaluation path was part of the state of the schema so the initial path might not be correct as it matches the schema location fragment 
        //assertEquals("/paths/~1pet/post/requestBody/content/application~1json/schema/$ref/oneOf", errors.get(0).getEvaluationPath().toString());
        assertEquals("/$ref/oneOf", errors.get(0).getEvaluationPath().toString());
        assertEquals("classpath:schema/oas/3.0/petstore.yaml#/components/schemas/PetRequest/oneOf", errors.get(0).getSchemaLocation().toString());
        assertEquals("", errors.get(0).getInstanceLocation().toString());
        assertEquals("required", errors.get(1).getKeyword());
        assertEquals("bark", errors.get(1).getProperty());
        //assertEquals("/paths/~1pet/post/requestBody/content/application~1json/schema/$ref/oneOf/1/$ref/allOf/1/required", errors.get(1).getEvaluationPath().toString());
        assertEquals("/$ref/oneOf/1/$ref/allOf/1/required", errors.get(1).getEvaluationPath().toString());
        assertEquals("classpath:schema/oas/3.0/petstore.yaml#/components/schemas/Dog/allOf/1/required", errors.get(1).getSchemaLocation().toString());
        assertEquals("", errors.get(1).getInstanceLocation().toString());
    }

}
