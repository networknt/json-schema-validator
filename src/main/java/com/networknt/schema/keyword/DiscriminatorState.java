package com.networknt.schema.keyword;

/**
 * Discriminator state for an instance location.
 */
public class DiscriminatorState {
    private String propertyName;
    private String discriminatingValue;
    private String mappedSchema = null;
    private boolean explicitMapping = false;
    private String matchedSchema;

    /**
     * Gets the property name defined in the discriminator keyword schema.
     * 
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the property name defined in the discriminator keyword schema.
     * 
     * @param propertyName the property name
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets the discriminating value, which is the value in the payload
     * corresponding with the property name.
     * 
     * @return the discriminating value
     */
    public String getDiscriminatingValue() {
        return discriminatingValue;
    }

    /**
     * Sets the discriminating value, which is the value in the payload
     * corresponding with the property name.
     * 
     * @param discriminatingValue
     */
    public void setDiscriminatingValue(String discriminatingValue) {
        this.discriminatingValue = discriminatingValue;
    }

    /**
     * Gets the mapped schema which is the discriminating value mapped to the schema
     * name or uri.
     * 
     * @return the mapped schema
     */
    public String getMappedSchema() {
        return mappedSchema;
    }

    /**
     * Sets the mapped schema which is the discriminating value mapped to the schema
     * name or uri.
     * 
     * @param mappedSchema the mapped schema
     */
    public void setMappedSchema(String mappedSchema) {
        this.mappedSchema = mappedSchema;
    }

    public boolean isExplicitMapping() {
        return explicitMapping;
    }

    public void setExplicitMapping(boolean explicitMapping) {
        this.explicitMapping = explicitMapping;
    }

    public void setMatchedSchema(String matchedSchema) {
        this.matchedSchema = matchedSchema;
    }

    public String getMatchedSchema() {
        return this.matchedSchema;
    }
}
