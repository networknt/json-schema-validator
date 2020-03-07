package com.networknt.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CollectorContextTest {

	private static final String SAMPLE_COLLECTOR_TYPE = "sampleCollectorType";

	private JsonSchema jsonSchema;
	
	
	@Before
	public void setup() throws Exception {
		setupSchema();
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCollectorContextWithKeyword() throws Exception {
		ValidationResult validationResult = validate("{\"test-property\":\"sample\" }");
		Assert.assertEquals(0, validationResult.getValidationMessages().size());
		List<String> contextValue = (List<String>) validationResult.getCollectorContext().get(SAMPLE_COLLECTOR_TYPE);
		Assert.assertEquals(contextValue.get(0), "actual_value_added_to_context");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCollectorContextWithMultiplThreads() throws Exception {

		ValidationThread validationRunnable1 = new ValidationThread("{\"test-property\":\"sample1\" }", "thread1");
		ValidationThread validationRunnable2 = new ValidationThread("{\"test-property\":\"sample2\" }", "thread2");
		ValidationThread validationRunnable3 = new ValidationThread("{\"test-property\":\"sample3\" }", "thread3");

		// This simulates calling the validateAndCollect method from three different
		// threads.It should be noted that all these three threads use same
		// json schema instance.Create three threads with there own Runnables.
		Thread thread1 = new Thread(validationRunnable1);
		Thread thread2 = new Thread(validationRunnable2);
		Thread thread3 = new Thread(validationRunnable3);

		thread1.start();
		thread2.start();
		thread3.start();

		thread1.join();
		thread2.join();
		thread3.join();

		ValidationResult validationResult1 = validationRunnable1.getValidationResult();
		ValidationResult validationResult2 = validationRunnable2.getValidationResult();
		ValidationResult validationResult3 = validationRunnable3.getValidationResult();

		Assert.assertEquals(0, validationResult1.getValidationMessages().size());
		Assert.assertEquals(0, validationResult2.getValidationMessages().size());
		Assert.assertEquals(0, validationResult3.getValidationMessages().size());

		List<String> contextValue1 = (List<String>) validationResult1.getCollectorContext().get(SAMPLE_COLLECTOR_TYPE);
		List<String> contextValue2 = (List<String>) validationResult2.getCollectorContext().get(SAMPLE_COLLECTOR_TYPE);
		List<String> contextValue3 = (List<String>) validationResult3.getCollectorContext().get(SAMPLE_COLLECTOR_TYPE);

		Assert.assertEquals(contextValue1.get(0), "actual_value_added_to_context1");
		Assert.assertEquals(contextValue2.get(0), "actual_value_added_to_context2");
		Assert.assertEquals(contextValue3.get(0), "actual_value_added_to_context3");
	}
	
	private JsonMetaSchema getJsonMetaSchema(String uri) throws Exception {
		JsonMetaSchema jsonMetaSchema = JsonMetaSchema.builder(uri, JsonMetaSchema.getV201909())
				.addKeyword(new CustomKeyword()).build();
		return jsonMetaSchema;
	}

	private void setupSchema() throws Exception {
		final JsonMetaSchema metaSchema = getJsonMetaSchema(
				"https://github.com/networknt/json-schema-validator/tests/schemas/example01#");
		final JsonSchemaFactory schemaFactory = JsonSchemaFactory
				.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).addMetaSchema(metaSchema)
				.build();
		this.jsonSchema = schemaFactory.getSchema(getSchemaString());
	}
	
	private String getSchemaString() {
		return "{"
				+ "\"$schema\": \"https://github.com/networknt/json-schema-validator/tests/schemas/example01#\","
				+ "\"title\" : \"Sample test schema\",\n"
				+ "\"description\" : \"Sample schema definition\","
				+ "\"type\" : \"object\","
				+ "\"properties\" :"
				+ "{"
					+ "\"test-property\" : "
					+ "{"
							+ "\"title\": \"Test Property\","
							+ "\"type\": \"string\", "
							+ "\"custom-keyword\":[\"x\",\"y\"]"
					+ "}"
				+ "},"
				+ "\"required\": [\"test-property\"]\n" 
		+ "}";
	}
	
	
	private class ValidationThread implements Runnable {

		private String data;
		
		private String name;

		private ValidationResult validationResult;

		ValidationThread(String data,String name) {
			this.name = name;
			this.data = data;
		}

		@Override
		public void run() {
			try {
				this.validationResult = validate(data);
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ValidationResult getValidationResult() {
			return this.validationResult;
		}
		
		@Override
		public String toString() {
			return "ValidationThread [data=" + data + ", name=" + name + ", validationResult=" + validationResult + "]";
		}

	}

	/**
	 *
	 * Our own custom keyword. In this case we don't use this keyword. It is just
	 * for demonstration purpose.
	 * 
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
	}

	 /**
	 *
	 *  We will be collecting information/data by adding the data in the form of
	 * collectors into collector context object while we are validating this node.
	 * This will be helpful in cases where we don't want to revisit the entire JSON
	 * document again just for gathering this kind of information. In this test case
	 * we try to add the data into collector based on the value of the node. For the
	 * purpose of this test case we use a map to derive the data to be added to the
	 * collector but it can be any other data source.
	 * 
	 */
	private class CustomValidator implements JsonValidator {
		@Override
		public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
			// Get an instance of collector context.
			CollectorContext collectorContext = CollectorContext.getInstance();
			collectorContext.add(SAMPLE_COLLECTOR_TYPE, new Collector<List<String>>() {
				@Override
				public List<String> collect() {
					List<String> references = new ArrayList<String>();
					references.add(getDatasourceMap().get(node.textValue()));
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

	private ValidationResult validate(String jsonData) throws JsonMappingException, JsonProcessingException, Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return this.jsonSchema.validateAndCollect(objectMapper.readTree(jsonData));
	}

	private Map<String, String> getDatasourceMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("sample", "actual_value_added_to_context");
		map.put("sample1", "actual_value_added_to_context1");
		map.put("sample2", "actual_value_added_to_context2");
		map.put("sample3", "actual_value_added_to_context3");
		return map;
	}
	
}
