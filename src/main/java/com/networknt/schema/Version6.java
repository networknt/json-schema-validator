package com.networknt.schema;

import java.util.Arrays;

/**
 * Draft 6 dialect.
 */
public class Version6 extends JsonSchemaVersion {
    private static final String IRI = SchemaId.V6;
    // Draft 6 uses "$id"
    private static final String ID = "$id";

    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(IRI)
                .specification(SpecVersion.VersionFlag.V6)
                .idKeyword(ID)
                .formats(Formats.DEFAULT)
                .keywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V6))
                // keywords that may validly exist, but have no validation aspect to them
                .keywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new AnnotationKeyword("title"),
                        new AnnotationKeyword("description"),
                        new AnnotationKeyword("default"),
                        new NonValidationKeyword("additionalItems"),
                        new NonValidationKeyword("definitions"),
                        new AnnotationKeyword("examples")
                ))
                .build();
    }
}
