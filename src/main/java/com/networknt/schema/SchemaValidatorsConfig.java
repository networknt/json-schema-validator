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
     * if HAS_ELEMENT_VALIDATION_ERROR = true, the caller can decide, in conjunction with a missing node flag
     * on how to treat the error
     */
    private boolean elementValidationError = false;
    
    public boolean isTypeLoose() {
        return typeLoose;
    }

    public void setTypeLoose(boolean typeLoose) {
        this.typeLoose = typeLoose;
    }

    public boolean isMissingNodeAsError() {
    	return missingNodeAsError;
    }
    
    public void setMissingNodeAsError(boolean missingNodeAsError) {
    	this.missingNodeAsError = missingNodeAsError;
    }
    
    public boolean hasElementValidationError() {
    	return elementValidationError;
    }
    
    public void setElementValidationError(boolean elementValidationError) {
    	this.elementValidationError = elementValidationError;
    }
    
    public SchemaValidatorsConfig() {
        loadDefaultConfig();
    }

    private void loadDefaultConfig() {
        this.typeLoose = true;
    }
}
