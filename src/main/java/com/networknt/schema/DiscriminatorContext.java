package com.networknt.schema;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DiscriminatorContext {
    private final Map<String, ObjectNode> discriminators = new HashMap<>();

    private boolean discriminatorMatchFound = false;

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

    public boolean isDiscriminatorMatchFound() {
        return this.discriminatorMatchFound;
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