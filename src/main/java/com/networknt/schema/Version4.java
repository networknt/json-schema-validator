package com.networknt.schema;

import java.util.Arrays;

/**
 * Draft 4 dialect.
 */
public class Version4 extends JsonSchemaVersion {
    private static final String IRI = SchemaId.V4;
    private static final String ID = "id";

    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(IRI)
                .specification(SpecVersion.VersionFlag.V4)
                .idKeyword(ID)
                .formats(Formats.DEFAULT)
                .keywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V4))
                // keywords that may validly exist, but have no validation aspect to them
                .keywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("id"),
                        new AnnotationKeyword("title"),
                        new AnnotationKeyword("description"),
                        new AnnotationKeyword("default"),
                        new NonValidationKeyword("definitions"),
                        new NonValidationKeyword("additionalItems"),
                        new AnnotationKeyword("exampleSetFlag")
                ))
                .build();
    }
}
