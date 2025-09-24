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

package com.networknt.schema.dialect;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.Format;
import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.Vocabularies;
import com.networknt.schema.Vocabulary;
import com.networknt.schema.VocabularyFactory;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.FormatKeyword;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.keyword.KeywordFactory;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.keyword.UnknownKeywordFactory;
import com.networknt.schema.keyword.Keywords;
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
 * A dialect represents the set of keywords and semantics that can be used to
 * evaluate a schema. The dialect can be uniquely identified by its IRI which
 * points to the meta-schema used to validate schemas written for that dialect.
 * The dialect for a particular schema is indicated using the $schema keyword.
 */
public class Dialect {
    private static final Logger logger = LoggerFactory.getLogger(Dialect.class);

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
     * Builder for {@link Dialect}. 
     */
    public static class Builder {
        private String id;
        private String idKeyword = "$id";
        private SpecificationVersion specificationVersion = null;
        private final Map<String, Keyword> keywords = new HashMap<>();
        private final Map<String, Format> formats = new HashMap<>();
        private final Map<String, Boolean> vocabularies = new HashMap<>();
        private FormatKeywordFactory formatKeywordFactory = null;
        private VocabularyFactory vocabularyFactory = null;
        private KeywordFactory unknownKeywordFactory = null;

        public Builder(String id) {
            this.id = id;
        }

        private Map<String, Keyword> createKeywordsMap(Map<String, Keyword> kwords, Map<String, Format> formats) {
            boolean formatKeywordPresent = false;
            Map<String, Keyword> map = new HashMap<>();
            for (Map.Entry<String, Keyword> type : kwords.entrySet()) {
                String keywordName = type.getKey();
                Keyword keyword = type.getValue();
                if (Keywords.FORMAT.getValue().equals(keywordName)) {
                    if (!(keyword instanceof FormatKeyword) && !Keywords.FORMAT.equals(keyword)) {
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
         * Sets the specification version.
         * 
         * @param specification the specification version
         * @return the builder
         */
        public Builder specificationVersion(SpecificationVersion specification) {
            this.specificationVersion = specification;
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

        public Dialect build() {
            // create builtin keywords with (custom) formats.
            Map<String, Keyword> keywords = this.keywords;
            if (this.specificationVersion != null) {
                if (this.specificationVersion.getOrder() >= SpecificationVersion.DRAFT_2019_09.getOrder()) {
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
                            Error error = Error.builder()
                                    .message("Meta-schema ''{0}'' has unknown required vocabulary ''{1}''")
                                    .arguments(this.id, id).build();
                            throw new InvalidSchemaException(error);
                        }
                    }
                }
            }
            Map<String, Keyword> result = createKeywordsMap(keywords, this.formats);
            return new Dialect(this.id, this.idKeyword, result, this.vocabularies, this.specificationVersion, this);
        }
    }

    private final String id;
    private final String idKeyword;
    private final Map<String, Keyword> keywords;
    private final Map<String, Boolean> vocabularies;
    private final SpecificationVersion specificationVersion;

    private final Builder builder;

    Dialect(String dialectId, String idKeyword, Map<String, Keyword> keywords, Map<String, Boolean> vocabularies, SpecificationVersion specification, Builder builder) {
        if (StringUtils.isBlank(dialectId)) {
            throw new IllegalArgumentException("dialect id must not be null or blank");
        }
        if (StringUtils.isBlank(idKeyword)) {
            throw new IllegalArgumentException("idKeyword must not be null or blank");
        }
        if (keywords == null) {
            throw new IllegalArgumentException("keywords must not be null ");
        }

        this.id = dialectId;
        this.idKeyword = idKeyword;
        this.keywords = keywords;
        this.specificationVersion = specification;
        this.vocabularies = vocabularies;
        this.builder = builder;
    }

    /**
     * Create a builder without keywords or formats.
     *
     * @param id the IRI of the dialect that will be defined via this builder.
     * @return a builder instance without any keywords or formats - usually not what one needs.
     */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    /**
     * Create a builder.
     * 
     * @param id       the IRI of your new Dialect that will be defined via
     *                  this builder.
     * @param blueprint the Dialect to base your custom Dialect on.
     * @return a builder instance preconfigured to be the same as blueprint, but
     *         with a different uri.
     */
    public static Builder builder(String id, Dialect blueprint) {
        Builder builder = builder(blueprint);
        builder.id = id;
        return builder;
    }

    /**
     * Create a builder.
     * 
     * @param blueprint the Dialect to base your custom Dialect on.
     * @return a builder instance preconfigured to be the same as blueprint
     */
    public static Builder builder(Dialect blueprint) {
        Map<String, Boolean> vocabularies = new HashMap<>(blueprint.getVocabularies());
        return builder(blueprint.getId())
                .idKeyword(blueprint.idKeyword)
                .keywords(blueprint.builder.keywords.values())
                .formats(blueprint.builder.formats.values())
                .specificationVersion(blueprint.getSpecificationVersion())
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

    public String getId() {
        return this.id;
    }

    public Map<String, Keyword> getKeywords() {
        return this.keywords;
    }

    public Map<String, Boolean> getVocabularies() {
        return this.vocabularies;
    }
    
    public SpecificationVersion getSpecificationVersion() {
        return this.specificationVersion;
    }

    /**
     * Creates a new validator of the keyword.
     *
     * @param schemaContext the schema context
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param keyword the keyword
     * @param schemaNode the schema node
     * @param parentSchema the parent schema
     * @return the validator
     */
    public KeywordValidator newValidator(SchemaContext schemaContext, SchemaLocation schemaLocation,
            NodePath evaluationPath, String keyword, JsonNode schemaNode, Schema parentSchema) {
        try {
            Keyword kw = this.keywords.get(keyword);
            if (kw == null) {
                if (keyword.equals(schemaContext.getSchemaRegistryConfig().getErrorMessageKeyword())) {
                    return null;
                }
                if (schemaContext.isNullableKeywordEnabled() && "nullable".equals(keyword)) {
                    return null;
                }
                if (Keywords.DISCRIMINATOR.getValue().equals(keyword)
                        && schemaContext.isDiscriminatorKeywordEnabled()) {
                    return Keywords.DISCRIMINATOR.newValidator(schemaLocation, evaluationPath, schemaNode,
                            parentSchema, schemaContext);
                }
                kw = this.builder.unknownKeywordFactory != null
                        ? this.builder.unknownKeywordFactory.getKeyword(keyword, schemaContext)
                        : UnknownKeywordFactory.getInstance().getKeyword(keyword, schemaContext);
                if (kw == null) {
                    return null;
                }
            }
            return kw.newValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, schemaContext);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof SchemaException) {
                logger.error("Error:", e);
                throw (SchemaException) e.getTargetException();
            }
            logger.warn("Could not load validator {}", keyword);
            throw new SchemaException(e.getTargetException());
        } catch (SchemaException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Could not load validator {}", keyword);
            throw new SchemaException(e);
        }
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Dialect other = (Dialect) obj;
        return Objects.equals(id, other.id);
    }
}
