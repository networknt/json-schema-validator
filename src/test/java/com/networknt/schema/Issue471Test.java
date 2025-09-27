package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

class Issue471Test {
    private final String DATA_PATH = "/data/issue471.json";
    private final String SCHEMA_PATH = "/schema/issue471-2019-09.json";

    // Only one test method is allowed at a time as the ResourceBundle is statically initialized

    @Test
    @Disabled
    void shouldFailV201909_with_enUS() throws Exception {
        Locale.setDefault(Locale.US);
        Map<String, String> errorsMap = validate();
        Assertions.assertEquals("$.title: may only be 10 characters long", errorsMap.get("$.title"));
        Assertions.assertEquals("$.pictures: there must be a maximum of 2 items in the array", errorsMap.get("$.pictures"));
    }

    @Test
    @Disabled
    void shouldFailV201909_with_zhCN() throws Exception {
        Locale.setDefault(Locale.CHINA);
        Map<String, String> errorsMap = validate();
        Assertions.assertEquals("$.title：可能只有 10 个字符长", errorsMap.get("$.title"));
        Assertions.assertEquals("$.pictures：数组中最多必须有 2 个项目", errorsMap.get("$.pictures"));
    }

    @Test
    @Disabled
    void shouldFailV201909_with_deDE() throws Exception {
        Locale.setDefault(Locale.GERMANY);
        Map<String, String> errorsMap = validate();
        Assertions.assertEquals("$.title darf höchstens 10 Zeichen lang sein", errorsMap.get("$.title"));
        Assertions.assertEquals("$.pictures: Es dürfen höchstens 2 Elemente in diesem Array sein", errorsMap.get("$.pictures"));
    }

    @Test
    @Disabled
    void shouldFailV201909_with_frFR() throws Exception {
        Locale.setDefault(Locale.FRANCE);
        Map<String, String> errorsMap = validate();
        Assertions.assertEquals("$.title: ne doit pas dépasser 10 caractères", errorsMap.get("$.title"));
        Assertions.assertEquals("$.pictures: doit avoir un maximum de 2 éléments dans le tableau", errorsMap.get("$.pictures"));
    }

    @Test
    @Disabled
    void shouldFailV201909_with_frIT() throws Exception {
        Locale.setDefault(Locale.ITALIAN);
        Map<String, String> errorsMap = validate();
        Assertions.assertEquals("$.title: può avere lunghezza massima di 10", errorsMap.get("$.title"));
        Assertions.assertEquals("$.pictures: deve esserci un numero massimo di 2 elementi nell'array", errorsMap.get("$.pictures"));
    }

    private Map<String, String> validate() throws Exception {
        InputStream schemaInputStream = Issue471Test.class.getResourceAsStream(SCHEMA_PATH);
        JsonSchema schema = getJsonSchemaFromStreamContentV201909(schemaInputStream);
        InputStream dataInputStream = Issue471Test.class.getResourceAsStream(DATA_PATH);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);

        List<ValidationMessage> validationMessages = schema.validate(node);
        return convertValidationMessagesToMap(validationMessages);
    }

    private Map<String, String> convertValidationMessagesToMap(List<ValidationMessage> validationMessages) {
        return validationMessages.stream().collect(Collectors.toMap(m -> m.getInstanceLocation().toString(), ValidationMessage::getMessage));
    }

    private JsonSchema getJsonSchemaFromStreamContentV201909(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        return factory.getSchema(schemaContent);
    }

    private JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

}
