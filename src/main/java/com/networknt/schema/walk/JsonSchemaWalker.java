package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.BaseJsonValidator;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public interface JsonSchemaWalker {
	/**
	 * 
	 * This method gives the capability to walk through the given JsonNode, allowing
	 * functionality beyond validation like collecting information,handling cross-cutting
	 * concerns like logging or instrumentation. This method also performs
	 * the validation if {@code shouldValidateSchema} is set to true. <br>
	 * <br>
	 * {@link BaseJsonValidator#walk(ExecutionContext, JsonNode, JsonNode, JsonNodePath, boolean)} provides
	 * a default implementation of this method. However, validators that parse
	 * sub-schemas should override this method to call walk method on those
	 * sub-schemas.
	 * 
	 * @param executionContext     ExecutionContext
	 * @param node                 JsonNode
	 * @param rootNode             JsonNode
	 * @param instanceLocation     JsonNodePath
	 * @param shouldValidateSchema boolean
	 * @return a set of validation messages if shouldValidateSchema is true.
	 */
    Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema);
}
