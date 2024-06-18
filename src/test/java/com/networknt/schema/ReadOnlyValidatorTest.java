package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ReadOnlyValidatorTest {

	@Test
	void givenConfigWriteFalseWhenReadOnlyTrueThenAllows() throws IOException {
		ObjectNode node = getJsonNode();
		Set<ValidationMessage> errors = loadJsonSchema(false).validate(node);
		assertTrue(errors.isEmpty());
	}

	@Test
	void givenConfigWriteTrueWhenReadOnlyTrueThenDenies() throws IOException {
		ObjectNode node = getJsonNode();
		Set<ValidationMessage> errors = loadJsonSchema(true).validate(node);
		assertFalse(errors.isEmpty());
		assertEquals("/firstName: is a readonly field, it cannot be changed",
				errors.stream().map(e -> e.getMessage()).collect(Collectors.toList()).get(0));
	}

	private JsonSchema loadJsonSchema(Boolean write) {
		JsonSchema schema = this.getJsonSchema(write);
		schema.initializeValidators();
		return schema;

	}

	private JsonSchema getJsonSchema(Boolean write) {
		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
		SchemaValidatorsConfig schemaConfig = createSchemaConfig(write);
		InputStream schema = getClass().getClassLoader().getResourceAsStream("schema/read-only-schema.json");
		return factory.getSchema(schema, schemaConfig);
	}

	private SchemaValidatorsConfig createSchemaConfig(Boolean write) {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().readOnly(write).build();
		return config;
	}

	private ObjectNode getJsonNode() throws IOException {
		InputStream node = getClass().getClassLoader().getResourceAsStream("data/read-only-data.json");
		ObjectMapper mapper = new ObjectMapper();
		return (ObjectNode) mapper.readTree(node);
	}

}
