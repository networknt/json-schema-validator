package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

/**
 * Standard json validator interface, implemented by all validators and JsonSchema.
 */
public interface JsonValidator {
    public static final String AT_ROOT = "$";

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     *
     * @param rootNode JsonNode
     * @return A list of ValidationMessage if there is any validation error, or an empty
     * list if there is no error.
     */
    Set<ValidationMessage> validate(JsonNode rootNode);

    /**
     * Validate the given JsonNode, the given node is the child node of the root node at given
     * data path.
     *
     * @param node     JsonNode
     * @param rootNode JsonNode
     * @param at       String
     * @return A list of ValidationMessage if there is any validation error, or an empty
     * list if there is no error.
     */
    Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at);

}
