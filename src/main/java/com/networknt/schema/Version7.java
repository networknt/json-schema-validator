package com.networknt.schema;

import java.util.Arrays;

/**
 * Draft 7 dialect.
 */
public class Version7 extends JsonSchemaVersion {
    private static final String IRI = SchemaId.V7;
    private static final String ID = "$id";

    @Override
    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(IRI)
                .specification(SpecVersion.VersionFlag.V7)
                .idKeyword(ID)
                .addFormats(Formats.DEFAULT)
                .addKeywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V7))
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new AnnotationKeyword("title"),
                        new AnnotationKeyword("description"),
                        new AnnotationKeyword("default"),
                        new NonValidationKeyword("definitions"),
                        new NonValidationKeyword("$comment"),
                        new AnnotationKeyword("examples"),
                        new NonValidationKeyword("then"),
                        new NonValidationKeyword("else"),
                        new NonValidationKeyword("additionalItems")
                ))
                .build();
    }
}
