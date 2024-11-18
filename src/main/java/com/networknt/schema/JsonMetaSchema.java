/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a meta-schema which is uniquely identified by its IRI.
 */
public class JsonMetaSchema {
    private static final Logger logger = LoggerFactory.getLogger(JsonMetaSchema.class);

    /**
     * Factory for creating a format keyword.
     */
    public interface FormatKeywordFactory {
        /**
         * Creates a format keyword.
         * 
         * @param formats the formats
         * @return the format keyword
         */
        FormatKeyword newInstance(Map<String, Format> formats);
    }

    /**
     * Builder for {@link JsonMetaSchema}. 
     */
    public static class Builder {
        private String iri;
        private String idKeyword = "$id";
        private VersionFlag specification = null;
        private final Map<String, Keyword> keywords = new HashMap<>();
        private final Map<String, Format> formats = new HashMap<>();
        private final Map<String, Boolean> vocabularies = new HashMap<>();
        private FormatKeywordFactory formatKeywordFactory = null;
        private VocabularyFactory vocabularyFactory = null;
        private KeywordFactory unknownKeywordFactory = null;

        public Builder(String iri) {
            this.iri = iri;
        }

        private Map<String, Keyword> createKeywordsMap(Map<String, Keyword> kwords, Map<String, Format> formats) {
            boolean formatKeywordPresent = false;
            Map<String, Keyword> map = new HashMap<>();
            for (Map.Entry<String, Keyword> type : kwords.entrySet()) {
                String keywordName = type.getKey();
                Keyword keyword = type.getValue();
                if (ValidatorTypeCode.FORMAT.getValue().equals(keywordName)) {
                    if (!(keyword instanceof FormatKeyword) && !ValidatorTypeCode.FORMAT.equals(keyword)) {
                        throw new IllegalArgumentException("Overriding the keyword 'format' is not supported. Use the formatKeywordFactory and extend the FormatKeyword.");
                    }
                    // Indicate that the format keyword needs to be created
                    formatKeywordPresent = true;
                } else {
                    map.put(keyword.getValue(), keyword);
                }
            }
            if (formatKeywordPresent) {
                final FormatKeyword formatKeyword = formatKeywordFactory != null ? formatKeywordFactory.newInstance(formats)
                        : new FormatKeyword(formats);
                map.put(formatKeyword.getValue(), formatKeyword);
            }
            return map;
        }

        /**
         * Sets the format keyword factory.
         * 
         * @param formatKeywordFactory the format keyword factory
         * @return the builder
         */
        public Builder formatKeywordFactory(FormatKeywordFactory formatKeywordFactory) {
            this.formatKeywordFactory = formatKeywordFactory;
            return this;
        }

        /**
         * Sets the vocabulary factory for handling custom vocabularies.
         * 
         * @param vocabularyFactory the factory
         * @return the builder
         */
        public Builder vocabularyFactory(VocabularyFactory vocabularyFactory) {
            this.vocabularyFactory = vocabularyFactory;
            return this;
        }

        /**
         * Sets the keyword factory for handling unknown keywords.
         * 
         * @param unknownKeywordFactory the factory
         * @return the builder
         */
        public Builder unknownKeywordFactory(KeywordFactory unknownKeywordFactory) {
            this.unknownKeywordFactory = unknownKeywordFactory;
            return this;
        }

        /**
         * Customize the formats. 
         * 
         * @param customizer the customizer
         * @return the builder
         */
        public Builder formats(Consumer<Map<String, Format>> customizer) {
            customizer.accept(this.formats);
            return this;
        }

        /**
         * Customize the keywords.
         * 
         * @param customizer the customizer
         * @return the builder
         */
        public Builder keywords(Consumer<Map<String, Keyword>> customizer) {
            customizer.accept(this.keywords);
            return this;
        }

        /**
         * Adds the keyword.
         * 
         * @param keyword the keyword
         * @return the builder
         */
        public Builder keyword(Keyword keyword) {
            this.keywords.put(keyword.getValue(), keyword);
            return this;
        }

        /**
         * Adds the keywords.
         * 
         * @param keywords the keywords
         * @return the builder
         */
        public Builder keywords(Collection<? extends Keyword> keywords) {
            for (Keyword keyword : keywords) {
                this.keywords.put(keyword.getValue(), keyword);
            }
            return this;
        }

        /**
         * Adds the format.
         * 
         * @param format the format
         * @return the builder
         */
        public Builder format(Format format) {
            this.formats.put(format.getName(), format);
            return this;
        }

        /**
         * Adds the formats.
         * 
         * @param formats the formats
         * @return the builder
         */
        public Builder formats(Collection<? extends Format> formats) {
            for (Format format : formats) {
                format(format);
            }
            return this;
        }

