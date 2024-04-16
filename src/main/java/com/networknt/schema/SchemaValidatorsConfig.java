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
import com.networknt.schema.i18n.DefaultMessageSource;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.walk.DefaultItemWalkListenerRunner;
import com.networknt.schema.walk.DefaultKeywordWalkListenerRunner;
import com.networknt.schema.walk.DefaultPropertyWalkListenerRunner;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkListenerRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SchemaValidatorsConfig {
    public static final int DEFAULT_PRELOAD_JSON_SCHEMA_REF_MAX_NESTING_DEPTH = 40;

    /**
     * Used to validate the acceptable $id values.
     */
    private JsonSchemaIdValidator schemaIdValidator = JsonSchemaIdValidator.DEFAULT;

    /**
     * when validate type, if TYPE_LOOSE = true, will try to convert string to
     * different types to match the type defined in schema.
     */
    private boolean typeLoose;

    /**
     * When set to true, validator process is stop immediately when a very first
     * validation error is discovered.
     */
    private boolean failFast;

    /**
     * When set to true, walker sets nodes that are missing or NullNode to the
     * default value, if any, and mutate the input json.
     */
    private ApplyDefaultsStrategy applyDefaultsStrategy = ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;

    /**
     * When set to true, use ECMA-262 compatible validator
     */
    private boolean ecma262Validator;

    /**
     * When set to true, use Java-specific semantics rather than native JavaScript
     * semantics
     */
    private boolean javaSemantics;

    /**
     * When set to true, can interpret round doubles as integers
     */
    private boolean losslessNarrowing;

    /**
     * When set to true, "messages" provided in schema are used for forming validation errors
     * else default messages are used
     */
    private boolean isCustomMessageSupported = true;

    /**
     * When set to true, support for discriminators is enabled for validations of
     * oneOf, anyOf and allOf as described on <a href=
     * "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">GitHub</a>.
     */
    private boolean openAPI3StyleDiscriminators = false;

    /**
     * Contains a mapping of how strict a keyword's validators should be.
     * Defaults to {@literal true}.
     * <p>
     * Each validator has its own understanding of what constitutes strict
     * and permissive.
     */
    private final Map<String, Boolean> strictness = new HashMap<>(0);

    /**
     * When a field is set as nullable in the OpenAPI specification, the schema
     * validator validates that it is nullable however continues with validation
     * against the nullable field
     * <p>
     * If handleNullableField is set to true && incoming field is nullable && value
     * is field: null --> succeed If handleNullableField is set to false && incoming
     * field is nullable && value is field: null --> it is up to the type validator
     * using the SchemaValidator to handle it.
     */
    private boolean handleNullableField = true;

    /**
     * When set to true assumes that schema is used to validate incoming data from an API.
     */
    private Boolean readOnly = null;

    /**
     * When set to true assumes that schema is used to to validate outgoing data from an API.
     */
    private Boolean writeOnly = null;

    /**
     * The approach used to generate paths in reported messages, logs and errors. Default is the legacy "JSONPath-like" approach.
     */
    private PathType pathType = PathType.DEFAULT;

    /**
     * Controls if the schema will automatically be preloaded.
     */
    private boolean preloadJsonSchema = true;

    /**
     * Controls the max depth of the evaluation path to preload when preloading refs.
     */
    private int preloadJsonSchemaRefMaxNestingDepth = DEFAULT_PRELOAD_JSON_SCHEMA_REF_MAX_NESTING_DEPTH;

    /**
     * Controls if schemas loaded from refs will be cached and reused for subsequent runs.
     */
    private boolean cacheRefs = true;

    // This is just a constant for listening to all Keywords.
    public static final String ALL_KEYWORD_WALK_LISTENER_KEY = "com.networknt.AllKeywordWalkListener";

    private final Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap = new HashMap<>();

    private final List<JsonSchemaWalkListener> propertyWalkListeners = new ArrayList<>();

    private final List<JsonSchemaWalkListener> itemWalkListeners = new ArrayList<>();

    private ExecutionContextCustomizer executionContextCustomizer;

    private boolean loadCollectors = true;

    /**
     * The Locale to consider when loading validation messages from the default resource bundle.
     */
    private Locale locale;

    /**
     * The message source to use for generating localised messages.
     */
    private MessageSource messageSource;

    /**
     * Since Draft 2019-09 format assertions are not enabled by default.
     */
    private Boolean formatAssertionsEnabled = null;

    /************************ START OF UNEVALUATED CHECKS **********************************/

    // These are costly in terms of performance so we provide a way to disable them.
    private boolean disableUnevaluatedItems = false;
    private boolean disableUnevaluatedProperties = false;
    
    public SchemaValidatorsConfig disableUnevaluatedAnalysis() {
        disableUnevaluatedItems();
        disableUnevaluatedProperties();
        return this;
    }

    public SchemaValidatorsConfig disableUnevaluatedItems() {
        this.disableUnevaluatedItems = true;
        return this;
    }

    public SchemaValidatorsConfig disableUnevaluatedProperties() {
        this.disableUnevaluatedProperties = true;
        return this;
    }

    public SchemaValidatorsConfig enableUnevaluatedAnalysis() {
        enableUnevaluatedItems();
        enableUnevaluatedProperties();
        return this;
    }

    public SchemaValidatorsConfig enableUnevaluatedItems() {
        this.disableUnevaluatedItems = false;
        return this;
    }

    public SchemaValidatorsConfig enableUnevaluatedProperties() {
        this.disableUnevaluatedProperties = false;
        return this;
    }

    public boolean isUnevaluatedItemsAnalysisDisabled() {
        return this.disableUnevaluatedItems;
    }

    public boolean isUnevaluatedItemsAnalysisEnabled() {
        return !isUnevaluatedItemsAnalysisDisabled();
    }

    public boolean isUnevaluatedPropertiesAnalysisDisabled() {
        return this.disableUnevaluatedProperties;
    }

    public boolean isUnevaluatedPropertiesAnalysisEnabled() {
        return !isUnevaluatedPropertiesAnalysisDisabled();
    }

    /************************ END OF UNEVALUATED CHECKS **********************************/

    /**
     *
     * @return true if type loose is used.
     */
    public boolean isTypeLoose() {
        return this.typeLoose;
    }

    public void setTypeLoose(boolean typeLoose) {
        this.typeLoose = typeLoose;
    }

    /**
     * When enabled,
     * {@link JsonValidator#validate(ExecutionContext, JsonNode, JsonNode, JsonNodePath)}
     * doesn't return any {@link java.util.Set}&lt;{@link ValidationMessage}&gt;,
     * instead a {@link JsonSchemaException} is thrown as soon as a validation
     * errors is discovered.
     *
     * @param failFast boolean
     */
    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isFailFast() {
        return this.failFast;
    }

    public void setApplyDefaultsStrategy(ApplyDefaultsStrategy applyDefaultsStrategy) {
        this.applyDefaultsStrategy = applyDefaultsStrategy != null ? applyDefaultsStrategy
                : ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;
    }

    public ApplyDefaultsStrategy getApplyDefaultsStrategy() {
        return this.applyDefaultsStrategy;
    }

    public boolean isHandleNullableField() {
        return this.handleNullableField;
    }

    public void setHandleNullableField(boolean handleNullableField) {
        this.handleNullableField = handleNullableField;
    }

    public boolean isEcma262Validator() {
        return this.ecma262Validator;
    }

    public void setEcma262Validator(boolean ecma262Validator) {
        this.ecma262Validator = ecma262Validator;
    }

    public boolean isJavaSemantics() {
        return this.javaSemantics;
    }

    public void setJavaSemantics(boolean javaSemantics) {
        this.javaSemantics = javaSemantics;
    }

    public boolean isCustomMessageSupported() {
        return isCustomMessageSupported;
    }

    public void setCustomMessageSupported(boolean customMessageSupported) {
        this.isCustomMessageSupported = customMessageSupported;
    }

    public void addKeywordWalkListener(JsonSchemaWalkListener keywordWalkListener) {
        if (this.keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY) == null) {
            List<JsonSchemaWalkListener> keywordWalkListeners = new ArrayList<>();
            this.keywordWalkListenersMap.put(ALL_KEYWORD_WALK_LISTENER_KEY, keywordWalkListeners);
        }
        this.keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY).add(keywordWalkListener);
    }

    public void addKeywordWalkListener(String keyword, JsonSchemaWalkListener keywordWalkListener) {
        if (this.keywordWalkListenersMap.get(keyword) == null) {
            List<JsonSchemaWalkListener> keywordWalkListeners = new ArrayList<>();
            this.keywordWalkListenersMap.put(keyword, keywordWalkListeners);
        }
        this.keywordWalkListenersMap.get(keyword).add(keywordWalkListener);
    }

    public void addKeywordWalkListeners(List<JsonSchemaWalkListener> keywordWalkListeners) {
        if (this.keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY) == null) {
            List<JsonSchemaWalkListener> ikeywordWalkListeners = new ArrayList<>();
            this.keywordWalkListenersMap.put(ALL_KEYWORD_WALK_LISTENER_KEY, ikeywordWalkListeners);
        }
        this.keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY).addAll(keywordWalkListeners);
    }

    public void addKeywordWalkListeners(String keyword, List<JsonSchemaWalkListener> keywordWalkListeners) {
        if (this.keywordWalkListenersMap.get(keyword) == null) {
            List<JsonSchemaWalkListener> ikeywordWalkListeners = new ArrayList<>();
            this.keywordWalkListenersMap.put(keyword, ikeywordWalkListeners);
        }
        this.keywordWalkListenersMap.get(keyword).addAll(keywordWalkListeners);
    }

    public void addPropertyWalkListeners(List<JsonSchemaWalkListener> propertyWalkListeners) {
        this.propertyWalkListeners.addAll(propertyWalkListeners);
    }

    public void addPropertyWalkListener(JsonSchemaWalkListener propertyWalkListener) {
        this.propertyWalkListeners.add(propertyWalkListener);
    }

    public void addItemWalkListener(JsonSchemaWalkListener itemWalkListener) {
        this.itemWalkListeners.add(itemWalkListener);
    }

    public void addItemWalkListeners(List<JsonSchemaWalkListener> itemWalkListeners) {
        this.itemWalkListeners.addAll(itemWalkListeners);
    }

    public List<JsonSchemaWalkListener> getPropertyWalkListeners() {
        return this.propertyWalkListeners;
    }

    public Map<String, List<JsonSchemaWalkListener>> getKeywordWalkListenersMap() {
        return this.keywordWalkListenersMap;
    }

    public List<JsonSchemaWalkListener> getArrayItemWalkListeners() {
        return this.itemWalkListeners;
    }

    private final WalkListenerRunner itemWalkListenerRunner = new DefaultItemWalkListenerRunner(getArrayItemWalkListeners());

    WalkListenerRunner getItemWalkListenerRunner() {
        return this.itemWalkListenerRunner;
    }
    
    private WalkListenerRunner keywordWalkListenerRunner = new DefaultKeywordWalkListenerRunner(getKeywordWalkListenersMap());

    WalkListenerRunner getKeywordWalkListenerRunner() {
        return this.keywordWalkListenerRunner;
    }

    private WalkListenerRunner propertyWalkListenerRunner = new DefaultPropertyWalkListenerRunner(getPropertyWalkListeners());

    WalkListenerRunner getPropertyWalkListenerRunner() {
        return this.propertyWalkListenerRunner;
    }

    public SchemaValidatorsConfig() {
    }

    public ExecutionContextCustomizer getExecutionContextCustomizer() {
        return this.executionContextCustomizer;
    }

    public void setExecutionContextCustomizer(ExecutionContextCustomizer executionContextCustomizer) {
        this.executionContextCustomizer = executionContextCustomizer;
    }

    public boolean isLosslessNarrowing() {
        return this.losslessNarrowing;
    }

    public void setLosslessNarrowing(boolean losslessNarrowing) {
        this.losslessNarrowing = losslessNarrowing;
    }

    /**
     * Indicates whether OpenAPI 3 style discriminators should be supported
     * 
     * @return true in case discriminators are enabled
     * @since 1.0.51
     */
    public boolean isOpenAPI3StyleDiscriminators() {
        return this.openAPI3StyleDiscriminators;
    }

    /**
     * When enabled, the validation of <code>anyOf</code> and <code>allOf</code> in
     * polymorphism will respect OpenAPI 3 style discriminators as described in the
     * <a href=
     * "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">OpenAPI
     * 3.0.3 spec</a>. The presence of a discriminator configuration on the schema
     * will lead to the following changes in the behavior:
     * <ul>
     * <li>for <code>oneOf</code> the spec is unfortunately very vague. Whether
     * <code>oneOf</code> semantics should be affected by discriminators or not is
     * not even 100% clear within the members of the OAS steering committee.
     * Therefore <code>oneOf</code> at the moment ignores discriminators</li>
     * <li>for <code>anyOf</code> the validation will choose one of the candidate
     * schemas for validation based on the discriminator property value and will
     * pass validation when this specific schema passes. This is in particular
     * useful when the payload could match multiple candidates in the
     * <code>anyOf</code> list and could lead to ambiguity. Example: type B has all
     * mandatory properties of A and adds more mandatory ones. Whether the payload
     * is an A or B is determined via the discriminator property name. A payload
     * indicating it is an instance of B then requires passing the validation of B
     * and passing the validation of A would not be sufficient anymore.</li>
     * <li>for <code>allOf</code> use cases with discriminators defined on the
     * copied-in parent type, it is possible to automatically validate against a
     * subtype. Example: some schema specifies that there is a field of type A. A
     * carries a discriminator field and B inherits from A. Then B is automatically
     * a candidate for validation as well and will be chosen in case the
     * discriminator property matches</li>
     * </ul>
     * 
     * @param openAPI3StyleDiscriminators whether or not discriminators should be
     *                                    used. Defaults to <code>false</code>
     * @since 1.0.51
     */
    public void setOpenAPI3StyleDiscriminators(boolean openAPI3StyleDiscriminators) {
        this.openAPI3StyleDiscriminators = openAPI3StyleDiscriminators;
    }

    public void setLoadCollectors(boolean loadCollectors) {
        this.loadCollectors = loadCollectors;
    }

    public boolean doLoadCollectors() {
        return this.loadCollectors;
    }

    public boolean isReadOnly() {
        return null != this.readOnly && this.readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isWriteOnly() {
        return null != this.writeOnly && this.writeOnly;
    }

    public void setWriteOnly(boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    /**
     * Use {@code isReadOnly} or {@code isWriteOnly}
     * @return true if schema is used to write data
     */
    @Deprecated
    public boolean isWriteMode() {
        return null == this.writeOnly || this.writeOnly;
    }

    /**
     * Set the approach used to generate paths in messages, logs and errors (default is PathType.LEGACY).
     *
     * @param pathType The path generation approach.
     */
    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    /**
     * Get the approach used to generate paths in messages, logs and errors.
     *
     * @return The path generation approach.
     */
    public PathType getPathType() {
        return this.pathType;
    }

    /**
     * Answers whether a keyword's validators may relax their analysis. The
     * default is to perform strict checking. One must explicitly allow a
     * validator to be more permissive.
     * <p>
     * Each validator has its own understanding of what is permissive and
     * strict. Consult the keyword's documentation for details. 
     * 
     * @param keyword the keyword to adjust (not null)
     * @return Whether to perform a strict validation.
     */
    public boolean isStrict(String keyword) {
        return isStrict(keyword, Boolean.TRUE);
    }

    /**
     * Determines if the validator should perform strict checking.
     *
     * @param keyword the keyword
     * @param defaultValue the default value
     * @return whether to perform a strict validation
     */
    public boolean isStrict(String keyword, Boolean defaultValue) {
        return this.strictness.getOrDefault(Objects.requireNonNull(keyword, "keyword cannot be null"), defaultValue);
    }

    /**
     * Alters the strictness of validations for a specific keyword. When set to
     * {@literal true}, instructs the keyword's validators to perform strict
     * validation. Otherwise, a validator may perform a more permissive check.
     * 
     * @param keyword The keyword to adjust (not null)
     * @param strict Whether to perform strict validations
     */
    public void setStrict(String keyword, boolean strict) {
        this.strictness.put(Objects.requireNonNull(keyword, "keyword cannot be null"), strict);
    }

    /**
     * Get the locale to consider when generating localised messages (default is the
     * JVM default).
     * <p>
     * This locale is on a schema basis and will be used as the default locale for
     * {@link com.networknt.schema.ExecutionConfig}.
     *
     * @return The locale.
     */
    public Locale getLocale() {
        if (this.locale == null) {
            // This should not be cached as it can be changed using Locale#setDefault(Locale)
            return Locale.getDefault();
        }
        return this.locale;
    }

    /**
     * Set the locale to consider when generating localised messages.
     * <p>
     * Note that this locale is set on a schema basis. To configure the schema on a
     * per execution basis use
     * {@link com.networknt.schema.ExecutionConfig#setLocale(Locale)}.
     *
     * @param locale The locale.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Get the message source to use for generating localised messages.
     * 
     * @return the message source
     */
    public MessageSource getMessageSource() {
        if (this.messageSource == null) {
            return DefaultMessageSource.getInstance();
        }
        return this.messageSource;
    }

    /**
     * Set the message source to use for generating localised messages.
     * 
     * @param messageSource the message source
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    /**
     * Gets the format assertion enabled flag.
     * <p>
     * This defaults to null meaning that it will follow the defaults of the
     * specification.
     * <p>
     * Since draft 2019-09 this will default to false unless enabled by using the
     * $vocabulary keyword.
     * 
     * @return the format assertions enabled flag
     */
    public Boolean getFormatAssertionsEnabled() {
        return formatAssertionsEnabled;
    }

    /**
     * Sets the format assertion enabled flag.
     * 
     * @param formatAssertionsEnabled the format assertions enabled flag
     */
    public void setFormatAssertionsEnabled(Boolean formatAssertionsEnabled) {
        this.formatAssertionsEnabled = formatAssertionsEnabled;
    }

    /**
     * Gets the schema id validator to validate $id.
     * 
     * @return the validator
     */
    public JsonSchemaIdValidator getSchemaIdValidator() {
        return schemaIdValidator;
    }

    /**
     * Sets the schema id validator to validate $id.
     * 
     * @param schemaIdValidator the validator
     */
    public void setSchemaIdValidator(JsonSchemaIdValidator schemaIdValidator) {
        this.schemaIdValidator = schemaIdValidator;
    }

    /**
     * Gets if the schema should be preloaded.
     * 
     * @return true if it should be preloaded
     */
    public boolean isPreloadJsonSchema() {
        return preloadJsonSchema;
    }

    /**
     * Sets if the schema should be preloaded.
     * 
     * @param preloadJsonSchema true to preload
     */
    public void setPreloadJsonSchema(boolean preloadJsonSchema) {
        this.preloadJsonSchema = preloadJsonSchema;
    }

    /**
     * Gets the max depth of the evaluation path to preload when preloading refs.
     *
     * @return the max depth to preload
     */
    public int getPreloadJsonSchemaRefMaxNestingDepth() {
        return preloadJsonSchemaRefMaxNestingDepth;
    }

    /**
     * Sets the max depth of the evaluation path to preload when preloading refs.
     *
     * @param preloadJsonSchemaRefMaxNestingDepth the max depth to preload
     */
    public void setPreloadJsonSchemaRefMaxNestingDepth(int preloadJsonSchemaRefMaxNestingDepth) {
        this.preloadJsonSchemaRefMaxNestingDepth = preloadJsonSchemaRefMaxNestingDepth;
    }

    /**
     * Gets if schemas loaded from refs will be cached and reused for subsequent
     * runs.
     *
     * @return true if schemas loaded from refs should be cached
     */
    public boolean isCacheRefs() {
        return cacheRefs;
    }

    /**
     * Sets if schemas loaded from refs will be cached and reused for subsequent
     * runs.
     * <p>
     * Note that setting this to false will affect performance as refs will need to
     * be repeatedly resolved for each evaluation run. It may be needed to be set to
     * false if there are multiple nested applicators like anyOf, oneOf and allOf as
     * that will consume a lot of memory to cache all the permutations.
     * 
     * @param cacheRefs true to cache
     */
    public void setCacheRefs(boolean cacheRefs) {
        this.cacheRefs = cacheRefs;
    }
}
