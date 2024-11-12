package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.SpecVersion.VersionFlag;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OverwritingCustomMessageBugTest {
  private JsonSchema getJsonSchemaFromStreamContentV7(InputStream schemaContent) {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);
    return factory.getSchema(schemaContent);
  }

  private JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readTree(content);
  }

  @Test
  void customMessageIsNotOverwritten() throws Exception {
    Set<ValidationMessage> errors = validate();
    Map<String, String> errorMsgMap = transferErrorMsg(errors);
    Assertions.assertTrue(errorMsgMap.containsKey("$.toplevel[1].foos"), "error message must contains key: $.foos");
    Assertions.assertTrue(errorMsgMap.containsKey("$.toplevel[1].bars"), "error message must contains key: $.bars");
    Assertions.assertEquals("$.toplevel[1].foos: Must be a string with the a shape foofoofoofoo... with at least one foo", errorMsgMap.get("$.toplevel[1].foos"));
    Assertions.assertEquals("$.toplevel[1].bars: Must be a string with the a shape barbarbar... with at least one bar", errorMsgMap.get("$.toplevel[1].bars"));
  }


  private Set<ValidationMessage> validate() throws Exception {
    String schemaPath = "/schema/OverwritingCustomMessageBug.json";
    String dataPath = "/data/OverwritingCustomMessageBug.json";
    InputStream schemaInputStream = OverwritingCustomMessageBugTest.class.getResourceAsStream(schemaPath);
    JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);
    InputStream dataInputStream = OverwritingCustomMessageBugTest.class.getResourceAsStream(dataPath);
    JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
    return schema.validate(node);
  }

  private Map<String, String> transferErrorMsg(Set<ValidationMessage> validationMessages) {
    Map<String, String> pathToMessage = new HashMap<>();
    validationMessages.forEach(msg -> {
      pathToMessage.put(msg.getInstanceLocation().toString(), msg.getMessage());
    });
    return pathToMessage;
  }
}