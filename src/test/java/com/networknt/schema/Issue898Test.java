package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.stream.Collectors.toList;

class Issue898Test extends BaseJsonSchemaValidatorTest {

    @Test
    void testMessagesWithSingleQuotes() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle(I18nSupport.DEFAULT_BUNDLE_BASE_NAME, Locale.FRENCH);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setResourceBundle(bundle);

        JsonSchema schema = getJsonSchemaFromClasspath("schema/issue898.json", SpecVersion.VersionFlag.V202012, config);
        JsonNode node = getJsonNodeFromClasspath("data/issue898.json");

        List<String> messages = schema.validate(node).stream()
                .map(ValidationMessage::getMessage)
                .collect(toList());

        Assertions.assertEquals(2, messages.size());
        Assertions.assertEquals("$.foo: n'a pas de valeur dans l'énumération [foo1, foo2]", messages.get(0));
        Assertions.assertEquals("$.bar ne correspond pas à l'expression régulière (bar)+", messages.get(1));
    }

}
