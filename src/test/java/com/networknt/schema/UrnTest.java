package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.uri.ClasspathURLFactory;
import com.networknt.schema.uri.URLFactory;
import com.networknt.schema.urn.URNFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrnTest
{
  private final ObjectMapper mapper = new ObjectMapper();
  private final ClasspathURLFactory classpathURLFactory = new ClasspathURLFactory();
  private final URLFactory urlFactory = new URLFactory();

  /**
   * Validate that a JSON URI Mapping file containing the URI Mapping schema is
   * schema valid.
   *
   * @throws IOException if unable to parse the mapping file
   */
  @Test
  public void testURNToURI() throws Exception {
    URL urlTestData = ClasspathURLFactory.convert(
        this.classpathURLFactory.create("resource:draft7/urn/test.json"));
    
    URNFactory urnFactory = new URNFactory()
    {
      @Override public URI create(String urn)
      {
        try {
          URL absoluteURL = ClasspathURLFactory.convert(new ClasspathURLFactory().create(String.format("resource:draft7/urn/%s.schema.json", urn)));
          return absoluteURL.toURI();
        } catch (Exception ex) {
          return null;
        }
      }
    };

    InputStream is = null;
    try {
      is = new URL("https://raw.githubusercontent.com/francesc79/json-schema-validator/feature/urn-management/src/test/resources/draft7/urn/urn.schema.json").openStream();
      JsonMetaSchema draftV7 = JsonMetaSchema.getV7();
      JsonSchemaFactory.Builder builder = JsonSchemaFactory.builder()
          .defaultMetaSchemaURI(draftV7.getUri())
          .addMetaSchema(draftV7)
          .addUrnFactory(urnFactory);
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
