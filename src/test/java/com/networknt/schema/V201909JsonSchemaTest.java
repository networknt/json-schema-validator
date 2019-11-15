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
    public void testOptionalBignumValidator() throws Exception {
        runTestFile("draft2019-09/optional/bignum.json");
    }

    @Test
    @Ignore
    public void testOptionalContentValidator() throws Exception {
        runTestFile("draft2019-09/optional/content.json");
    }

    @Test
    @Ignore
    public void testEcmascriptRegexValidator() throws Exception {
        runTestFile("draft2019-09/optional/ecmascript-regex.json");
    }

    @Test
    @Ignore
    public void testZeroTerminatedFloatsValidator() throws Exception {
        runTestFile("draft2019-09/optional/zeroTerminatedFloats.json");
    }

    @Test
    public void testOptionalFormatDateValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/date.json");
    }

    @Test
    public void testOptionalFormatDateTimeValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/date-time.json");
    }

    @Test
    public void testOptionalFormatEmailValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/email.json");
    }

    @Test
    public void testOptionalFormatHostnameValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/hostname.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIdnEmailValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/idn-email.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIdnHostnameValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/idn-hostname.json");
    }

    @Test
    public void testOptionalFormatIpv4Validator() throws Exception {
        runTestFile("draft2019-09/optional/format/ipv4.json");
    }

    @Test
    public void testOptionalFormatIpv6Validator() throws Exception {
        runTestFile("draft2019-09/optional/format/ipv6.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIriValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/iri.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIriReferenceValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/iri-reference.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatJsonPointerValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/json-pointer.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatRegexValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/regex.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatRelativeJsonPointerValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/relative-json-pointer.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatTimeValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/time.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatUriValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/uri.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatUriReferenceValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/uri-reference.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatUriTemplateValidator() throws Exception {
        runTestFile("draft2019-09/optional/format/uri-template.json");
    }

    @Test
    public void testAdditionalItemsValidator() throws Exception {
        runTestFile("draft2019-09/additionalItems.json");
    }

    @Test
    public void testAdditionalPropertiesValidator() throws Exception {
        runTestFile("draft2019-09/additionalProperties.json");
    }

    @Test
    @Ignore
    public void testAllOfValidator() throws Exception {
        runTestFile("draft2019-09/allOf.json");
    }

    @Test
    @Ignore
    public void testAnchorValidator() throws Exception {
        runTestFile("draft2019-09/anchor.json");
    }

    @Test
    @Ignore
    public void testAnyOfValidator() throws Exception {
        runTestFile("draft2019-09/anyOf.json");
    }

    @Test
    @Ignore
    public void testBooleanSchemaValidator() throws Exception {
        runTestFile("draft2019-09/boolean_schema.json");
    }

    @Test
    @Ignore
    public void testConstValidator() throws Exception {
        runTestFile("draft2019-09/const.json");
    }

    @Test
    @Ignore
    public void testContainsValidator() throws Exception {
        runTestFile("draft2019-09/contains.json");
    }

    @Test
    public void testDefaultValidator() throws Exception {
        runTestFile("draft2019-09/default.json");
    }

    @Test
    @Ignore
    public void testDefsValidator() throws Exception {
        runTestFile("draft2019-09/defs.json");
    }

    @Test
    @Ignore
    public void testDependenciesValidator() throws Exception {
        runTestFile("draft2019-09/dependencies.json");
    }

    @Test
    @Ignore
    public void testEnumValidator() throws Exception {
        runTestFile("draft2019-09/enum.json");
    }

    @Test
    public void testExclusiveMaximumValidator() throws Exception {
        runTestFile("draft2019-09/exclusiveMaximum.json");
    }

    @Test
    public void testExclusiveMinimumValidator() throws Exception {
        runTestFile("draft2019-09/exclusiveMinimum.json");
    }

    @Test
    public void testFormatValidator() throws Exception {
        runTestFile("draft2019-09/format.json");
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

    @Test
    @Ignore
    public void testItemsValidator() throws Exception {
        runTestFile("draft2019-09/items.json");
    }

    @Test
    public void testMaximumValidator() throws Exception {
        runTestFile("draft2019-09/maximum.json");
    }

    @Test
    public void testMaxItemsValidator() throws Exception {
        runTestFile("draft2019-09/maxItems.json");
    }

    @Test
    public void testMaxLengthValidator() throws Exception {
        runTestFile("draft2019-09/maxLength.json");
    }

    @Test
    public void testMaxPropertiesValidator() throws Exception {
        runTestFile("draft2019-09/maxProperties.json");
    }

    @Test
    public void testMinimumValidator() throws Exception {
        runTestFile("draft2019-09/minimum.json");
    }

    @Test
    public void testMinItemsValidator() throws Exception {
        runTestFile("draft2019-09/minItems.json");
    }

    @Test
    public void testMinLengthValidator() throws Exception {
        runTestFile("draft2019-09/minLength.json");
    }

    @Test
    public void testMinPropertiesValidator() throws Exception {
        runTestFile("draft2019-09/minProperties.json");
    }

    @Test
    public void testMultipleOfValidator() throws Exception {
        runTestFile("draft2019-09/multipleOf.json");
    }

    @Test
    @Ignore
    public void testNotValidator() throws Exception {
        runTestFile("draft2019-09/not.json");
    }

    @Test
    @Ignore
    public void testOneOfValidator() throws Exception {
        runTestFile("draft2019-09/oneOf.json");
    }

    @Test
    public void testPatternValidator() throws Exception {
        runTestFile("draft2019-09/pattern.json");
    }

    @Test
    @Ignore
    public void testPatternPropertiesValidator() throws Exception {
        runTestFile("draft2019-09/patternProperties.json");
    }

    @Test
    @Ignore
    public void testPropertiesValidator() throws Exception {
        runTestFile("draft2019-09/properties.json");
    }

    @Test
    @Ignore
    public void testPropertyNamesValidator() throws Exception {
        runTestFile("draft2019-09/propertyNames.json");
    }

    @Test
    @Ignore
    public void testRefValidator() throws Exception {
        runTestFile("draft2019-09/ref.json");
    }

    @Test
    @Ignore
    public void testRefRemoteValidator() throws Exception {
        runTestFile("draft2019-09/refRemote.json");
    }

    @Test
    public void testRequiredValidator() throws Exception {
        runTestFile("draft2019-09/required.json");
    }

    @Test
    public void testTypeValidator() throws Exception {
        runTestFile("draft2019-09/type.json");
    }

    @Test
    public void testUniqueItemsValidator() throws Exception {
        runTestFile("draft2019-09/uniqueItems.json");
    }

}
