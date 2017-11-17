/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonMetaSchema {


    private static final Logger logger = LoggerFactory
            .getLogger(JsonMetaSchema.class);
    
    
    private static class DraftV4 {
        private static String URI = "http://json-schema.org/draft-04/schema#";
        // Draft 6 uses "$id"
        private static final String DRAFT_4_ID = "id";
        public static final List<Format> BUILTIN_FORMATS = new ArrayList<Format>();
        static {
            BUILTIN_FORMATS.add(pattern("date-time", 
                    "^\\d{4}-(?:0[0-9]{1}|1[0-2]{1})-[0-9]{2}[tT ]\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([zZ]|[+-]\\d{2}:\\d{2})$"));
            BUILTIN_FORMATS.add(pattern("date", "^\\d{4}-(?:0[0-9]{1}|1[0-2]{1})-[0-9]{2}$"));
            BUILTIN_FORMATS.add(pattern("time", "^\\d{2}:\\d{2}:\\d{2}$"));
            BUILTIN_FORMATS.add(pattern("email", "^\\S+@\\S+$"));
            BUILTIN_FORMATS.add(pattern("ip-address", 
                    "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
            BUILTIN_FORMATS.add(pattern("ipv4", 
                    "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
            BUILTIN_FORMATS.add(pattern("ipv6", 
                    "^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$"));
            BUILTIN_FORMATS.add(pattern("uri", "(^[a-zA-Z][a-zA-Z0-9+-.]*:[^\\s]*$)|(^//[^\\s]*$)"));
            BUILTIN_FORMATS.add(pattern("color", 
                    "(#?([0-9A-Fa-f]{3,6})\\b)|(aqua)|(black)|(blue)|(fuchsia)|(gray)|(green)|(lime)|(maroon)|(navy)|(olive)|(orange)|(purple)|(red)|(silver)|(teal)|(white)|(yellow)|(rgb\\(\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*\\))|(rgb\\(\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*\\))"));
            BUILTIN_FORMATS.add(pattern("hostname", 
                    "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$"));
            BUILTIN_FORMATS.add(pattern("alpha", "^[a-zA-Z]+$"));
            BUILTIN_FORMATS.add(pattern("alphanumeric", "^[a-zA-Z0-9]+$"));
            BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
            BUILTIN_FORMATS.add(pattern("utc-millisec", "^[0-9]+(\\.?[0-9]+)?$"));
            BUILTIN_FORMATS.add(pattern("style", "\\s*(.+?):\\s*([^;]+);?"));
        }

        static PatternFormat pattern(String name, String regex) {
            return new PatternFormat(name, regex);
        }
        
        public static JsonMetaSchema getInstance() {
            return new Builder(URI)
                    .idKeyword(DRAFT_4_ID)
                    .addFormats(BUILTIN_FORMATS)
                    .addKeywords(ValidatorTypeCode.getNonFormatKeywords())
                    .build();
        }
    }
    
    public static class Builder {
        private Map<String, Keyword> keywords = new HashMap<String, Keyword>();
        private Map<String, Format> formats = new HashMap<String, Format>();
        private String uri;
        private String idKeyword = "id";
        
        public Builder(String uri) {
            this.uri = uri;
        }
        
        private static Map<String, Keyword> createKeywordsMap(Map<String, Keyword> kwords, Map<String, Format> formats) {
            final Map<String, Keyword> map = new HashMap<String, Keyword>();
            for (Map.Entry<String, Keyword> type: kwords.entrySet()) {
                String keywordName = type.getKey();
                Keyword keyword = type.getValue();
                if (ValidatorTypeCode.FORMAT.getValue().equals(keywordName)) {
                    if (!(keyword instanceof FormatKeyword)) {
                        throw new IllegalArgumentException("Overriding the keyword 'format' is not supported");
                    }
                    // ignore - format keyword will be created again below.
                } else {
                    map.put(keyword.getValue(), keyword);
                }
            }
            final FormatKeyword formatKeyword = new FormatKeyword(ValidatorTypeCode.FORMAT, formats);
            map.put(formatKeyword.getValue(), formatKeyword);
            return Collections.unmodifiableMap(map);
        }
        
        public Builder addKeyword(Keyword keyword) {
            this.keywords.put(keyword.getValue(), keyword);
            return this;
        }
        
        public Builder addKeywords(Collection<? extends Keyword> keywords) {
            for (Keyword keyword: keywords) {
                this.keywords.put(keyword.getValue(), keyword);
            }
            return this;
        }
        
        public Builder addFormat(Format format) {
            this.formats.put(format.getName(), format);
            return this;
        }
        
        public Builder addFormats(Collection<? extends Format> formats) {
            for (Format format : formats) {
                addFormat(format);
            }
            return this;
        }
        
        
        public Builder idKeyword(String idKeyword) {
            this.idKeyword = idKeyword;
            return this;
        }
        
        public JsonMetaSchema build() {
            // create builtin keywords with (custom) formats.
            final Map<String, Keyword> kwords = createKeywordsMap(keywords, formats);
            return new JsonMetaSchema(uri, idKeyword, kwords);
        }
    }
    
    private final String uri;
    private final String idKeyword;
    private final Map<String, Keyword> keywords;

    private JsonMetaSchema(String uri, String idKeyword, Map<String, Keyword> keywords) {
        if (StringUtils.isBlank(uri)) {
            throw new IllegalArgumentException("uri must not be null or blank");
        }
        if (StringUtils.isBlank(idKeyword)) {
            throw new IllegalArgumentException("idKeyword must not be null or blank");
        }
        if (keywords == null) {
            throw new IllegalArgumentException("keywords must not be null ");
        }

        this.uri = uri;
        this.idKeyword = idKeyword;
        this.keywords = keywords;
    }

    public static JsonMetaSchema getDraftV4() {
        return DraftV4.getInstance();
    }
    
    /**
     * Builder without keywords or formats.
     * 
     * Use {@link #getDraftV4()} for the Draft 4 Metaschema, or if you need a builder based on Draft4, use
     * 
     * <code>
     * JsonMetaSchema.builder("http://your-metaschema-uri", JsonSchemaFactory.getDraftV4()).build();
     * </code>
     * 
     * @param uri the URI of the metaschema that will be defined via this builder.
     * @return a builder instance without any keywords or formats - usually not what one needs.
     */
    public static Builder builder(String uri) {
        return new Builder(uri);
    }
    
    /**
     * 
     * @param uri the URI of your new JsonMetaSchema that will be defined via this builder.
     * @param blueprint the JsonMetaSchema to base your custom JsonMetaSchema on.
     * @return a builder instance preconfigured to be the same as blueprint, but with a different uri.
     */
    public static Builder builder(String uri, JsonMetaSchema blueprint) {
        FormatKeyword formatKeyword = (FormatKeyword)blueprint.keywords.get(ValidatorTypeCode.FORMAT.getValue());
        if (formatKeyword == null) {
            throw new IllegalArgumentException("The formatKeyword did not exist - blueprint is invalid.");
        }
        return builder(uri)
                .idKeyword(blueprint.idKeyword)
                .addKeywords(blueprint.keywords.values())
                .addFormats(formatKeyword.getFormats());
    }

    public String readId(JsonNode schemaNode) {
        JsonNode idNode = schemaNode.get(idKeyword);
        if (idNode == null || !idNode.isTextual()) {
            return null;
        }
        return idNode.textValue();
    }
    
    public String getUri() {
        return uri;
    }
    
    public Optional<JsonValidator> newValidator(ValidationContext validationContext, String schemaPath, String keyword /* keyword */, JsonNode schemaNode,
            JsonSchema parentSchema) {
        
        try {
            Keyword kw = keywords.get(keyword);
            if (kw == null) {
                logger.warn("Unknown keyword " + keyword);
                return Optional.empty();
            }
            return Optional.of(kw.newValidator(schemaPath, schemaNode, parentSchema, validationContext));
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof JsonSchemaException) {
                throw (JsonSchemaException) e.getTargetException();
            } else {
                logger.warn("Could not load validator " + keyword);
                throw new JsonSchemaException(e.getTargetException());
            }
        } catch(JsonSchemaException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Could not load validator " + keyword);
            throw new JsonSchemaException(e);
        }
    }
    

}
