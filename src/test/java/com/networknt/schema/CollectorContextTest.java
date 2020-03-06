package com.networknt.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CollectorContextTest {

	private static final String SAMPLE_COLLECTOR_TYPE = "sampleCollectorType";

	private class ReferenceTypesKeyWord implements Keyword {
		@Override
		public String getValue() {
			return "reference-content-types";
		}

		@Override
		public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
				ValidationContext validationContext) throws JsonSchemaException, Exception {
			if (schemaNode != null && schemaNode.isArray()) {
				return new ReferenceTypesValidator();
			}
			return null;
		}
	}

	private class ReferenceTypesValidator implements JsonValidator {
		@Override
		public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
			CollectorContext collectorContext = CollectorContext.INSTANCE;
			collectorContext.add(SAMPLE_COLLECTOR_TYPE, new Collector<List<String>>() {
				@Override
				public List<String> collect() {
					List<String> references = new ArrayList<String>();
					references.add("value");
					return references;
				}
			});
			return new TreeSet<ValidationMessage>();
		}

		@Override
		public Set<ValidationMessage> validate(JsonNode rootNode) {
			return validate(rootNode, rootNode, BaseJsonValidator.AT_ROOT);
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCollectorContextWithKeyword() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		final JsonMetaSchema metaSchema = getJsonMetaSchema(
				"https://github.com/networknt/json-schema-validator/tests/schemas/example01#");
		final JsonSchemaFactory schemaFactory = JsonSchemaFactory
				.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
				.build();
		final JsonSchema schema = schemaFactory.getSchema(
				"{\"$schema\": \"https://github.com/networknt/json-schema-validator/tests/schemas/example01#\","
						+ "\"title\" : \"Sample test schema\",\n"
						+ "\"description\" : \"Sample schema definition\",\"type\" : \"object\",\"properties\" :{\"excerpt\" : {"
						+ "\"title\": \"Excerpt\","
						+ "\"type\": \"string\", \"reference-content-types\":[\"x\",\"y\"]}},"
						+ "\"required\": [\"excerpt\"]\n" + "}");
		Set<ValidationMessage> messages = schema.validateAndCollect(objectMapper.readTree("{\"excerpt\":\"sample\" }"));
		Assert.assertEquals(0, messages.size());
		List<String> contextValue = (List<String>) CollectorContext.INSTANCE.get(SAMPLE_COLLECTOR_TYPE);
		Assert.assertEquals(contextValue.get(0), "value");
	}

	protected JsonMetaSchema getJsonMetaSchema(String uri) throws Exception {
		JsonMetaSchema jsonMetaSchema = JsonMetaSchema.builder(uri, JsonMetaSchema.getV201909())
				.addKeyword(new ReferenceTypesKeyWord()).build();
		return jsonMetaSchema;
	}

}