        /**
         * Adds a required vocabulary.
         * <p>
         * Note that an error will be raised if this vocabulary is unknown.
         * 
         * @param vocabulary the vocabulary IRI
         * @return the builder
         */
        public Builder vocabulary(String vocabulary) {
            return vocabulary(vocabulary, true);
        }

        /**
         * Adds a vocabulary.
         * 
         * @param vocabulary the vocabulary IRI
         * @param required   true indicates if the vocabulary is not recognized
         *                   processing should stop
         * @return the builder
         */
        public Builder vocabulary(String vocabulary, boolean required) {
            this.vocabularies.put(vocabulary, required);
            return this;
        }

        /**
         * Adds the vocabularies.
         * 
         * @param vocabularies the vocabularies to add
         * @return the builder
         */
        public Builder vocabularies(Map<String, Boolean> vocabularies) {
            this.vocabularies.putAll(vocabularies);
            return this;
        }

        /**
         * Customize the vocabularies.
         *
         * @param customizer the customizer
         * @return the builder
         */
        public Builder vocabularies(Consumer<Map<String, Boolean>> customizer) {
            customizer.accept(this.vocabularies);
            return this;
        }

        /**
         * Sets the specification.
         * 
         * @param specification the specification
         * @return the builder
         */
        public Builder specification(VersionFlag specification) {
            this.specification = specification;
            return this;
        }

        /**
         * Sets the id keyword.
         * 
         * @param idKeyword the id keyword
         * @return the builder
         */
        public Builder idKeyword(String idKeyword) {
            this.idKeyword = idKeyword;
            return this;
        }

        public JsonMetaSchema build() {
            // create builtin keywords with (custom) formats.
            Map<String, Keyword> keywords = this.keywords;
            if (this.specification != null) {
                if (this.specification.getVersionFlagValue() >= SpecVersion.VersionFlag.V201909.getVersionFlagValue()) {
                    keywords = new HashMap<>(this.keywords);
                    for(Entry<String, Boolean> entry : this.vocabularies.entrySet()) {
                        Vocabulary vocabulary = null;
                        String id = entry.getKey();
                        if (this.vocabularyFactory != null) {
                            vocabulary = this.vocabularyFactory.getVocabulary(id);
                        }
                        if (vocabulary == null) {
                            vocabulary = Vocabularies.getVocabulary(id);
                        }
                        if (vocabulary != null) {
                            for (Keyword keyword : vocabulary.getKeywords()) {
                                keywords.put(keyword.getValue(), keyword);
                            }
                        } else if (Boolean.TRUE.equals(entry.getValue())) {
                            ValidationMessage validationMessage = ValidationMessage.builder()
                                    .message("Meta-schema ''{1}'' has unknown required vocabulary ''{2}''")
                                    .arguments(this.iri, id).build();
                            throw new InvalidSchemaException(validationMessage);
                        }
                    }
                }
            }
            Map<String, Keyword> result = createKeywordsMap(keywords, this.formats);
            return new JsonMetaSchema(this.iri, this.idKeyword, result, this.vocabularies, this.specification, this);
        }

        @Deprecated
        public Builder addKeyword(Keyword keyword) {
            return keyword(keyword);
        }

        @Deprecated
        public Builder addKeywords(Collection<? extends Keyword> keywords) {
            return keywords(keywords);
        }

        @Deprecated
        public Builder addFormat(Format format) {
            return format(format);
        }

        @Deprecated
        public Builder addFormats(Collection<? extends Format> formats) {
            return formats(formats);
        }
    }

    private final String iri;
    private final String idKeyword;
    private final Map<String, Keyword> keywords;
    private final Map<String, Boolean> vocabularies;
    private final VersionFlag specification;

    private final Builder builder;

    JsonMetaSchema(String iri, String idKeyword, Map<String, Keyword> keywords, Map<String, Boolean> vocabularies, VersionFlag specification, Builder builder) {
        if (StringUtils.isBlank(iri)) {
            throw new IllegalArgumentException("iri must not be null or blank");
        }
        if (StringUtils.isBlank(idKeyword)) {
            throw new IllegalArgumentException("idKeyword must not be null or blank");
        }
        if (keywords == null) {
            throw new IllegalArgumentException("keywords must not be null ");
        }

        this.iri = iri;
        this.idKeyword = idKeyword;
        this.keywords = keywords;
        this.specification = specification;
        this.vocabularies = vocabularies;
        this.builder = builder;
    }

    public static JsonMetaSchema getV4() {
        return new Version4().getInstance();
    }

    public static JsonMetaSchema getV6() {
        return new Version6().getInstance();
    }

    public static JsonMetaSchema getV7() {
        return new Version7().getInstance();
    }

    public static JsonMetaSchema getV201909() {
        return new Version201909().getInstance();
    }

    public static JsonMetaSchema getV202012() {
        return new Version202012().getInstance();
    }

