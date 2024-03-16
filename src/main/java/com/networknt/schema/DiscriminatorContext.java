package com.networknt.schema;

import java.util.HashMap;
import java.util.Map;

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
}