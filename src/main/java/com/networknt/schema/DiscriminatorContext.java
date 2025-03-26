package com.networknt.schema;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DiscriminatorContext {
    private final Map<String, ObjectNode> discriminators = new HashMap<>();

    private boolean discriminatorMatchFound = false;

    private boolean discriminatorIgnore = false;

    public void registerDiscriminator(final SchemaLocation schemaLocation, final ObjectNode discriminator) {
        this.discriminators.put("#" + schemaLocation.getFragment().toString(), discriminator);
    }

    public ObjectNode getDiscriminatorForPath(final SchemaLocation schemaLocation) {
        return this.discriminators.get("#" + schemaLocation.getFragment().toString());
    }

    public ObjectNode getDiscriminatorForPath(final String schemaLocation) {
        return this.discriminators.get(schemaLocation);
    }

    public void markMatch() {
        this.discriminatorMatchFound = true;
    }

    /**
     * Indicate that discriminator processing should be ignored.
     * <p>
     * This is used when the discriminator property value is missing from the data.
     * <p>
     * See issue #436 for background.
     */
    public void markIgnore() {
        this.discriminatorIgnore = true;
    }

    public boolean isDiscriminatorMatchFound() {
        return this.discriminatorMatchFound;
    }

    public boolean isDiscriminatorIgnore() {
        return this.discriminatorIgnore;
    }

    /**
     * Returns true if we have a discriminator active. In this case no valid match in anyOf should lead to validation failure
     *
     * @return true in case there are discriminator candidates
     */
    public boolean isActive() {
        return !this.discriminators.isEmpty();
    }

    // Moved from BaseJsonValidator (non-static, uses instance methods)
    public void checkDiscriminatorMatch(final ObjectNode discriminator,
                                        final String discriminatorPropertyValue,
                                        final JsonSchema jsonSchema) {
        if (discriminatorPropertyValue == null) {
            this.markIgnore();
            return;
        }

        final JsonNode discriminatorMapping = discriminator.get("mapping");
        if (null == discriminatorMapping) {
            checkForImplicitDiscriminatorMappingMatch(discriminatorPropertyValue, jsonSchema);
        } else {
            checkForExplicitDiscriminatorMappingMatch(discriminatorPropertyValue, discriminatorMapping, jsonSchema);
            if (!this.isDiscriminatorMatchFound() && noExplicitDiscriminatorKeyOverride(discriminatorMapping, jsonSchema)) {
                checkForImplicitDiscriminatorMappingMatch(discriminatorPropertyValue, jsonSchema);
            }
        }
    }

    // Moved from BaseJsonValidator (non-static, uses instance methods)
    public void registerAndMergeDiscriminator(final ObjectNode discriminator,
                                              final JsonSchema schema,
                                              final JsonNodePath instanceLocation) {
        final JsonNode discriminatorOnSchema = schema.schemaNode.get("discriminator");
        if (null != discriminatorOnSchema && null != this.getDiscriminatorForPath(schema.schemaLocation)) {
            final JsonNode propertyName = discriminatorOnSchema.get("propertyName");
            if (null != propertyName) {
                throw new JsonSchemaException(instanceLocation + " schema " + schema + " attempts redefining the discriminator property");
            }
            final ObjectNode mappingOnContextDiscriminator = (ObjectNode) discriminator.get("mapping");
            final ObjectNode mappingOnCurrentSchemaDiscriminator = (ObjectNode) discriminatorOnSchema.get("mapping");
            if (null == mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                discriminator.set("mapping", discriminatorOnSchema);
            } else if (null != mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                final Iterator<Map.Entry<String, JsonNode>> fieldsToAdd = mappingOnCurrentSchemaDiscriminator.fields();
                while (fieldsToAdd.hasNext()) {
                    final Map.Entry<String, JsonNode> fieldToAdd = fieldsToAdd.next();
                    final String mappingKeyToAdd = fieldToAdd.getKey();
                    final JsonNode mappingValueToAdd = fieldToAdd.getValue();

                    final JsonNode currentMappingValue = mappingOnContextDiscriminator.get(mappingKeyToAdd);
                    if (null != currentMappingValue && currentMappingValue != mappingValueToAdd) {
                        throw new JsonSchemaException(instanceLocation + "discriminator mapping redefinition from " + mappingKeyToAdd
                                + "/" + currentMappingValue + " to " + mappingValueToAdd);
                    } else if (null == currentMappingValue) {
                        mappingOnContextDiscriminator.set(mappingKeyToAdd, mappingValueToAdd);
                    }
                }
            }
        }
        this.registerDiscriminator(schema.schemaLocation, discriminator);
    }

    // Helper methods moved from BaseJsonValidator (non-static)
    private void checkForImplicitDiscriminatorMappingMatch(final String discriminatorPropertyValue,
                                                           final JsonSchema schema) {
        if (schema.schemaLocation.getFragment().getName(-1).equals(discriminatorPropertyValue)) {
            this.markMatch();
        }
    }

    private void checkForExplicitDiscriminatorMappingMatch(final String discriminatorPropertyValue,
                                                           final JsonNode discriminatorMapping,
                                                           final JsonSchema schema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getKey().equals(discriminatorPropertyValue)
                    && ("#" + schema.schemaLocation.getFragment().toString()).equals(candidateExplicitMapping.getValue().asText())) {
                this.markMatch();
                break;
            }
        }
    }

    private boolean noExplicitDiscriminatorKeyOverride(final JsonNode discriminatorMapping,
                                                       final JsonSchema parentSchema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getValue().asText().equals(parentSchema.schemaLocation.getFragment().toString())) {
                return false;
            }
        }
        return true;
    }
}