package com.networknt.schema.keyword;

/**
 * Discriminator state for an instance location.
 */
public class DiscriminatorState {
	private String propertyName;
	private String propertyValue;
	private String discriminatingValue = null;
	private boolean explicitMapping = false;
	private String match;
	
	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public String getDiscriminatingValue() {
		return discriminatingValue;
	}

	public void setDiscriminatingValue(String discriminatingValue) {
		this.discriminatingValue = discriminatingValue;
	}

	public boolean isExplicitMapping() {
		return explicitMapping;
	}

	public void setExplicitMapping(boolean explicitMapping) {
		this.explicitMapping = explicitMapping;
	}

	public void setMatch(String match) {
		this.match = match;
	}
	
	public String getMatch() {
		return this.match;
	}
}
