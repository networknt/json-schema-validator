package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Issue471Test {

    private JsonSchema getJsonSchemaFromStreamContentV201909(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        return factory.getSchema(schemaContent);
    }

    private JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    // Only one test method is allowed at a time

    @Test
    @Disabled
    public void shouldFailV201909_with_enUS() throws Exception {
        Locale.setDefault(Locale.US);
        Set<ValidationMessage> errors = validate();
        Map<String, String> errorMsgMap = transferErrorMsg(errors);
        Assertions.assertTrue(errorMsgMap.containsKey("$.title"), "error message must contains key: $.title");
        Assertions.assertTrue(errorMsgMap.containsKey("$.pictures"), "error message must contains key: $.pictures");
        Assertions.assertEquals("$.title: may only be 10 characters long", errorMsgMap.get("$.title"));
        Assertions.assertEquals("$.pictures: there must be a maximum of 2 items in the array", errorMsgMap.get("$.pictures"));
    }

    @Test
    @Disabled
    public void shouldFailV201909_with_zhCN() throws Exception {
        Locale.setDefault(Locale.CHINA);
        Set<ValidationMessage> errors = validate();
        Map<String, String> errorMsgMap = transferErrorMsg(errors);
        Assertions.assertTrue(errorMsgMap.containsKey("$.title"), "error message must contains key: $.title");
        Assertions.assertTrue(errorMsgMap.containsKey("$.pictures"), "error message must contains key: $.pictures");
        Assertions.assertEquals("$.title：可能只有 10 个字符长", errorMsgMap.get("$.title"));
        Assertions.assertEquals("$.pictures：数组中最多必须有 2 个项目", errorMsgMap.get("$.pictures"));
    }

    private Set<ValidationMessage> validate() throws Exception {
        String schemaPath = "/schema/issue471-2019-09.json";
        String dataPath = "/data/issue471.json";
        InputStream schemaInputStream = Issue471Test.class.getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV201909(schemaInputStream);
        InputStream dataInputStream = Issue471Test.class.getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        return schema.validate(node);
    }

    private Map<String, String> transferErrorMsg(Set<ValidationMessage> validationMessages) {
        Map<String, String> pathToMessage = new HashMap<>();
        validationMessages.forEach(msg -> {
            pathToMessage.put(msg.getPath(), msg.getMessage());
        });
        return pathToMessage;
    }
}
