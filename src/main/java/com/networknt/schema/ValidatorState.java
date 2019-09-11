package com.networknt.schema;

public class ValidatorState {
	/**
	 * Flag set when a node has matched Works in conjunction with the next flag:
	 * isComplexValidator, to be used for complex validators such as oneOf, for ex
	 */
	private boolean matchedNode = true;

	/**
	 * Flag set if complex validators such as oneOf, for ex, neeed to have their
	 * properties validated. The PropertiesValidator is not aware generally of a
	 * complex validator is being validated or a simple poperty tree
	 */
	private boolean isComplexValidator = false;

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
