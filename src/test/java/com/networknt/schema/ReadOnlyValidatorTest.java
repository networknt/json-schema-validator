package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ReadOnlyValidatorTest {

	@Test
	void givenConfigWriteFalseWhenReadOnlyTrueThenAllows() throws IOException {
		ObjectNode node = getJsonNode();
		List<Error> errors = loadJsonSchema().validate(node, executionContext -> executionContext
				.executionConfig(executionConfig -> executionConfig.readOnly(false)));
		assertTrue(errors.isEmpty());
	}

	@Test
	void givenConfigWriteTrueWhenReadOnlyTrueThenDenies() throws IOException {
		ObjectNode node = getJsonNode();
		List<Error> errors = loadJsonSchema().validate(node, executionContext -> executionContext
				.executionConfig(executionConfig -> executionConfig.readOnly(true)));
		assertFalse(errors.isEmpty());
		assertEquals("/firstName: is a readonly field, it cannot be changed",
				errors.stream().map(e -> e.toString()).collect(Collectors.toList()).get(0));
	}

	private Schema loadJsonSchema() {
		Schema schema = this.getJsonSchema();
		schema.initializeValidators();
		return schema;

	}

	private Schema getJsonSchema() {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_2020_12);
		InputStream schema = getClass().getClassLoader().getResourceAsStream("schema/read-only-schema.json");
		return factory.getSchema(schema);
	}

	private ObjectNode getJsonNode() throws IOException {
		InputStream node = getClass().getClassLoader().getResourceAsStream("data/read-only-data.json");
		ObjectMapper mapper = new ObjectMapper();
		return (ObjectNode) mapper.readTree(node);
	}

}
