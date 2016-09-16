package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class RefValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RefValidator.class);

    protected JsonSchema schema;

    public RefValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.REF);
        String refValue = schemaNode.asText();
        if (refValue.startsWith("#")) {
            // handle local $ref
            if (refValue.equals("#")) {
                schema = parentSchema.findAncestor();
            } else {

                JsonNode node = parentSchema.getRefSchemaNode(refValue);
                if (node != null) {
                    schema = new JsonSchema(mapper, refValue, node, parentSchema);
                }
            }
        } else {
            // handle remote ref
            int index = refValue.indexOf("#");
            String schemaUrl = refValue;
            if (index > 0) {
                schemaUrl = schemaUrl.substring(0, index);
            }
            JsonSchemaFactory factory = new JsonSchemaFactory(mapper);
            try {
                URL url = new URL(schemaUrl);
                parentSchema = factory.getSchema(url);
            } catch (MalformedURLException e) {
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaUrl);
                parentSchema = factory.getSchema(is);
            }
            if (index < 0) {
                schema = parentSchema.findAncestor();
            } else {
                refValue = refValue.substring(index);
                if (refValue.equals("#")) {
                    schema = parentSchema.findAncestor();
                } else {
                    JsonNode node = parentSchema.getRefSchemaNode(refValue);
                    if (node != null) {
                        schema = new JsonSchema(mapper, refValue, node, parentSchema);
                    }
                }
            }
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (schema != null) {
            return schema.validate(node, rootNode, at);
        } else {
            return new HashSet<ValidationMessage>();
        }
    }

}
