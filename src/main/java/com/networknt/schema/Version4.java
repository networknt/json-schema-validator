package com.networknt.schema;

import java.util.Arrays;

public class Version4 extends JsonSchemaVersion{
    private static final String URI = SchemaId.V4;
    private static final String ID = "id";

    static {
        // add version specific formats here.
        //BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
    }

    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(URI)
                .specification(SpecVersion.VersionFlag.V4)
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V4))
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
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
