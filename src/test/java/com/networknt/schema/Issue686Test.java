package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.i18n.DefaultMessageSource;
import com.networknt.schema.i18n.ResourceBundleMessageSource;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue686Test {

    @Test
    void testDefaults() {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        assertEquals(DefaultMessageSource.getInstance(), config.getMessageSource());
    }

    @Test
    void testValidationWithDefaultBundleAndLocale() throws JsonProcessingException {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(DefaultMessageSource.BUNDLE_BASE_NAME, Locale.getDefault());
        String expectedMessage = new MessageFormat(resourceBundle.getString("type")).format(new String[] {"$.foo", "integer", "string"});
        verify(config, expectedMessage);
    }

    @Test
    void testValidationWithDefaultBundleAndCustomLocale() throws JsonProcessingException {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setLocale(Locale.ITALIAN);
        verify(config, "$.foo: integer trovato, string atteso");
    }

    @Test
    void testValidationWithCustomBundle() throws JsonProcessingException {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setMessageSource(new ResourceBundleMessageSource("issue686/translations"));
        config.setLocale(Locale.FRENCH);
        verify(config, "$.foo: integer found, string expected (TEST) (FR)");
    }

    @Test
    void testLocaleSwitch() throws JsonProcessingException {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setLocale(Locale.ITALIAN);
        verify(config, "$.foo: integer trovato, string atteso");
        config.setLocale(Locale.FRENCH);
        verify(config, "$.foo: integer a été trouvé, mais string est attendu");
    }

    private JsonSchema getSchema(SchemaValidatorsConfig config) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        return factory.getSchema("{ \"$schema\": \"https://json-schema.org/draft/2019-09/schema\", \"$id\": \"https://json-schema.org/draft/2019-09/schema\", \"type\": \"object\", \"properties\": { \"foo\": { \"type\": \"string\" } } } }", config);
    }

    private void verify(SchemaValidatorsConfig config, String expectedMessage) throws JsonProcessingException {
        Set<ValidationMessage> messages = getSchema(config).validate(new ObjectMapper().readTree(" { \"foo\": 123 } "));
        assertEquals(1, messages.size());
        assertEquals(expectedMessage, messages.iterator().next().getMessage());
    }

}
