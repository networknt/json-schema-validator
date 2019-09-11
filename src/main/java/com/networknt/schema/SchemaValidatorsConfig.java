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

import java.util.HashMap;
import java.util.Map;

public class SchemaValidatorsConfig {
    /**
     * when validate type, if TYPE_LOOSE = true, will try to convert string to different types to match the type defined in schema.
     */
    private boolean typeLoose;
    
    /**
     * Flag set when a node has matched
     * Works in conjunction with the next flag: isComplexValidator, to be used for complex validators such as oneOf, for ex
     */
    private boolean matchedNode = true;
    
    /**
     * Flag set if complex validators such as oneOf, for ex, neeed to have their properties validated.
     * The PropertiesValidator is not aware generally of a complex validator is being validated or a simple poperty tree
     */
    private boolean isComplexValidator = false;
    
    /**
     * Map of public, normally internet accessible schema URLs to alternate locations; this allows for offline
     * validation of schemas that refer to public URLs. This is merged with any mappings the {@link JsonSchemaFactory} 
     * may have been built with.
     */
    private Map<String, String> uriMappings = new HashMap<String, String>();

    /**
     * When a field is set as nullable in the OpenAPI specification, the schema validator validates that it is nullable
     * however continues with validation against the nullable field
     * 
     * If handleNullableField is set to true && incoming field is nullable && value is field: null --> succeed
     * If handleNullableField is set to false && incoming field is nullable && value is field: null --> it is up to the type 
     * validator using the SchemaValidator to handle it.
     */
    private boolean handleNullableField = true;
    
    public boolean isTypeLoose() {
        return typeLoose;
    }

    public void setTypeLoose(boolean typeLoose) {
        this.typeLoose = typeLoose;
    }

    public Map<String, String> getUriMappings() {
        // return a copy of the mappings
        return new HashMap<String, String>(uriMappings);
    }

    public void setUriMappings(Map<String, String> uriMappings) {
        this.uriMappings = uriMappings;
    }
    
    public boolean isHandleNullableField() {
		return handleNullableField;
	}

	public void setHandleNullableField(boolean handleNullableField) {
		this.handleNullableField = handleNullableField;
	}

	public SchemaValidatorsConfig() {
        loadDefaultConfig();
    }

    private void loadDefaultConfig() {
        this.typeLoose = true;
        this.uriMappings = new HashMap<String, String>();
    }
    
    public void setMatchedNode(boolean matchedNode) {
    	this.matchedNode = matchedNode;
    }
    public boolean hasMatchedNode() {
    	return matchedNode;
    }

	public boolean isComplexValidator() {
		return isComplexValidator;
	}

	public void setComplexValidator(boolean isComplexValidator) {
		this.isComplexValidator = isComplexValidator;
	}
}
