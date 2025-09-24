package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.i18n.DefaultMessageSource;
import com.networknt.schema.i18n.ResourceBundleMessageSource;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Issue686Test {

    @Test
    void testDefaults() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().build();
        assertEquals(DefaultMessageSource.getInstance(), config.getMessageSource());
    }

    @Test
    void testValidationWithDefaultBundleAndLocale() throws JsonProcessingException {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().build();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(DefaultMessageSource.BUNDLE_BASE_NAME, Locale.getDefault());
        String expectedMessage = new MessageFormat(resourceBundle.getString("type")).format(new String[] {"integer", "string"});
        verify(config, "/foo: " + expectedMessage);
    }

    @Test
    void testValidationWithDefaultBundleAndCustomLocale() throws JsonProcessingException {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().locale(Locale.ITALIAN).build();
        verify(config, "/foo: integer trovato, string previsto");
    }

    @Test
    void testValidationWithCustomBundle() throws JsonProcessingException {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder()
                .messageSource(new ResourceBundleMessageSource("issue686/translations"))
                .locale(Locale.FRENCH)
                .build();
        verify(config, "/foo: integer found, string expected (TEST) (FR)");
    }

    @Test
    void testLocaleSwitch() throws JsonProcessingException {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().locale(Locale.ITALIAN).build();
        verify(config, "/foo: integer trovato, string previsto");
        SchemaRegistryConfig config2 = SchemaRegistryConfig.builder().locale(Locale.FRENCH).build();
        verify(config2, "/foo: integer trouvé, string attendu");
    }

    private Schema getSchema(SchemaRegistryConfig config) {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09, builder -> builder.schemaRegistryConfig(config));
        return factory.getSchema("{ \"$schema\": \"https://json-schema.org/draft/2019-09/schema\", \"$id\": \"https://json-schema.org/draft/2019-09/schema\", \"type\": \"object\", \"properties\": { \"foo\": { \"type\": \"string\" } } } }");
    }

    private void verify(SchemaRegistryConfig config, String expectedMessage) throws JsonProcessingException {
        List<Error> messages = getSchema(config).validate(new ObjectMapper().readTree(" { \"foo\": 123 } "));
        assertEquals(1, messages.size());
        assertEquals(expectedMessage, messages.iterator().next().toString());
    }

}
