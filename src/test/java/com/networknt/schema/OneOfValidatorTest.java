package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

public class OneOfValidatorTest {

    private static final SpecVersion.VersionFlag SCHEMA_VERSION = SpecVersion.VersionFlag.V201909;
    private static final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SCHEMA_VERSION);
    private static final ObjectMapper mapper = new ObjectMapper();


    public static final String ONE_OF_VALIDATOR = "src/test/resources/data/issue520";

    @Test
    public void validTest() throws IOException {
        String json = FileUtils.readFileToString(FileUtils.getFile(ONE_OF_VALIDATOR, "issue520-oneOfValidator-valid.json"), "UTF-8");
        String schema = FileUtils.readFileToString(FileUtils.getFile(ONE_OF_VALIDATOR, "issue520-oneOfValidator-schema.json"), "UTF-8");
        Set<ValidationMessage> validationMessages = factory.getSchema(schema).validate(mapper.readTree(json));
        System.out.println(validationMessages);
    }

    @Test
    public void invalid1Test() throws IOException {
        String json = FileUtils.readFileToString(FileUtils.getFile(ONE_OF_VALIDATOR, "issue520-oneOfValidator-invalid-1.json"), "UTF-8");
        String schema = FileUtils.readFileToString(FileUtils.getFile(ONE_OF_VALIDATOR, "issue520-oneOfValidator-schema.json"), "UTF-8");
        Set<ValidationMessage> validationMessages = factory.getSchema(schema).validate(mapper.readTree(json));
        System.out.println(validationMessages);
    }

    @Test
    public void invalid2Test() throws IOException {
        String json = FileUtils.readFileToString(FileUtils.getFile(ONE_OF_VALIDATOR, "issue520-oneOfValidator-invalid-2.json"), "UTF-8");
        String schema = FileUtils.readFileToString(FileUtils.getFile(ONE_OF_VALIDATOR, "issue520-oneOfValidator-schema.json"), "UTF-8");
        Set<ValidationMessage> validationMessages = factory.getSchema(schema).validate(mapper.readTree(json));
        System.out.println(validationMessages);
    }
}
