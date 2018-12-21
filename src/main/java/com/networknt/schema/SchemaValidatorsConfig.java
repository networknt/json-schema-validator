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
