/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
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
    
	/**
	 * 
	 * This method gives the capability to walk through the given JsonNode, allowing
	 * functionality beyond validation like collecting information,handling cross
	 * cutting concerns like logging or instrumentation. This method also performs
	 * the validation if {@code shouldValidateSchema} is set to true. <br>
	 * <br>
	 * {@link BaseJsonValidator#walk(JsonNode, JsonNode, String, List, boolean)}
	 * provides a default implementation of this method. However keywords that parse
	 * sub-schemas should override this method to call walk method on those
	 * subschemas.
	 * 
	 * @param node
	 * @param rootNode
	 * @param at
	 * @param jsonWalkListeners
	 * @param shouldValidateSchema
	 * @return
	 */
	Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema);

}
