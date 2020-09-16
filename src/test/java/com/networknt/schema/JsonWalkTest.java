package com.networknt.schema;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkListener;

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
				.addKeywordWalkListener(new AllKeywordListener())
				.addKeywordWalkListener(ValidatorTypeCode.REF.getValue(), new RefKeywordListener())
				.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new PropertiesKeywordListener()).build();
		this.jsonSchema = schemaFactory.getSchema(getSchema());
	}

	private JsonMetaSchema getJsonMetaSchema(String uri) throws Exception {
		JsonMetaSchema jsonMetaSchema = JsonMetaSchema.builder(uri, JsonMetaSchema.getV201909())
				.addKeyword(new CustomKeyword()).build();
		return jsonMetaSchema;
	}

	@Test
	public void testWalk() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		ValidationResult result = jsonSchema.walk(
				objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data.json")), false);
		JsonNode collectedNode = (JsonNode) result.getCollectorContext().get(SAMPLE_COLLECTOR);
		assertTrue(collectedNode.equals(objectMapper.readTree("{" + 
				"    \"PROPERTY1\": \"sample1\","
				+"    \"PROPERTY2\": \"sample2\"," 
				+"    \"property3\": {" 
				+"        \"street_address\":\"test-address\"," 
				+"        \"phone_number\": {" 
				+"            \"country-code\": \"091\"," 
				+"            \"number\": \"123456789\"" 
				+"          }"
				+"     }" 
				+"}")));
	}

	private InputStream getSchema() {
		return getClass().getClassLoader().getResourceAsStream("schema/walk-schema.json");
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

	private class AllKeywordListener implements WalkListener {
		@Override
		public boolean onWalkStart(WalkEvent keywordWalkEvent) {
			ObjectMapper mapper = new ObjectMapper();
			String keyWordName = keywordWalkEvent.getKeyWordName();
			JsonNode schemaNode = keywordWalkEvent.getSchemaNode();
			CollectorContext collectorContext = CollectorContext.getInstance();
			if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
				collectorContext.add(SAMPLE_COLLECTOR, mapper.createObjectNode());
			}
			if (keyWordName.equals(CUSTOM_KEYWORD) && schemaNode.get(CUSTOM_KEYWORD).isArray()) {
				ObjectNode objectNode = (ObjectNode) collectorContext.get(SAMPLE_COLLECTOR);
				objectNode.put(keywordWalkEvent.getSchemaNode().get("title").textValue().toUpperCase(),
						keywordWalkEvent.getNode().textValue());
			}
			return true;
		}

		@Override
		public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {

		}
	}

	private class RefKeywordListener implements WalkListener {

		@Override
		public boolean onWalkStart(WalkEvent keywordWalkEvent) {
			ObjectMapper mapper = new ObjectMapper();
			CollectorContext collectorContext = CollectorContext.getInstance();
			if (collectorContext.get(SAMPLE_COLLECTOR) == null) {
				collectorContext.add(SAMPLE_COLLECTOR, mapper.createObjectNode());
			}
			ObjectNode objectNode = (ObjectNode) collectorContext.get(SAMPLE_COLLECTOR);
			objectNode.set(keywordWalkEvent.getSchemaNode().get("title").textValue().toLowerCase(),
					keywordWalkEvent.getNode());
			return false;
		}

		@Override
		public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {
			
		}
	}
	
	private class PropertiesKeywordListener implements WalkListener {

		@Override
		public boolean onWalkStart(WalkEvent keywordWalkEvent) {
			JsonNode schemaNode = keywordWalkEvent.getSchemaNode();
			if(schemaNode.get("title").textValue().equals("Property3")) {
				return false;
			}
			return true;
		}

		@Override
		public void onWalkEnd(WalkEvent keywordWalkEvent, Set<ValidationMessage> validationMessages) {
			
		}
	}

}
