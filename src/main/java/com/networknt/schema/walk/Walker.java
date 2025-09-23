package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Validator;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;

public interface Walker {
    /**
     * This method gives the capability to walk through the given JsonNode, allowing
     * functionality beyond validation like collecting information,handling
     * cross-cutting concerns like logging or instrumentation. This method also
     * performs the validation if {@code shouldValidateSchema} is set to true. <br>
     * <br>
     * {@link Validator#walk(ExecutionContext, JsonNode, JsonNode, NodePath, boolean)}
     * provides a default implementation of this method. However, validators that
     * parse sub-schemas should override this method to call walk method on those
     * sub-schemas.
     * 
     * @param executionContext     the execution context
     * @param instanceNode         the instance node being processed
     * @param instance             the instance document that the instance node
     *                             belongs to
     * @param instanceLocation     the location of the instance node being processed
     * @param shouldValidateSchema true to validate the schema while walking
     */
    void walk(ExecutionContext executionContext, JsonNode instanceNode, JsonNode instance,
            NodePath instanceLocation, boolean shouldValidateSchema);
}
