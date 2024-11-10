package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.serialization.JsonMapperFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract class to use if the data JSON has a declared schema node at root level
 *
 * @see Issue769ContainsTest
 * @author vwuilbea
 */
abstract class AbstractJsonSchemaTest {

    private static final String SCHEMA = "$schema";
    private static final SpecVersion.VersionFlag DEFAULT_VERSION_FLAG = SpecVersion.VersionFlag.V202012;
    private static final String ASSERT_MSG_ERROR_CODE = "Validation result should contain {0} error code";
    private static final String ASSERT_MSG_TYPE = "Validation result should contain {0} type";

    protected Set<ValidationMessage> validate(String dataPath) {
        JsonNode dataNode = getJsonNodeFromPath(dataPath);
        return getJsonSchemaFromDataNode(dataNode).validate(dataNode);
    }

    protected void assertValidatorType(String filename, ValidatorTypeCode validatorTypeCode) {
        Set<ValidationMessage> validationMessages = validate(getDataTestFolder() + filename);

        assertTrue(
                validationMessages.stream().anyMatch(vm -> validatorTypeCode.getErrorCode().equals(vm.getCode())),
                () -> MessageFormat.format(ASSERT_MSG_ERROR_CODE, validatorTypeCode.getErrorCode()));
        assertTrue(
                validationMessages.stream().anyMatch(vm -> validatorTypeCode.getValue().equals(vm.getType())),
                () -> MessageFormat.format(ASSERT_MSG_TYPE, validatorTypeCode.getValue()));
    }

    protected abstract String getDataTestFolder();

    private JsonSchema getJsonSchemaFromDataNode(JsonNode dataNode) {
        return Optional.ofNullable(dataNode.get(SCHEMA))
                .map(JsonNode::textValue)
                .map(this::getJsonNodeFromPath)
                .map(this::getJsonSchema)
                .orElseThrow(() -> new IllegalArgumentException("No schema found on document to test"));
    }

    private JsonNode getJsonNodeFromPath(String dataPath) {
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        ObjectMapper mapper = JsonMapperFactory.getInstance();
        try {
            return mapper.readTree(dataInputStream);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonSchema getJsonSchema(JsonNode schemaNode) {
        return JsonSchemaFactory
                .getInstance(SpecVersionDetector.detectOptionalVersion(schemaNode, false).orElse(DEFAULT_VERSION_FLAG))
                .getSchema(schemaNode);
    }

}
