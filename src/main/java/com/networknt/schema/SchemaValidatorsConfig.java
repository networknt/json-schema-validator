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
     * if IS_MISSING_NODE_AS_ERROR = true, the validator will ignore the missing node.
     * if set to false, then the validator will report an error
     */
    private boolean missingNodeAsError = false;

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
    
    public boolean isMissingNodeAsError() {
    	return missingNodeAsError;
    }
    
    public void setMissingNodeAsError(boolean missingNodeAsError) {
    	this.missingNodeAsError = missingNodeAsError;
    }

    public boolean getHandleNullableField() {
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
}
