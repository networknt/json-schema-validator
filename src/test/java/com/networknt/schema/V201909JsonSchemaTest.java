package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.FileResourceManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.undertow.Handlers.resource;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

public class V201909JsonSchemaTest {
    protected ObjectMapper mapper = new ObjectMapper();
    protected JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(mapper).build();
    protected static Undertow server = null;

    public V201909JsonSchemaTest() {
    }

    @BeforeClass
    public static void setUp() {
        if(server == null) {
            server = Undertow.builder()
                    .addHttpListener(1234, "localhost")
                    .setHandler(resource(new FileResourceManager(
                            new File("./src/test/resources/draft2019-09"), 100)))
                    .build();
            server.start();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if(server != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {

            }
            server.stop();
        }
    }

    private void runTestFile(String testCaseFile) throws Exception {
        final URI testCaseFileUri = URI.create("classpath:" + testCaseFile);
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(testCaseFile);
        ArrayNode testCases = mapper.readValue(in, ArrayNode.class);

        for (int j = 0; j < testCases.size(); j++) {
            try {
                JsonNode testCase = testCases.get(j);
                SchemaValidatorsConfig config = new SchemaValidatorsConfig();

                ArrayNode testNodes = (ArrayNode) testCase.get("tests");
                for (int i = 0; i < testNodes.size(); i++) {
                    JsonNode test = testNodes.get(i);
                    JsonNode node = test.get("data");
                    JsonNode typeLooseNode = test.get("isTypeLoose");
                    // Configure the schemaValidator to set typeLoose's value based on the test file,
                    // if test file do not contains typeLoose flag, use default value: true.
                    config.setTypeLoose((typeLooseNode == null) ? false : typeLooseNode.asBoolean());
                    JsonSchema schema = validatorFactory.getSchema(testCaseFileUri, testCase.get("schema"), config);
                    List<ValidationMessage> errors = new ArrayList<ValidationMessage>();

                    errors.addAll(schema.validate(node));

                    if (test.get("valid").asBoolean()) {
                        if (!errors.isEmpty()) {
                            System.out.println("---- test case failed ----");
                            System.out.println("schema: " + schema.toString());
                            System.out.println("data: " + test.get("data"));
                        }
                        assertEquals(0, errors.size());
                    } else {
                        if (errors.isEmpty()) {
                            System.out.println("---- test case failed ----");
                            System.out.println("schema: " + schema);
                            System.out.println("data: " + test.get("data"));
                        } else {
                            JsonNode errorCount = test.get("errorCount");
                            if (errorCount != null && errorCount.isInt() && errors.size() != errorCount.asInt()) {
                                System.out.println("---- test case failed ----");
                                System.out.println("schema: " + schema);
                                System.out.println("data: " + test.get("data"));
                                System.out.println("errors: " + errors);
                                assertEquals("expected error count", errorCount.asInt(), errors.size());
                            }
                        }
                        assertEquals(false, errors.isEmpty());
                    }
                }
            } catch (JsonSchemaException e) {
                throw new IllegalStateException(String.format("Current schema should not be invalid: %s", testCaseFile), e);
            }
        }
    }

    @Test
    public void testBignumValidator() throws Exception {
        runTestFile("draft2019-09/optional/bignum.json");
    }

    @Test
    public void testFormatDateValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/date.json");
    }

    @Test
    public void testFormatDateTimeValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/date-time.json");
    }

    @Test
    public void testFormatEmailValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/email.json");
    }

    @Test
    public void testFormatHostnameValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/hostname.json");
    }

    @Test
    @Ignore
    public void testFormatIdnEmailValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/idn-email.json");
    }

    @Test
    public void testIfValidator() throws Exception {
        runTestFile("draft2019-09/if.json");
    }

    @Test
    @Ignore
    public void testIfThenElseValidator() throws Exception {
        runTestFile("draft2019-09/if-then-else.json");
    }

}
