package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Issue784Test {

    private static final String FOO_BAR = "foo-bar";
    private static final String SOMETHING_ELSE = "something-else";

    static class CustomDateTimeFormat implements Format {

        @Override
        public String getName() {
            return "date-time";
        }

        @Override
        public boolean matches(ExecutionContext executionContext, String value) {
            return value.equals(FOO_BAR);
        }

        @Override
        public String getErrorMessageDescription() {
            return null;
        }
    }

    @Test
    void allowToOverrideDataTime() throws IOException {
        JsonSchema jsonSchema = createSchema(true);

        // Custom validator checks for FOO_BAR
        assertEquals(0, validate(jsonSchema, FOO_BAR).size());

        // Fails with SOMETHING_ELSE
        assertEquals(1, validate(jsonSchema, SOMETHING_ELSE).size());
    }

    @Test
    void useDefaultValidatorIfNotOverriden() throws IOException {
        JsonSchema jsonSchema = createSchema(false);

        // Default validator fails with FOO_BAR
        assertEquals(1, validate(jsonSchema, FOO_BAR).size());

        // Default validator fails with SOMETHING_ELSE
        assertEquals(1, validate(jsonSchema, SOMETHING_ELSE).size());
    }


    private Set<ValidationMessage> validate(JsonSchema jsonSchema, String myDateTimeContent) throws JsonProcessingException {
        return jsonSchema.validate(new ObjectMapper().readTree(" { \"my-date-time\": \"" + myDateTimeContent + "\" } "));
    }

    private JsonSchema createSchema(boolean useCustomDateFormat) {
        JsonMetaSchema overrideDateTimeValidator =JsonMetaSchema
                .builder(JsonMetaSchema.getV7().getIri(), JsonMetaSchema.getV7())
                .formats(formats -> {
                    if (useCustomDateFormat) {
                        CustomDateTimeFormat format = new CustomDateTimeFormat();
                        formats.put(format.getName(), format);
                    }
                })
                .build();

        return new JsonSchemaFactory
                .Builder()
                .defaultMetaSchemaIri(overrideDateTimeValidator.getIri())
                .metaSchema(overrideDateTimeValidator)
                .build()
                .getSchema(Issue784Test.class.getResourceAsStream("/issue784/schema.json"));
    }
}
