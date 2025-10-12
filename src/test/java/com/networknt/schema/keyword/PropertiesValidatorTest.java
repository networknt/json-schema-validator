package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.BaseJsonSchemaValidatorTest;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Result;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.walk.ApplyDefaultsStrategy;
import com.networknt.schema.walk.KeywordWalkListenerRunner;
import com.networknt.schema.walk.PropertyWalkListenerRunner;
import com.networknt.schema.walk.WalkConfig;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import com.networknt.schema.walk.WalkListener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by josejulio on 25/04/22.
 */
class PropertiesValidatorTest extends BaseJsonSchemaValidatorTest {

    @Test
    void testDoesNotThrowWhenApplyingDefaultPropertiesToNonObjects() throws Exception {
        Assertions.assertDoesNotThrow(() -> {
            WalkConfig walkConfig = WalkConfig.builder().applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true)).build();
            SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4);
            Schema schema = factory.getSchema("{\"type\":\"object\",\"properties\":{\"foo\":{\"type\":\"object\", \"properties\": {} },\"i-have-default\":{\"type\":\"string\",\"default\":\"foo\"}}}");
            JsonNode node = getJsonNodeFromStringContent("{\"foo\": \"bar\"}");
            Result result = schema.walk(node, true, executionContext -> executionContext.setWalkConfig(walkConfig));
            Assertions.assertEquals(result.getErrors().size(), 1);
        });
    }
    
    @Test
    void evaluationPath() {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
        String schemaData = "{\n"
                + "  \"type\": \"object\",\n"
                + "  \"properties\": {\n"
                + "    \"productId\": {\n"
                + "      \"type\": \"integer\",\n"
                + "      \"minimum\": 1\n"
                + "    }\n"
                + "  }\n"
                + "}";
        String instanceData = "{\n"
                + "  \"productId\": 0\n"
                + "}";
        Schema schema = schemaRegistry.getSchema(schemaData, InputFormat.JSON);
        List<Error> errors = schema.validate(instanceData, InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("/properties/productId/minimum", errors.get(0).getEvaluationPath().toString());
        assertEquals("#/properties/productId/minimum", errors.get(0).getSchemaLocation().toString());
        assertEquals("minimum", errors.get(0).getKeyword());
    }

    @Test
    void evaluationPathWalk() {
        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder()
                .propertyWalkListener(new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }
                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                }).build();

        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }
                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                }).build();

        
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
        String schemaData = "{\n"
                + "  \"type\": \"object\",\n"
                + "  \"properties\": {\n"
                + "    \"productId\": {\n"
                + "      \"type\": \"integer\",\n"
                + "      \"minimum\": 1\n"
                + "    }\n"
                + "  },\n"
                + "  \"additionalProperties\": {\n"
                + "    \"type\": \"integer\"\n"
                + "  }\n"
                + "}";
        String instanceData = "{\n"
                + "  \"productId\": 0,\n"
                + "  \"product\": \"hello\"\n"
                + "}";
        Schema schema = schemaRegistry.getSchema(schemaData, InputFormat.JSON);
        Result result = schema.walk(instanceData, InputFormat.JSON, true,
                executionContext -> executionContext
                        .walkConfig(walkConfig -> walkConfig.propertyWalkListenerRunner(propertyWalkListenerRunner)
                                .keywordWalkListenerRunner(keywordWalkListenerRunner)));
        List<Error> errors = result.getErrors();
        assertEquals(2, errors.size());
        assertEquals("/properties/productId/minimum", errors.get(0).getEvaluationPath().toString());
        assertEquals("#/properties/productId/minimum", errors.get(0).getSchemaLocation().toString());
        assertEquals("minimum", errors.get(0).getKeyword());
        assertEquals("/additionalProperties/type", errors.get(1).getEvaluationPath().toString());
        assertEquals("#/additionalProperties/type", errors.get(1).getSchemaLocation().toString());
        assertEquals("type", errors.get(1).getKeyword());
    }
}
