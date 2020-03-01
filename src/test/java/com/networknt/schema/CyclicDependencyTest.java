package com.networknt.schema;

import com.networknt.schema.impl.JsonValidator;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.ValidationException;

public class CyclicDependencyTest {


    @Test
    public void whenDependencyBetweenSchemaThenValidationSuccessful() {

        JsonValidator validator = new JsonValidator();
        String jsonObject = "{\n" +
                "  \"element\": {\n" +
                "    \"id\": \"top\",\n" +
                "    \"extension\": [\n" +
                "      {\n" +
                "        \"url\": \"http://inner.test\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"extension\": [\n" +
                "    {\n" +
                "      \"url\": \"http://top.test\",\n" +
                "      \"valueElement\": {\n" +
                "        \"id\": \"inner\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String jsonSchemaLocation = "/draft4/cyclic/Master";
        try {
            boolean validate = validator.validate(jsonObject, jsonSchemaLocation);
            Assert.assertTrue(validate);
        } catch (ValidationException e) {
            e.printStackTrace();
            Assert.fail("Failing while validating JSON: " + jsonObject +
                    " using schema from: " + jsonSchemaLocation +
                    " exception: " + e);
        }
    }


}
