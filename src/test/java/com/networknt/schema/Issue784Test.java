package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue784Test {

    private static final String FOO_BAR = "foo-bar";
    private static final String SOMETHING_ELSE = "something-else";

    static class CustomDateTimeFormat implements Format {

        @Override
        public String getName() {
            return "date-time";
        }

        @Override
        public boolean matches(String value) {
            return value.equals(FOO_BAR);
        }

        @Override
        public String getErrorMessageDescription() {
            return null;
        }
    }

    @Test
    public void allowToOverrideDataTime() throws IOException {
        JsonSchema jsonSchema = createSchema(true);

        // Custom validator checks for FOO_BAR
        assertEquals(0, validate(jsonSchema, FOO_BAR).size());

        // Fails with SOMETHING_ELSE
        assertEquals(1, validate(jsonSchema, SOMETHING_ELSE).size());
    }

    @Test
    public void useDefaultValidatorIfNotOverriden() throws IOException {
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
        JsonMetaSchema overrideDateTimeValidator = new JsonMetaSchema
                .Builder(JsonMetaSchema.getV7().getUri())
                .idKeyword("$id")
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V7))
                .addFormats(useCustomDateFormat ? Collections.singletonList(new CustomDateTimeFormat()) : Collections.emptyList())
                .build();

        return new JsonSchemaFactory
                .Builder()
                .defaultMetaSchemaURI(overrideDateTimeValidator.getUri())
                .addMetaSchema(overrideDateTimeValidator)
                .build()
                .getSchema(Issue784Test.class.getResourceAsStream("/issue784/schema.json"));
    }
}
