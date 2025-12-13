package com.networknt.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import tools.jackson.databind.JsonNode;

/**
 * ErrorMessages.
 */
public class ErrorMessages {
    /**
     * Gets the custom error message to use.
     * 
     * @param parentSchema        the parent schema
     * @param errorMessageKeyword the error message keyword
     * @param keyword             the keyword
     * @return the custom error message
     */
    public static Map<String, String> getErrorMessage(Schema parentSchema, String errorMessageKeyword,
            String keyword) {
        final JsonNode message = getMessageNode(errorMessageKeyword, parentSchema.schemaNode, parentSchema, keyword);
        if (message != null) {
            JsonNode messageNode = message.get(keyword);
            if (messageNode != null) {
                if (messageNode.isString()) {
                    return Collections.singletonMap("", messageNode.asString());
                } else if (messageNode.isObject()) {
                    Map<String, String> result = new LinkedHashMap<>();
                    messageNode.properties().iterator()
                            .forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue().asString()));
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
        }
        return Collections.emptyMap();
    }

    protected static JsonNode getMessageNode(String errorMessageKeyword, JsonNode schemaNode, Schema parentSchema,
            String pname) {
        if (schemaNode.get(errorMessageKeyword) != null && schemaNode.get(errorMessageKeyword).get(pname) != null) {
            return schemaNode.get(errorMessageKeyword);
        }
        JsonNode messageNode;
        messageNode = schemaNode.get(errorMessageKeyword);
        if (messageNode == null && parentSchema != null) {
            messageNode = parentSchema.schemaNode.get(errorMessageKeyword);
            if (messageNode == null) {
                return getMessageNode(errorMessageKeyword, parentSchema.schemaNode, parentSchema.getParentSchema(),
                        pname);
            }
        }
        return messageNode;
    }
}
