package com.networknt.schema;

import java.util.Arrays;

public class Version202012 extends JsonSchemaVersion {
    private static final String URI = "https://json-schema.org/draft/2020-12/schema";
    private static final String ID = "$id";

    static {
        // add version specific formats here.
        //BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
    }

    @Override
    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(URI)
                .specification(SpecVersion.VersionFlag.V202012)
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V202012))
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions"),
                        new NonValidationKeyword("$comment"),
                        new NonValidationKeyword("$defs"),
                        new NonValidationKeyword("$anchor"),
                        new NonValidationKeyword("$dynamicAnchor"),
                        new NonValidationKeyword("deprecated"),
                        new NonValidationKeyword("contentMediaType"),
                        new NonValidationKeyword("contentEncoding"),
                        new NonValidationKeyword("examples"),
                        new NonValidationKeyword("then"),
                        new NonValidationKeyword("else"),
                        new NonValidationKeyword("additionalItems")
                ))
                .vocabulary("https://json-schema.org/draft/2020-12/vocab/core")
                .vocabulary("https://json-schema.org/draft/2020-12/vocab/applicator")
                .vocabulary("https://json-schema.org/draft/2020-12/vocab/unevaluated")
                .vocabulary("https://json-schema.org/draft/2020-12/vocab/validation")
                .vocabulary("https://json-schema.org/draft/2020-12/vocab/meta-data")
                .vocabulary("https://json-schema.org/draft/2020-12/vocab/format-annotation")
                .vocabulary("https://json-schema.org/draft/2020-12/vocab/content")
                .build();
    }
}
