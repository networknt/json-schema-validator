package com.networknt.schema;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class Issue366FailFast {

  @Before
  public void setup() throws IOException {
    setupSchema();
  }

  JsonSchema jsonSchema;
  ObjectMapper objectMapper = new ObjectMapper();
  private void setupSchema() throws IOException {

    SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
    schemaValidatorsConfig.setFailFast(true);
    JsonSchemaFactory schemaFactory = JsonSchemaFactory
        .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7))
        .objectMapper(objectMapper)
        .build();

    schemaValidatorsConfig.setTypeLoose(false);

    URI uri = getSchema();

    InputStream in = getClass().getResourceAsStream("/draft7/issue366_schema.json");
    JsonNode testCases = objectMapper.readValue(in, JsonNode.class);
    this.jsonSchema = schemaFactory.getSchema(uri, testCases,schemaValidatorsConfig);
  }

  protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(content);
    return node;
  }

  @Test
  public void firstOneValid() throws Exception {
    String dataPath = "/data/issue366.json";

    InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
    JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
    List<JsonNode> testNodes = node.findValues("tests");
    JsonNode testNode = testNodes.get(0).get(0);
    JsonNode dataNode = testNode.get("data");
    Set<ValidationMessage> errors = jsonSchema.validate(dataNode);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void secondOneValid() throws Exception {
    String dataPath = "/data/issue366.json";

    InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
    JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
    List<JsonNode> testNodes = node.findValues("tests");
    JsonNode testNode = testNodes.get(0).get(1);
    JsonNode dataNode = testNode.get("data");
    Set<ValidationMessage> errors = jsonSchema.validate(dataNode);
    assertTrue(errors.isEmpty());
  }

  @Test(expected = JsonSchemaException.class)
  public void bothValid() throws Exception {
    String dataPath = "/data/issue366.json";

    InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
    JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
    List<JsonNode> testNodes = node.findValues("tests");
    JsonNode testNode = testNodes.get(0).get(2);
    JsonNode dataNode = testNode.get("data");
    jsonSchema.validate(dataNode);
  }

  @Test(expected = JsonSchemaException.class)
  public void neitherValid() throws Exception {
    String dataPath = "/data/issue366.json";

    InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
    JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
    List<JsonNode> testNodes = node.findValues("tests");
    JsonNode testNode = testNodes.get(0).get(3);
    JsonNode dataNode = testNode.get("data");
    jsonSchema.validate(dataNode);
  }

  private URI getSchema() {
   return URI.create("classpath:" + "/draft7/issue366_schema.json");
  }
}
