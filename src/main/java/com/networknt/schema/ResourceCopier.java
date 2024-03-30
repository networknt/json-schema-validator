package com.networknt.schema;



abstract class ResourceCopier {
    public abstract void copyResources(ValidationContext source, ValidationContext destination);
}

class SchemaResourcesCopier extends ResourceCopier {
    @Override
    public void copyResources(ValidationContext source, ValidationContext destination) {
        destination.getSchemaResources().putAll(source.getSchemaResources());
    }
}

class SchemaReferencesCopier extends ResourceCopier {
    @Override
    public void copyResources(ValidationContext source, ValidationContext destination) {
        destination.getSchemaReferences().putAll(source.getSchemaReferences());
    }
}

class DynamicAnchorsCopier extends ResourceCopier {
    @Override
    public void copyResources(ValidationContext source, ValidationContext destination) {
        destination.getDynamicAnchors().putAll(source.getDynamicAnchors());
    }
}
