package com.networknt.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonMetaSchemaTest {

	@Test
	void timeFormatValidation() {
		Format timeFormat = null;
		for (Format commonBuiltinFormat : JsonMetaSchema.COMMON_BUILTIN_FORMATS) {
			if ("time".equals(commonBuiltinFormat.getName())) {
				timeFormat = commonBuiltinFormat;
			}
		}
		if (timeFormat == null) {
			throw new IllegalArgumentException("time format not found");
		}

		assertTrue(timeFormat.matches("14:16:23"));
		assertTrue(timeFormat.matches("14:16:23.123456"));
		assertFalse(timeFormat.matches("14:16:23."));

	}

}