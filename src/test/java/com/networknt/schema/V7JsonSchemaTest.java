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
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static io.undertow.Handlers.resource;
import static org.junit.Assert.assertEquals;

public class V7JsonSchemaTest {
    protected ObjectMapper mapper = new ObjectMapper();
    protected JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(mapper).build();
    protected static Undertow server = null;

    public V7JsonSchemaTest() {
    }

    @BeforeClass
    public static void setUp() {
        if(server == null) {
            server = Undertow.builder()
                    .addHttpListener(1234, "localhost")
                    .setHandler(resource(new FileResourceManager(
                            new File("./src/test/resources/remotes"), 100)))
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
        runTestFile("draft7/optional/bignum.json");
    }

    @Test
    @Ignore
    public void testOptionalContentValidator() throws Exception {
        runTestFile("draft7/optional/content.json");
    }

    @Test
    @Ignore
    public void testEcmascriptRegexValidator() throws Exception {
        runTestFile("draft7/optional/ecmascript-regex.json");
    }

    @Test
    @Ignore
    public void testZeroTerminatedFloatsValidator() throws Exception {
        runTestFile("draft7/optional/zeroTerminatedFloats.json");
    }

    @Test
    public void testOptionalFormatDateValidator() throws Exception {
        runTestFile("draft7/optional/format/date.json");
    }

    @Test
    public void testOptionalFormatDateTimeValidator() throws Exception {
        runTestFile("draft7/optional/format/date-time.json");
    }

    @Test
    public void testOptionalFormatEmailValidator() throws Exception {
        runTestFile("draft7/optional/format/email.json");
    }

    @Test
    public void testOptionalFormatHostnameValidator() throws Exception {
        runTestFile("draft7/optional/format/hostname.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIdnEmailValidator() throws Exception {
        runTestFile("draft7/optional/format/idn-email.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIdnHostnameValidator() throws Exception {
        runTestFile("draft7/optional/format/idn-hostname.json");
    }

    @Test
    public void testOptionalFormatIpv4Validator() throws Exception {
        runTestFile("draft7/optional/format/ipv4.json");
    }

    @Test
    public void testOptionalFormatIpv6Validator() throws Exception {
        runTestFile("draft7/optional/format/ipv6.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIriValidator() throws Exception {
        runTestFile("draft7/optional/format/iri.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatIriReferenceValidator() throws Exception {
        runTestFile("draft7/optional/format/iri-reference.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatJsonPointerValidator() throws Exception {
        runTestFile("draft7/optional/format/json-pointer.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatRegexValidator() throws Exception {
        runTestFile("draft7/optional/format/regex.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatRelativeJsonPointerValidator() throws Exception {
        runTestFile("draft7/optional/format/relative-json-pointer.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatTimeValidator() throws Exception {
        runTestFile("draft7/optional/format/time.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatUriValidator() throws Exception {
        runTestFile("draft7/optional/format/uri.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatUriReferenceValidator() throws Exception {
        runTestFile("draft7/optional/format/uri-reference.json");
    }

    @Test
    @Ignore
    public void testOptionalFormatUriTemplateValidator() throws Exception {
        runTestFile("draft7/optional/format/uri-template.json");
    }

    @Test
    public void testAdditionalItemsValidator() throws Exception {
        runTestFile("draft7/additionalItems.json");
    }

    @Test
    public void testAdditionalPropertiesValidator() throws Exception {
        runTestFile("draft7/additionalProperties.json");
    }

    @Test
    public void testAllOfValidator() throws Exception {
        runTestFile("draft7/allOf.json");
    }

    @Test
    public void testAnyOfValidator() throws Exception {
        runTestFile("draft7/anyOf.json");
    }

    @Test
    public void testBooleanSchemaValidator() throws Exception {
        runTestFile("draft7/boolean_schema.json");
    }

    @Test
    public void testConstValidator() throws Exception {
        runTestFile("draft7/const.json");
    }

    @Test
    public void testContainsValidator() throws Exception {
        runTestFile("draft7/contains.json");
    }

    @Test
    public void testDefaultValidator() throws Exception {
        runTestFile("draft7/default.json");
    }

    @Test
    public void testDefsValidator() throws Exception {
        runTestFile("draft7/definitions.json");
    }

    @Test
    public void testDependenciesValidator() throws Exception {
        runTestFile("draft7/dependencies.json");
    }

    @Test
    public void testEnumValidator() throws Exception {
        runTestFile("draft7/enum.json");
    }

    @Test
    public void testExclusiveMaximumValidator() throws Exception {
        runTestFile("draft7/exclusiveMaximum.json");
    }

    @Test
    public void testExclusiveMinimumValidator() throws Exception {
        runTestFile("draft7/exclusiveMinimum.json");
    }

    @Test
    public void testFormatValidator() throws Exception {
        runTestFile("draft7/format.json");
    }

    @Test
    public void testIfThenElseValidator() throws Exception {
        runTestFile("draft7/if-then-else.json");
    }

    @Test
    public void testItemsValidator() throws Exception {
        runTestFile("draft7/items.json");
    }

    @Test
    public void testMaximumValidator() throws Exception {
        runTestFile("draft7/maximum.json");
    }

    @Test
    public void testMaxItemsValidator() throws Exception {
        runTestFile("draft7/maxItems.json");
    }

    @Test
    public void testMaxLengthValidator() throws Exception {
        runTestFile("draft7/maxLength.json");
    }

    @Test
    public void testMaxPropertiesValidator() throws Exception {
        runTestFile("draft7/maxProperties.json");
    }

    @Test
    public void testMinimumValidator() throws Exception {
        runTestFile("draft7/minimum.json");
    }

    @Test
    public void testMinItemsValidator() throws Exception {
        runTestFile("draft7/minItems.json");
    }

    @Test
    public void testMinLengthValidator() throws Exception {
        runTestFile("draft7/minLength.json");
    }

    @Test
    public void testMinPropertiesValidator() throws Exception {
        runTestFile("draft7/minProperties.json");
    }

    @Test
    public void testMultipleOfValidator() throws Exception {
        runTestFile("draft7/multipleOf.json");
    }

    @Test
    public void testNotValidator() throws Exception {
        runTestFile("draft7/not.json");
    }

    @Test
    public void testOneOfValidator() throws Exception {
        runTestFile("draft7/oneOf.json");
    }

    @Test
    public void testPatternValidator() throws Exception {
        runTestFile("draft7/pattern.json");
    }

    @Test
    public void testPatternPropertiesValidator() throws Exception {
        runTestFile("draft7/patternProperties.json");
    }

    @Test
    public void testPropertiesValidator() throws Exception {
        runTestFile("draft7/properties.json");
    }

    @Test
    @Ignore
    public void testPropertyNamesValidator() throws Exception {
        runTestFile("draft7/propertyNames.json");
    }

    @Test
    @Ignore
    public void testRefValidator() throws Exception {
        runTestFile("draft7/ref.json");
    }

    @Test
    public void testRefRemoteValidator() throws Exception {
        runTestFile("draft7/refRemote.json");
    }

    @Test
    public void testRefIdReference() throws Exception {
        runTestFile("draft7/idRef.json");
    }

    @Test
    @Ignore
    public void testRefRemoteValidator_Ignored() throws Exception {
        runTestFile("draft7/refRemote_ignored.json");
    }

    @Test
    public void testRequiredValidator() throws Exception {
        runTestFile("draft7/required.json");
    }

    @Test
    public void testTypeValidator() throws Exception {
        runTestFile("draft7/type.json");
    }

    @Test
    public void testUniqueItemsValidator() throws Exception {
        runTestFile("draft7/uniqueItems.json");
    }

    @Test
    public void testMultipleOfScale() throws Exception {
        runTestFile("multipleOfScale.json");
    }

}
