package com.networknt.schema;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.KeywordWalkEvent;
import com.networknt.schema.walk.KeywordWalkListener;

public class JsonWalkTest {
	
	private JsonSchema jsonSchema;
	
	private static final String SAMPLE_COLLECTOR = "sampleCollectorType";
	
	private static final String CUSTOM_KEYWORD = "custom-keyword";

	@Before
	public void setup() throws Exception {
		setupSchema();
	}

	private void setupSchema() throws Exception {
		final JsonMetaSchema metaSchema = getJsonMetaSchema(
				"https://github.com/networknt/json-schema-validator/tests/schemas/example01");
		final JsonSchemaFactory schemaFactory = JsonSchemaFactory
				.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
				.addKeywordWalkListener(new DefaultListener()).build();
		this.jsonSchema = schemaFactory.getSchema(getSchemaString());
	}

	
	private JsonMetaSchema getJsonMetaSchema(String uri) throws Exception {
        JsonMetaSchema jsonMetaSchema = JsonMetaSchema.builder(uri, JsonMetaSchema.getV201909())
                .addKeyword(new CustomKeyword()).build();
        return jsonMetaSchema;
    }
	
	@SuppressWarnings({ "unused", "unchecked" })
	@Test
	public void testWalk() throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		ValidationResult result = jsonSchema
				.walk(objectMapper.readTree("{\"property1\":\"sample1\",\"property2\":\"sample2\"}"), false);
		List<String> contextValues = (List<String>) result.getCollectorContext().get(SAMPLE_COLLECTOR);
	}
	
	private String getSchemaString() {
        return "{"
                + 	"\"$schema\": \"https://github.com/networknt/json-schema-validator/tests/schemas/example01\","
                + 	"\"title\" : \"Sample test schema\","
                + 	"\"description\" : \"Sample schema definition\","
                + 	"\"type\" : \"object\","
                + 	"\"properties\" :"
                + 	"{"
	            + 		"\"property1\" :"
	            +   	"{"
		        + 			"\"title\": \"Property1\","
		        + 			"\"type\": \"string\", "
		        + 			"\"" + CUSTOM_KEYWORD  + "\"" + ":[\"x\",\"y\"]"
		        +   	"},"
		        +   	"\"property2\" :"
		        +   	"{"
		        + 			"\"title\": \"Property2\","
		        + 			"\"type\": \"string\", "
		        + 			"\"" + CUSTOM_KEYWORD  + "\"" + ":[\"x\",\"y\"]"
		        +   	"}"
                + 	"},"
                + 	"\"additionalProperties\":\"false\","
                + 	"\"required\": [\"property1\"]"
                + 
             "}";
    }
	
	/**
     * Our own custom keyword.
     */
    private class CustomKeyword implements Keyword {
        @Override


    	
        public String getValue() {
            return "custom-keyword";
        }

        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                          ValidationContext validationContext) throws JsonSchemaException, Exception {
            if (schemaNode != null && schemaNode.isArray()) {
                return new CustomValidator();
            }
            return null;
        }
        
        /**
         * We will be collecting information/data by adding the data in the form of
         * collectors into collector context object while we are validating this node.
         * This will be helpful in cases where we don't want to revisit the entire JSON
         * document again just for gathering this kind of information.
         */
        private class CustomValidator implements JsonValidator {

            @Override
            public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
                return new TreeSet<ValidationMessage>();
            }

            @Override
            public Set<ValidationMessage> validate(JsonNode rootNode) {
                return validate(rootNode, rootNode, BaseJsonValidator.AT_ROOT);
            }

			@Override
			public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at,
					boolean shouldValidateSchema) {
				return new LinkedHashSet<ValidationMessage>();
			}
        }
    }
    
	private class DefaultListener implements KeywordWalkListener {

		@SuppressWarnings("unchecked")
		@Override
		public void onWalkStart(KeywordWalkEvent keywordWalkEvent) {
			String keyWordName = keywordWalkEvent.getKeyWordName();
			JsonNode schemaNode = keywordWalkEvent.getSchemaNode();
			CollectorContext collectorContext = CollectorContext.getInstance();
			if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
				collectorContext.add(SAMPLE_COLLECTOR, new ArrayList<String>());
			}
			if (keyWordName.equals(CUSTOM_KEYWORD) && schemaNode.get(CUSTOM_KEYWORD).isArray()) {
				List<String> returnList = (List<String>) collectorContext.get(SAMPLE_COLLECTOR);
				returnList.add(keywordWalkEvent.getNode().textValue());
			}
		}

		@Override
		public void onWalkEnd(KeywordWalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {
			
		}


	}

}
