/*
 * Copyright (c) 2025 the original author or authors.
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
     * Returns ture if the discriminating value is found in the payload
     * corresponding to the property name.
     * 
     * @return true if the discriminating value is found in the payload
     *         corresponding to the property name
     */
    public boolean hasDiscriminatingValue() {
        return this.discriminatingValue != null;
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

    /**
     * Gets whether the mapping is explicit using the mappings on the discriminator
     * keyword.
     * 
     * @return true if the mapping is explicitly mapped using mappings
     */
    public boolean isExplicitMapping() {
        return explicitMapping;
    }

    /**
     * Sets whether the mapping is explicit using the mappings on the discriminator
     * keyword.
     * 
     * @param explicitMapping true if explicitly mapped using mappings
     */
    public void setExplicitMapping(boolean explicitMapping) {
        this.explicitMapping = explicitMapping;
    }

    /**
     * Sets the matched schema $ref.
     * 
     * @param matchedSchema the matched schema $ref
     */
    public void setMatchedSchema(String matchedSchema) {
        this.matchedSchema = matchedSchema;
    }

    /**
     * Gets the matched schema $ref.
     * 
     * @return the matched schema $ref
     */
    public String getMatchedSchema() {
        return this.matchedSchema;
    }

    /**
     * Returns true if there was a schema that matched the discriminating value.
     * <p>
     * If the discriminating value does not match an implicit or explicit mapping,
     * no schema can be determined and validation SHOULD fail. Therefore if this
     * returns false validation should fail.
     * <p>
     * <a href="https://spec.openapis.org/oas/v3.1.2#examples-0">4.8.25.4
     * Examples</a>
     * 
     * @return true if there was a schema that matched the discriminating value.
     */
    public boolean hasMatchedSchema() {
        return this.matchedSchema != null;
    }

    
}
