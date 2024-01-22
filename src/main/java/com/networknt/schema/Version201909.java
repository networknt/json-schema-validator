package com.networknt.schema;

import java.util.Arrays;

public class Version201909 extends JsonSchemaVersion{
    private static final String URI = "https://json-schema.org/draft/2019-09/schema";
    private static final String ID = "$id";

    static {
        // add version specific formats here.
        //BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
    }
    @Override
    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(URI)
                .specification(SpecVersion.VersionFlag.V201909)
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V201909))
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("$recursiveAnchor"),
                        new NonValidationKeyword("$schema"),
                        new NonValidationKeyword("$vocabulary"),
                        new NonValidationKeyword("$id"),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions"),
                        new NonValidationKeyword("$comment"),
                        new NonValidationKeyword("$defs"),  // newly added in 2019-09 release.
                        new NonValidationKeyword("$anchor"),
                        new NonValidationKeyword("additionalItems"),
                        new NonValidationKeyword("deprecated"),
                        new NonValidationKeyword("contentMediaType"),
                        new NonValidationKeyword("contentEncoding"),
                        new NonValidationKeyword("examples"),
                        new NonValidationKeyword("then"),
                        new NonValidationKeyword("else")
                ))
                .vocabulary("https://json-schema.org/draft/2019-09/vocab/core")
                .vocabulary("https://json-schema.org/draft/2019-09/vocab/applicator")
                .vocabulary("https://json-schema.org/draft/2019-09/vocab/validation")
                .vocabulary("https://json-schema.org/draft/2019-09/vocab/meta-data")
                .vocabulary("https://json-schema.org/draft/2019-09/vocab/format", false)
                .vocabulary("https://json-schema.org/draft/2019-09/vocab/content")
                .build();
    }
}
