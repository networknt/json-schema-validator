package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrnTest
{
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Validate that a JSON URI Mapping file containing the URI Mapping schema is
   * schema valid.
   *
   * @throws IOException if unable to parse the mapping file
   */
  @Test
  void testURNToURI() throws Exception {
    InputStream urlTestData = UrnTest.class.getResourceAsStream("/draft7/urn/test.json");
    InputStream is = null;
    try {
      is = new URL("https://raw.githubusercontent.com/francesc79/json-schema-validator/feature/urn-management/src/test/resources/draft7/urn/urn.schema.json").openStream();
      JsonMetaSchema draftV7 = JsonMetaSchema.getV7();
      JsonSchemaFactory.Builder builder = JsonSchemaFactory.builder()
          .defaultMetaSchemaIri(draftV7.getIri())
          .metaSchema(draftV7)
          .schemaMappers(schemaMappers -> schemaMappers.add(value -> AbsoluteIri.of(String.format("resource:draft7/urn/%s.schema.json", value.toString())))
          );
      JsonSchemaFactory instance = builder.build();
      JsonSchema schema = instance.getSchema(is);
      assertEquals(0, schema.validate(mapper.readTree(urlTestData)).size());
    } catch( Exception e) {
      e.printStackTrace();
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
  }
}
