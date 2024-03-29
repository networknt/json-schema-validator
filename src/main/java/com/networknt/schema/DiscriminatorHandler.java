package com.networknt.schema;

import java.util.Map;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DiscriminatorHandler {
 
    protected static void checkDiscriminatorMatch(final DiscriminatorContext currentDiscriminatorContext,
            final ObjectNode discriminator,
            final String discriminatorPropertyValue,
            final JsonSchema jsonSchema) {
        if (discriminatorPropertyValue == null) {
            currentDiscriminatorContext.markIgnore();
            return;
        }

        final JsonNode discriminatorMapping = discriminator.get("mapping");
        if (null == discriminatorMapping) {
            checkForImplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                    discriminatorPropertyValue,
                    jsonSchema);
        } else {
            checkForExplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                    discriminatorPropertyValue,
                    discriminatorMapping,
                    jsonSchema);
            if (!currentDiscriminatorContext.isDiscriminatorMatchFound()
                    && noExplicitDiscriminatorKeyOverride(discriminatorMapping, jsonSchema)) {
                checkForImplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                        discriminatorPropertyValue,
                        jsonSchema);
            }
        }
    }

 
    protected static void registerAndMergeDiscriminator(final DiscriminatorContext currentDiscriminatorContext,
            final ObjectNode discriminator,
            final JsonSchema schema,
            final JsonNodePath instanceLocation) {
        final JsonNode discriminatorOnSchema = schema.schemaNode.get("discriminator");
        if (null != discriminatorOnSchema && null != currentDiscriminatorContext
                .getDiscriminatorForPath(schema.schemaLocation)) {
            // This is where A -> B -> C inheritance exists, A has the root discriminator
            // and B adds to the mapping
            final JsonNode propertyName = discriminatorOnSchema.get("propertyName");
            if (null != propertyName) {
                throw new JsonSchemaException(
                        instanceLocation + " schema " + schema + " attempts redefining the discriminator property");
            }
            final ObjectNode mappingOnContextDiscriminator = (ObjectNode) discriminator.get("mapping");
            final ObjectNode mappingOnCurrentSchemaDiscriminator = (ObjectNode) discriminatorOnSchema.get("mapping");
            if (null == mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                // Here we have a mapping on a nested discriminator and none on the root
                // discriminator, so we can simply make it the root's
                discriminator.set("mapping", discriminatorOnSchema);
            } else if (null != mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                // Here we have to merge. The spec doesn't specify anything on this, but here we
                // don't accept redefinition of mappings that already exist
                final Iterator<Map.Entry<String, JsonNode>> fieldsToAdd = mappingOnCurrentSchemaDiscriminator.fields();
                while (fieldsToAdd.hasNext()) {
                    final Map.Entry<String, JsonNode> fieldToAdd = fieldsToAdd.next();
                    final String mappingKeyToAdd = fieldToAdd.getKey();
                    final JsonNode mappingValueToAdd = fieldToAdd.getValue();
        
                    final JsonNode currentMappingValue = mappingOnContextDiscriminator.get(mappingKeyToAdd);
                    if (null != currentMappingValue && currentMappingValue != mappingValueToAdd) {
                        throw new JsonSchemaException(
                                instanceLocation + "discriminator mapping redefinition from " + mappingKeyToAdd
                                        + "/" + currentMappingValue + " to " + mappingValueToAdd);
                    } else if (null == currentMappingValue) {
                        mappingOnContextDiscriminator.set(mappingKeyToAdd, mappingValueToAdd);
                    }
                }
            }
        }
        currentDiscriminatorContext.registerDiscriminator(schema.schemaLocation, discriminator);
    }


    protected static void checkForImplicitDiscriminatorMappingMatch(
            final DiscriminatorContext currentDiscriminatorContext,
            final String discriminatorPropertyValue,
            final JsonSchema schema) {
        if (schema.schemaLocation.getFragment().getName(-1).equals(discriminatorPropertyValue)) {
            currentDiscriminatorContext.markMatch();
        }
    }



    protected static void checkForExplicitDiscriminatorMappingMatch(
            final DiscriminatorContext currentDiscriminatorContext,
            final String discriminatorPropertyValue,
            final JsonNode discriminatorMapping,
            final JsonSchema schema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getKey().equals(discriminatorPropertyValue)
                    && ("#" + schema.schemaLocation.getFragment().toString())
                            .equals(candidateExplicitMapping.getValue().asText())) {
                currentDiscriminatorContext.markMatch();
                break;
            }
        }
    }


    protected static boolean noExplicitDiscriminatorKeyOverride(final JsonNode discriminatorMapping,
            final JsonSchema parentSchema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getValue().asText()
                    .equals(parentSchema.schemaLocation.getFragment().toString())) {
                return false;
            }
        }
        return true;
    }
}