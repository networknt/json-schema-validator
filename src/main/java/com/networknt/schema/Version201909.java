package com.networknt.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Version201909 extends JsonSchemaVersion{
    private static final String URI = SchemaId.V201909;
    private static final String ID = "$id";
    private static final Map<String, Boolean> VOCABULARY;

    static {
        Map<String, Boolean> vocabulary = new HashMap<>();
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/core", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/applicator", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/validation", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/meta-data", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/format", false);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/content", true);
        VOCABULARY = vocabulary;
    }

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
                        new NonValidationKeyword("$recursiveAnchor", false),
                        new NonValidationKeyword("$schema", false),
                        new NonValidationKeyword("$vocabulary", false),
                        new NonValidationKeyword("$id", false),
                        new NonValidationKeyword("title"),
                        new NonValidationKeyword("description"),
                        new NonValidationKeyword("default"),
                        new NonValidationKeyword("definitions", false),
                        new NonValidationKeyword("$comment"),
                        new NonValidationKeyword("$defs", false),  // newly added in 2019-09 release.
                        new NonValidationKeyword("$anchor", false),
                        new NonValidationKeyword("additionalItems"),
                        new NonValidationKeyword("deprecated"),
                        new NonValidationKeyword("contentMediaType"),
                        new NonValidationKeyword("contentEncoding"),
                        new NonValidationKeyword("contentSchema"),
                        new NonValidationKeyword("examples"),
                        new NonValidationKeyword("then"),
                        new NonValidationKeyword("else")
                ))
                .vocabularies(VOCABULARY)
                .build();
    }
}