    /**
     * Create a builder without keywords or formats.
     * <p>
     * Use {@link #getV4()} for the Draft 4 Metaschema, or if you need a builder based on Draft4, use
     *
     * <code>
     * JsonMetaSchema.builder("http://your-metaschema-iri", JsonMetaSchema.getV4()).build();
     * </code>
     *
     * @param iri the IRI of the metaschema that will be defined via this builder.
     * @return a builder instance without any keywords or formats - usually not what one needs.
     */
    public static Builder builder(String iri) {
        return new Builder(iri);
    }

    /**
     * Create a builder.
     * 
     * @param iri       the IRI of your new JsonMetaSchema that will be defined via
     *                  this builder.
     * @param blueprint the JsonMetaSchema to base your custom JsonMetaSchema on.
     * @return a builder instance preconfigured to be the same as blueprint, but
     *         with a different uri.
     */
    public static Builder builder(String iri, JsonMetaSchema blueprint) {
        Builder builder = builder(blueprint);
        builder.iri = iri;
        return builder;
    }

    /**
     * Create a builder.
     * 
     * @param blueprint the JsonMetaSchema to base your custom JsonMetaSchema on.
     * @return a builder instance preconfigured to be the same as blueprint
     */
    public static Builder builder(JsonMetaSchema blueprint) {
        Map<String, Boolean> vocabularies = new HashMap<>(blueprint.getVocabularies());
        return builder(blueprint.getIri())
                .idKeyword(blueprint.idKeyword)
                .keywords(blueprint.builder.keywords.values())
                .formats(blueprint.builder.formats.values())
                .specification(blueprint.getSpecification())
                .vocabularies(vocabularies)
                .vocabularyFactory(blueprint.builder.vocabularyFactory)
                .formatKeywordFactory(blueprint.builder.formatKeywordFactory)
                .unknownKeywordFactory(blueprint.builder.unknownKeywordFactory)
                ;
    }

    public String getIdKeyword() {
        return this.idKeyword;
    }

    public String readId(JsonNode schemaNode) {
        return readText(schemaNode, this.idKeyword);
    }

    public String readAnchor(JsonNode schemaNode) {
        boolean supportsAnchor = this.keywords.containsKey("$anchor");
        if (supportsAnchor) {
            return readText(schemaNode, "$anchor");
        }
        return null;
    }

    public String readDynamicAnchor(JsonNode schemaNode) {
        boolean supportsDynamicAnchor = this.keywords.containsKey("$dynamicAnchor");
        if (supportsDynamicAnchor) {
            return readText(schemaNode, "$dynamicAnchor");
        }
        return null;
    }

    private static String readText(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode == null ? null : fieldNode.textValue();
    }

    public String getIri() {
        return this.iri;
    }

    public Map<String, Keyword> getKeywords() {
        return this.keywords;
    }

    public Map<String, Boolean> getVocabularies() {
        return this.vocabularies;
    }
    
    public VersionFlag getSpecification() {
        return this.specification;
    }

    /**
     * Creates a new validator of the keyword.
     *
     * @param validationContext the validation context
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param keyword the keyword
     * @param schemaNode the schema node
     * @param parentSchema the parent schema
     * @return the validator
     */
    public JsonValidator newValidator(ValidationContext validationContext, SchemaLocation schemaLocation,
            JsonNodePath evaluationPath, String keyword, JsonNode schemaNode, JsonSchema parentSchema) {
        try {
            Keyword kw = this.keywords.get(keyword);
            if (kw == null) {
                if (keyword.equals(validationContext.getConfig().getErrorMessageKeyword())) {
                    return null;
                }
                if (validationContext.getConfig().isNullableKeywordEnabled() && "nullable".equals(keyword)) {
                    return null;
                }
                if (ValidatorTypeCode.DISCRIMINATOR.getValue().equals(keyword)
                        && validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                    return ValidatorTypeCode.DISCRIMINATOR.newValidator(schemaLocation, evaluationPath, schemaNode,
                            parentSchema, validationContext);
                }
                kw = this.builder.unknownKeywordFactory != null
                        ? this.builder.unknownKeywordFactory.getKeyword(keyword, validationContext)
                        : UnknownKeywordFactory.getInstance().getKeyword(keyword, validationContext);
                if (kw == null) {
                    return null;
                }
            }
            return kw.newValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof JsonSchemaException) {
                logger.error("Error:", e);
                throw (JsonSchemaException) e.getTargetException();
            }
            logger.warn("Could not load validator {}", keyword);
            throw new JsonSchemaException(e.getTargetException());
        } catch (JsonSchemaException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Could not load validator {}", keyword);
            throw new JsonSchemaException(e);
        }
    }

    @Override
    public String toString() {
        return this.iri;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iri);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JsonMetaSchema other = (JsonMetaSchema) obj;
        return Objects.equals(iri, other.iri);
    }
}
