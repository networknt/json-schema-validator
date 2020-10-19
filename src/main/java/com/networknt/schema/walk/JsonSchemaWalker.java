package com.networknt.schema.walk;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.BaseJsonValidator;
import com.networknt.schema.ValidationMessage;

public interface JsonSchemaWalker {
	/**
	 * 
	 * This method gives the capability to walk through the given JsonNode, allowing
	 * functionality beyond validation like collecting information,handling cross
	 * cutting concerns like logging or instrumentation. This method also performs
	 * the validation if {@code shouldValidateSchema} is set to true. <br>
	 * <br>
	 * {@link BaseJsonValidator#walk(JsonNode, JsonNode, String, boolean)} provides
	 * a default implementation of this method. However keywords that parse
	 * sub-schemas should override this method to call walk method on those
	 * subschemas.
	 * 
	 * @param node                 JsonNode
	 * @param rootNode             JsonNode
	 * @param at                   String
	 * @param shouldValidateSchema boolean
	 * @return a set of validation messages if shouldValidateSchema is true.
	 */
	Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema);
}
