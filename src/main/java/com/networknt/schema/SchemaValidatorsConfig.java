/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache LicenseBuilder  Version 2.0 (the "License");
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
import com.networknt.schema.regex.ECMAScriptRegularExpressionFactory;
import com.networknt.schema.regex.JDKRegularExpressionFactory;
import com.networknt.schema.regex.RegularExpressionFactory;
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
import java.util.function.Consumer;

/**
 * Configuration for validators. 
 */
public class SchemaValidatorsConfig {
    // This is just a constant for listening to all Keywords.
    public static final String ALL_KEYWORD_WALK_LISTENER_KEY = "com.networknt.AllKeywordWalkListener";

    public static final int DEFAULT_PRELOAD_JSON_SCHEMA_REF_MAX_NESTING_DEPTH = 40;

    /**
     * The strategy the walker uses to sets nodes that are missing or NullNode to
     * the default value, if any, and mutate the input json.
     */
    private ApplyDefaultsStrategy applyDefaultsStrategy = ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;

    /**
     * Controls if schemas loaded from refs will be cached and reused for subsequent runs.
     */
    private boolean cacheRefs = true;

    /**
     * When set to true, "messages" provided in schema are used for forming validation errors
     * else default messages are used
     */
    private String errorMessageKeyword = "message";

    private ExecutionContextCustomizer executionContextCustomizer;

    /**
     * When set to true, validator process is stop immediately when a very first
     * validation error is discovered.
     */
    private boolean failFast;

    /**
     * Since Draft 2019-09 format assertions are not enabled by default.
     */
    private Boolean formatAssertionsEnabled = null;

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
    private boolean nullableKeywordEnabled = true;

    private final WalkListenerRunner itemWalkListenerRunner;

    private final List<JsonSchemaWalkListener> itemWalkListeners;

    /**
     * When set to true, use Java-specific semantics rather than native JavaScript
     * semantics
     */
    private boolean javaSemantics;

    private final WalkListenerRunner keywordWalkListenerRunner;

    private final Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap;

    /**
     * The Locale to consider when loading validation messages from the default resource bundle.
     */
    private Locale locale;

    /**
     * When set to true, can interpret round doubles as integers
     */
    private boolean losslessNarrowing;

    /**
     * The message source to use for generating localised messages.
     */
    private MessageSource messageSource;

    /**
     * When set to true, support for discriminators is enabled for validations of
     * oneOf, anyOf and allOf as described on <a href=
     * "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">GitHub</a>.
     */
    private boolean discriminatorKeywordEnabled = false;

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

    private final WalkListenerRunner propertyWalkListenerRunner;

    private final List<JsonSchemaWalkListener> propertyWalkListeners;

    /**
     * When set to true assumes that schema is used to validate incoming data from an API.
     */
    private Boolean readOnly = null;

    /**
     * Used to create {@link com.networknt.schema.regex.RegularExpression}.
     */
    private RegularExpressionFactory regularExpressionFactory = JDKRegularExpressionFactory.getInstance();

    /**
     * Used to validate the acceptable $id values.
     */
    private JsonSchemaIdValidator schemaIdValidator = JsonSchemaIdValidator.DEFAULT;
    
    /**
     * Contains a mapping of how strict a keyword's validators should be.
     * Defaults to {@literal true}.
     * <p>
     * Each validator has its own understanding of what constitutes strict
     * and permissive.
     */
    private final Map<String, Boolean> strictness;

    /**
     * when validate type, if TYPE_LOOSE = true, will try to convert string to
     * different types to match the type defined in schema.
     */
    private boolean typeLoose;

    /**
     * When set to true assumes that schema is used to validate outgoing data from an API.
     */
    private Boolean writeOnly = null;

    /**
     * Constructor to create an instance.
     * <p>
     * This is deprecated in favor of using the builder
     * {@link SchemaValidatorsConfig#builder()} to create an instance. Migration
     * note: The builder has different defaults from the constructor.
     * <pre>
     * SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
     *     .pathType(PathType.LEGACY)
     *     .errorMessageKeyword("message")
     *     .nullableKeywordEnabled(true)
     *     .build();
     * </pre>
     * <ul>
     * <li> customMessageSupported (errorMessageKeyword): change from message to null
     * <li> pathType: changed from PathType.LEGACY to PathType.JSON_POINTER.
     * <li> handleNullableField (nullableKeywordEnabled): changed from true to false
     * </ul>
     */
    @Deprecated
    public SchemaValidatorsConfig() {
        this.strictness = new HashMap<>(0);

        this.keywordWalkListenersMap = new HashMap<>();
        this.propertyWalkListeners = new ArrayList<>();
        this.itemWalkListeners = new ArrayList<>();
        
        this.itemWalkListenerRunner = new DefaultItemWalkListenerRunner(getArrayItemWalkListeners());
        this.keywordWalkListenerRunner = new DefaultKeywordWalkListenerRunner(getKeywordWalkListenersMap());
        this.propertyWalkListenerRunner = new DefaultPropertyWalkListenerRunner(getPropertyWalkListeners());
    }
 
    SchemaValidatorsConfig(ApplyDefaultsStrategy applyDefaultsStrategy, boolean cacheRefs,
            String errorMessageKeyword, ExecutionContextCustomizer executionContextCustomizer, boolean failFast,
            Boolean formatAssertionsEnabled, boolean nullableKeywordEnabled,
            List<JsonSchemaWalkListener> itemWalkListeners, boolean javaSemantics,
            Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap, Locale locale, boolean losslessNarrowing,
            MessageSource messageSource, boolean discriminatorKeywordEnabled, PathType pathType,
            boolean preloadJsonSchema, int preloadJsonSchemaRefMaxNestingDepth,
            List<JsonSchemaWalkListener> propertyWalkListeners, Boolean readOnly,
            RegularExpressionFactory regularExpressionFactory, JsonSchemaIdValidator schemaIdValidator,
            Map<String, Boolean> strictness, boolean typeLoose, Boolean writeOnly) {
        super();
        this.applyDefaultsStrategy = applyDefaultsStrategy;
        this.cacheRefs = cacheRefs;
        this.errorMessageKeyword = errorMessageKeyword;
        this.executionContextCustomizer = executionContextCustomizer;
        this.failFast = failFast;
        this.formatAssertionsEnabled = formatAssertionsEnabled;
        this.nullableKeywordEnabled = nullableKeywordEnabled;
        this.itemWalkListeners = itemWalkListeners;
        this.javaSemantics = javaSemantics;
        this.keywordWalkListenersMap = keywordWalkListenersMap;
        this.locale = locale;
        this.losslessNarrowing = losslessNarrowing;
        this.messageSource = messageSource;
        this.discriminatorKeywordEnabled = discriminatorKeywordEnabled;
        this.pathType = pathType;
        this.preloadJsonSchema = preloadJsonSchema;
        this.preloadJsonSchemaRefMaxNestingDepth = preloadJsonSchemaRefMaxNestingDepth;
        this.propertyWalkListeners = propertyWalkListeners;
        this.readOnly = readOnly;
        this.regularExpressionFactory = regularExpressionFactory;
        this.schemaIdValidator = schemaIdValidator;
        this.strictness = strictness;
        this.typeLoose = typeLoose;
        this.writeOnly = writeOnly;

        this.itemWalkListenerRunner = new DefaultItemWalkListenerRunner(getArrayItemWalkListeners());
        this.keywordWalkListenerRunner = new DefaultKeywordWalkListenerRunner(getKeywordWalkListenersMap());
        this.propertyWalkListenerRunner = new DefaultPropertyWalkListenerRunner(getPropertyWalkListeners());
    }

    public void addItemWalkListener(JsonSchemaWalkListener itemWalkListener) {
        this.itemWalkListeners.add(itemWalkListener);
    }

    public void addItemWalkListeners(List<JsonSchemaWalkListener> itemWalkListeners) {
        this.itemWalkListeners.addAll(itemWalkListeners);
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

    public void addPropertyWalkListener(JsonSchemaWalkListener propertyWalkListener) {
        this.propertyWalkListeners.add(propertyWalkListener);
    }

    public void addPropertyWalkListeners(List<JsonSchemaWalkListener> propertyWalkListeners) {
        this.propertyWalkListeners.addAll(propertyWalkListeners);
    }

    public ApplyDefaultsStrategy getApplyDefaultsStrategy() {
        return this.applyDefaultsStrategy;
    }

    public List<JsonSchemaWalkListener> getArrayItemWalkListeners() {
        return this.itemWalkListeners;
    }

    public ExecutionContextCustomizer getExecutionContextCustomizer() {
        return this.executionContextCustomizer;
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

    WalkListenerRunner getItemWalkListenerRunner() {
        return this.itemWalkListenerRunner;
    }

    WalkListenerRunner getKeywordWalkListenerRunner() {
        return this.keywordWalkListenerRunner;
    }

    public Map<String, List<JsonSchemaWalkListener>> getKeywordWalkListenersMap() {
        return this.keywordWalkListenersMap;
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
     * Get the approach used to generate paths in messages, logs and errors.
     *
     * @return The path generation approach.
     */
    public PathType getPathType() {
        return this.pathType;
    }

    /**
     * Gets the max depth of the evaluation path to preload when preloading refs.
     *
     * @return the max depth to preload
     */
    public int getPreloadJsonSchemaRefMaxNestingDepth() {
        return preloadJsonSchemaRefMaxNestingDepth;
    }

    WalkListenerRunner getPropertyWalkListenerRunner() {
        return this.propertyWalkListenerRunner;
    }

    public List<JsonSchemaWalkListener> getPropertyWalkListeners() {
        return this.propertyWalkListeners;
    }

    /**
     * Gets the regular expression factory.
     * <p>
     * This defaults to the JDKRegularExpressionFactory and the implementations
     * require inclusion of optional org.jruby.joni:joni or org.graalvm.js:js dependencies.
     *
     * @return the factory
     */
    public RegularExpressionFactory getRegularExpressionFactory() {
        return regularExpressionFactory;
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
     * Gets if schemas loaded from refs will be cached and reused for subsequent
     * runs.
     *
     * @return true if schemas loaded from refs should be cached
     */
    public boolean isCacheRefs() {
        return cacheRefs;
    }

    @Deprecated
    public boolean isCustomMessageSupported() {
        return this.errorMessageKeyword != null;
    }

    public String getErrorMessageKeyword() {
        return this.errorMessageKeyword;
    }

    /**
     * Gets whether to use a ECMA-262 compliant regular expression validator.
     * <p>
     * This defaults to the false and setting true require inclusion of optional
     * org.jruby.joni:joni or org.graalvm.js:js dependencies.
     *
     * @return true if ECMA-262 compliant
     */
    public boolean isEcma262Validator() {
        return !(this.regularExpressionFactory instanceof JDKRegularExpressionFactory);
    }
    
    public boolean isFailFast() {
        return this.failFast;
    }

    /**
     * Deprecated use {{@link #isNullableKeywordEnabled()} instead.
     *
     * @return true if the nullable keyword is enabled
     */
    @Deprecated
    public boolean isHandleNullableField() {
        return isNullableKeywordEnabled();
    }

    /**
     * Gets if the nullable keyword is enabled.
     *
     * @return true if the nullable keyword is enabled
     */
    public boolean isNullableKeywordEnabled() {
        return this.nullableKeywordEnabled;
    }

    public boolean isJavaSemantics() {
        return this.javaSemantics;
    }

    public boolean isLosslessNarrowing() {
        return this.losslessNarrowing;
    }

    /**
     * Indicates whether OpenAPI 3 style discriminators should be supported
     * <p>
     * Deprecated use {{@link #isDiscriminatorKeywordEnabled()} instead.
     * 
     * @return true in case discriminators are enabled
     * @since 1.0.51
     */
    @Deprecated
    public boolean isOpenAPI3StyleDiscriminators() {
        return isDiscriminatorKeywordEnabled();
    }

    /**
     * Gets if the discriminator keyword is enabled.
     * 
     * @return true if the discriminator keyword is enabled
     */
    public boolean isDiscriminatorKeywordEnabled() {
        return this.discriminatorKeywordEnabled;
    }

    /**
     * Gets if the schema should be preloaded.
     * 
     * @return true if it should be preloaded
     */
    public boolean isPreloadJsonSchema() {
        return preloadJsonSchema;
    }

    public boolean isReadOnly() {
        return null != this.readOnly && this.readOnly;
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
     *
     * @return true if type loose is used.
     */
    public boolean isTypeLoose() {
        return this.typeLoose;
    }

    public boolean isWriteOnly() {
        return null != this.writeOnly && this.writeOnly;
    }

    public void setApplyDefaultsStrategy(ApplyDefaultsStrategy applyDefaultsStrategy) {
        this.applyDefaultsStrategy = applyDefaultsStrategy != null ? applyDefaultsStrategy
                : ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;
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

    /**
     * Sets whether custom error messages in the schema are used.
     * <p>
     * This is deprecated in favor of setting the error message keyword to use.
     *
     * @param customMessageSupported true to use message as the error message keyword
     */
    @Deprecated
    public void setCustomMessageSupported(boolean customMessageSupported) {
        this.errorMessageKeyword = customMessageSupported ? "message" : null;
    }

    /**
     * Sets whether to use a ECMA-262 compliant regular expression validator.
     * <p>
     * This defaults to the false and setting true require inclusion of optional
     * org.jruby.joni:joni or org.graalvm.js:js dependencies.
     *
     * @param ecma262Validator true if ECMA-262 compliant
     */
    public void setEcma262Validator(boolean ecma262Validator) {
        this.regularExpressionFactory = ecma262Validator ? ECMAScriptRegularExpressionFactory.getInstance()
                : JDKRegularExpressionFactory.getInstance();
    }

    public void setExecutionContextCustomizer(ExecutionContextCustomizer executionContextCustomizer) {
        this.executionContextCustomizer = executionContextCustomizer;
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

    /**
     * Sets the format assertion enabled flag.
     * <p>
     * This is deprecated. Either set this using the builder
     * SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build() or
     * this should be set via
     * executionContext.getExecutionConfig().setFormatAssertionsEnabled(true).
     * 
     * @param formatAssertionsEnabled the format assertions enabled flag
     */
    @Deprecated
    public void setFormatAssertionsEnabled(Boolean formatAssertionsEnabled) {
        this.formatAssertionsEnabled = formatAssertionsEnabled;
    }

    public void setHandleNullableField(boolean handleNullableField) {
        this.nullableKeywordEnabled = handleNullableField;
    }

    public void setJavaSemantics(boolean javaSemantics) {
        this.javaSemantics = javaSemantics;
    }

    /**
     * Set the locale to consider when generating localised messages.
     * <p>
     * Note that this locale is set on a schema basis. To configure the schema on a
     * per execution basis use
     * {@link com.networknt.schema.ExecutionConfig#setLocale(Locale)}.
     * <p>
     * This is deprecated. Either set this using the builder
     * SchemaValidatorsConfig.builder().locale(locale).build() or this should be set
     * via executionContext.getExecutionConfig().setLocale(locale).
     *
     * @param locale The locale.
     */
    @Deprecated
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setLosslessNarrowing(boolean losslessNarrowing) {
        this.losslessNarrowing = losslessNarrowing;
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
     * @param openAPI3StyleDiscriminators whether discriminators should be used.
     *                                    Defaults to <code>false</code>
     * @since 1.0.51
     */
    public void setOpenAPI3StyleDiscriminators(boolean openAPI3StyleDiscriminators) {
        this.discriminatorKeywordEnabled = openAPI3StyleDiscriminators;
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
     * Sets if the schema should be preloaded.
     * 
     * @param preloadJsonSchema true to preload
     */
    public void setPreloadJsonSchema(boolean preloadJsonSchema) {
        this.preloadJsonSchema = preloadJsonSchema;
    }

    /**
     * Sets the max depth of the evaluation path to preload when preloading refs.
     *
     * @param preloadJsonSchemaRefMaxNestingDepth the max depth to preload
     */
    public void setPreloadJsonSchemaRefMaxNestingDepth(int preloadJsonSchemaRefMaxNestingDepth) {
        this.preloadJsonSchemaRefMaxNestingDepth = preloadJsonSchemaRefMaxNestingDepth;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Sets the regular expression factory.
     * <p>
     * This defaults to the JDKRegularExpressionFactory and the implementations
     * require inclusion of optional org.jruby.joni:joni or org.graalvm.js:js dependencies.
     *
     * @see JDKRegularExpressionFactory
     * @see ECMAScriptRegularExpressionFactory
     * @param regularExpressionFactory the factory
     */
    public void setRegularExpressionFactory(RegularExpressionFactory regularExpressionFactory) {
        this.regularExpressionFactory = regularExpressionFactory;
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

    public void setTypeLoose(boolean typeLoose) {
        this.typeLoose = typeLoose;
    }

    public void setWriteOnly(boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(SchemaValidatorsConfig config) {
        Builder builder = new Builder();
        builder.applyDefaultsStrategy = config.applyDefaultsStrategy;
        builder.cacheRefs = config.cacheRefs;
        builder.errorMessageKeyword = config.errorMessageKeyword;
        builder.executionContextCustomizer = config.executionContextCustomizer;
        builder.failFast = config.failFast;
        builder.formatAssertionsEnabled = config.formatAssertionsEnabled;
        builder.nullableKeywordEnabled = config.nullableKeywordEnabled;
        builder.itemWalkListeners = config.itemWalkListeners;
        builder.javaSemantics = config.javaSemantics;
        builder.keywordWalkListeners = config.keywordWalkListenersMap;
        builder.locale = config.locale;
        builder.losslessNarrowing = config.losslessNarrowing;
        builder.messageSource = config.messageSource;
        builder.discriminatorKeywordEnabled = config.discriminatorKeywordEnabled;
        builder.pathType = config.pathType;
        builder.preloadJsonSchema = config.preloadJsonSchema;
        builder.preloadJsonSchemaRefMaxNestingDepth = config.preloadJsonSchemaRefMaxNestingDepth;
        builder.propertyWalkListeners = config.propertyWalkListeners;
        builder.readOnly = config.readOnly;
        builder.regularExpressionFactory = config.regularExpressionFactory;
        builder.schemaIdValidator = config.schemaIdValidator;
        builder.strictness = config.strictness;
        builder.typeLoose = config.typeLoose;
        builder.writeOnly = config.writeOnly;
        return builder;
    }

    /**
     * Builder for {@link SchemaValidatorsConfig}.
     */
    public static class Builder {
        private ApplyDefaultsStrategy applyDefaultsStrategy = ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;
        private boolean cacheRefs = true;
        private String errorMessageKeyword = null;
        private ExecutionContextCustomizer executionContextCustomizer = null;
        private boolean failFast = false;
        private Boolean formatAssertionsEnabled = null;
        private boolean nullableKeywordEnabled = false;
        private List<JsonSchemaWalkListener> itemWalkListeners = new ArrayList<>();
        private boolean javaSemantics = false;
        private Map<String, List<JsonSchemaWalkListener>> keywordWalkListeners = new HashMap<>();
        private Locale locale = null; // This must be null to use Locale.getDefault() as the default can be changed
        private boolean losslessNarrowing = false;
        private MessageSource messageSource = null;
        private boolean discriminatorKeywordEnabled = false;
        private PathType pathType = PathType.JSON_POINTER;
        private boolean preloadJsonSchema = true;
        private int preloadJsonSchemaRefMaxNestingDepth = DEFAULT_PRELOAD_JSON_SCHEMA_REF_MAX_NESTING_DEPTH;
        private List<JsonSchemaWalkListener> propertyWalkListeners = new ArrayList<>();
        private Boolean readOnly = null;
        private RegularExpressionFactory regularExpressionFactory = JDKRegularExpressionFactory.getInstance();
        private JsonSchemaIdValidator schemaIdValidator = JsonSchemaIdValidator.DEFAULT;
        private Map<String, Boolean> strictness = new HashMap<>(0);
        private boolean typeLoose = false;
        private Boolean writeOnly = null;

        /**
         * Sets the strategy the walker uses to sets nodes to the default value.
         * <p>
         * Defaults to {@link ApplyDefaultsStrategy#EMPTY_APPLY_DEFAULTS_STRATEGY}.
         *
         * @param applyDefaultsStrategy the strategy
         * @return the builder
         */
        public Builder applyDefaultsStrategy(ApplyDefaultsStrategy applyDefaultsStrategy) {
            this.applyDefaultsStrategy = applyDefaultsStrategy != null ? applyDefaultsStrategy
                    : ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;
            return this;
        }
        /**
         * Sets if schemas loaded from refs will be cached and reused for subsequent runs.
         * <p>
         * Defaults to true.
         *
         * @param cacheRefs true to cache
         * @return the builder
         */
        public Builder cacheRefs(boolean cacheRefs) {
            this.cacheRefs = cacheRefs;
            return this;
        }
        /**
         * Sets the error message keyword for setting custom messages in the schema.
         * <p>
         * Defaults to null meaning custom messages are not enabled.
         * 
         * @param errorMessageKeyword to use for custom messages in the schema
         * @return the builder
         */
        public Builder errorMessageKeyword(String errorMessageKeyword) {
            this.errorMessageKeyword = errorMessageKeyword;
            return this;
        }
        /**
         * Sets the execution context customizer that is run before each run.
         *
         * @param executionContextCustomizer the customizer
         * @return the builder
         */
        public Builder executionContextCustomizer(ExecutionContextCustomizer executionContextCustomizer) {
            this.executionContextCustomizer = executionContextCustomizer;
            return this;
        }

        /**
         * Sets if the validation should immediately return once a validation error has
         * occurred. This can improve performance if inputs are invalid but cannot
         * return all error messages to the caller.
         * <p>
         * Defaults to false.
         * 
         * @param failFast true to enable
         * @return the builder
         */
        public Builder failFast(boolean failFast) {
            this.failFast = failFast;
            return this;
        }

        /**
         * Sets if format assertions are enabled. If format assertions are not enabled
         * the format keyword will behave like an annotation and not attempt to validate
         * if the inputs are valid.
         * <p>
         * Defaults to not enabling format assertions for Draft 2019-09 and above and
         * enabling format assertions for Draft 7 and below.
         * 
         * @param formatAssertionsEnabled true to enable
         * @return the builder
         */
        public Builder formatAssertionsEnabled(Boolean formatAssertionsEnabled) {
            this.formatAssertionsEnabled = formatAssertionsEnabled;
            return this;
        }

        /**
         * Sets if the nullable keyword is enabled.
         * 
         * @param nullableKeywordEnabled true to enable
         * @return the builder
         */
        public Builder nullableKeywordEnabled(boolean nullableKeywordEnabled) {
            this.nullableKeywordEnabled = nullableKeywordEnabled;
            return this;
        }
        public Builder itemWalkListeners(List<JsonSchemaWalkListener> itemWalkListeners) {
            this.itemWalkListeners = itemWalkListeners;
            return this;
        }
        public Builder javaSemantics(boolean javaSemantics) {
            this.javaSemantics = javaSemantics;
            return this;
        }
        public Builder keywordWalkListeners(Map<String, List<JsonSchemaWalkListener>> keywordWalkListeners) {
            this.keywordWalkListeners = keywordWalkListeners;
            return this;
        }
        /**
         * Set the locale to consider when generating localised messages.
         * <p>
         * Note that this locale is set on a schema basis. To configure the schema on a
         * per execution basis use
         * {@link com.networknt.schema.ExecutionConfig#setLocale(Locale)}.
         * <p>
         * Defaults to use {@link Locale#getDefault()}.
         *
         * @param locale The locale.
         * @return the builder
         */
        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }
        public Builder losslessNarrowing(boolean losslessNarrowing) {
            this.losslessNarrowing = losslessNarrowing;
            return this;
        }
        /**
         * Sets the message source to use for generating localised messages.
         *
         * @param messageSource the message source
         * @return the builder
         */
        public Builder messageSource(MessageSource messageSource) {
            this.messageSource = messageSource;
            return this;
        }
        /**
         * Sets if the discriminator keyword is enabled.
         * <p>
         * Defaults to false.
         * 
         * @param discriminatorKeywordEnabled true to enable
         * @return the builder
         */
        public Builder discriminatorKeywordEnabled(boolean discriminatorKeywordEnabled) {
            this.discriminatorKeywordEnabled = discriminatorKeywordEnabled;
            return this;
        }
        /**
         * Sets the path type to use when reporting the instance location of errors.
         * <p>
         * Defaults to {@link PathType#JSON_POINTER}.
         *
         * @param pathType the path type
         * @return the path type
         */
        public Builder pathType(PathType pathType) {
            this.pathType = pathType;
            return this;
        }
        /**
         * Sets if the schema should be preloaded.
         * <p>
         * Defaults to true.
         *
         * @param preloadJsonSchema true to preload
         * @return the builder
         */
        public Builder preloadJsonSchema(boolean preloadJsonSchema) {
            this.preloadJsonSchema = preloadJsonSchema;
            return this;
        }
        /**
         * Sets the max depth of the evaluation path to preload when preloading refs.
         * <p>
         * Defaults to 40.
         *
         * @param preloadJsonSchemaRefMaxNestingDepth to preload
         * @return the builder
         */
        public Builder preloadJsonSchemaRefMaxNestingDepth(int preloadJsonSchemaRefMaxNestingDepth) {
            this.preloadJsonSchemaRefMaxNestingDepth = preloadJsonSchemaRefMaxNestingDepth;
            return this;
        }
        public Builder propertyWalkListeners(List<JsonSchemaWalkListener> propertyWalkListeners) {
            this.propertyWalkListeners = propertyWalkListeners;
            return this;
        }
        public Builder readOnly(Boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }
        /**
         * Sets the regular expression factory.
         * <p>
         * Defaults to the {@link JDKRegularExpressionFactory}
         * <p>
         * The {@link ECMAScriptRegularExpressionFactory} requires the inclusion of
         * optional org.jruby.joni:joni or org.graalvm.js:js dependencies.
         *
         * @see JDKRegularExpressionFactory
         * @see ECMAScriptRegularExpressionFactory
         * @param regularExpressionFactory the factory
         * @return the builder
         */
        public Builder regularExpressionFactory(RegularExpressionFactory regularExpressionFactory) {
            this.regularExpressionFactory = regularExpressionFactory;
            return this;
        }
        /**
         * Sets the schema id validator to use.
         * <p>
         * Defaults to {@link JsonSchemaIdValidator#DEFAULT}.
         *
         * @param schemaIdValidator the builder
         * @return the builder
         */
        public Builder schemaIdValidator(JsonSchemaIdValidator schemaIdValidator) {
            this.schemaIdValidator = schemaIdValidator;
            return this;
        }
        public Builder strict(Map<String, Boolean> strict) {
            this.strictness = strict;
            return this;
        }
        public Builder typeLoose(boolean typeLoose) {
            this.typeLoose = typeLoose;
            return this;
        }
        public Builder writeOnly(Boolean writeOnly) {
            this.writeOnly = writeOnly;
            return this;
        }
        public SchemaValidatorsConfig build() {
            return new ImmutableSchemaValidatorsConfig(applyDefaultsStrategy, cacheRefs, errorMessageKeyword,
                    executionContextCustomizer, failFast, formatAssertionsEnabled, nullableKeywordEnabled,
                    itemWalkListeners, javaSemantics, keywordWalkListeners, locale, losslessNarrowing, messageSource,
                    discriminatorKeywordEnabled, pathType, preloadJsonSchema, preloadJsonSchemaRefMaxNestingDepth,
                    propertyWalkListeners, readOnly, regularExpressionFactory, schemaIdValidator, strictness, typeLoose,
                    writeOnly);
        }
        public Builder strict(String keyword, boolean strict) {
            this.strictness.put(Objects.requireNonNull(keyword, "keyword cannot be null"), strict);
            return this;
        }
        public Builder keywordWalkListener(String keyword, JsonSchemaWalkListener keywordWalkListener) {
            this.keywordWalkListeners.computeIfAbsent(keyword, key -> new ArrayList<>()).add(keywordWalkListener);
            return this;
        }
        public Builder keywordWalkListener(JsonSchemaWalkListener keywordWalkListener) {
            return keywordWalkListener(ALL_KEYWORD_WALK_LISTENER_KEY, keywordWalkListener);
        }
        public Builder keywordWalkListeners(Consumer<Map<String, List<JsonSchemaWalkListener>>> keywordWalkListeners) {
            keywordWalkListeners.accept(this.keywordWalkListeners);
            return this;
        }
        public Builder propertyWalkListener(JsonSchemaWalkListener propertyWalkListener) {
            this.propertyWalkListeners.add(propertyWalkListener);
            return this;
        }
        public Builder propertyWalkListeners(Consumer<List<JsonSchemaWalkListener>> propertyWalkListeners) {
            propertyWalkListeners.accept(this.propertyWalkListeners);
            return this;
        }
        public Builder itemWalkListener(JsonSchemaWalkListener itemWalkListener) {
            this.itemWalkListeners.add(itemWalkListener);
            return this;
        }
        public Builder itemWalkListeners(Consumer<List<JsonSchemaWalkListener>> itemWalkListeners) {
            itemWalkListeners.accept(this.itemWalkListeners);
            return this;
        }
    }

    /**
     * {@link SchemaValidatorsConfig} that throws on mutators or deprecated methods.
     * <p>
     * The {@link SchemaValidatorsConfig} will be made immutable in a future breaking release.
     */
    public static class ImmutableSchemaValidatorsConfig extends SchemaValidatorsConfig {
        public ImmutableSchemaValidatorsConfig(ApplyDefaultsStrategy applyDefaultsStrategy, boolean cacheRefs,
                String errorMessageKeyword, ExecutionContextCustomizer executionContextCustomizer, boolean failFast,
                Boolean formatAssertionsEnabled, boolean handleNullableField,
                List<JsonSchemaWalkListener> itemWalkListeners, boolean javaSemantics,
                Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap, Locale locale,
                boolean losslessNarrowing, MessageSource messageSource, boolean openAPI3StyleDiscriminators,
                PathType pathType, boolean preloadJsonSchema, int preloadJsonSchemaRefMaxNestingDepth,
                List<JsonSchemaWalkListener> propertyWalkListeners, Boolean readOnly,
                RegularExpressionFactory regularExpressionFactory, JsonSchemaIdValidator schemaIdValidator,
                Map<String, Boolean> strictness, boolean typeLoose, Boolean writeOnly) {
            super(applyDefaultsStrategy, cacheRefs, errorMessageKeyword, executionContextCustomizer, failFast,
                    formatAssertionsEnabled, handleNullableField, itemWalkListeners, javaSemantics, keywordWalkListenersMap, locale,
                    losslessNarrowing, messageSource, openAPI3StyleDiscriminators, pathType, preloadJsonSchema,
                    preloadJsonSchemaRefMaxNestingDepth, propertyWalkListeners, readOnly, regularExpressionFactory,
                    schemaIdValidator, strictness, typeLoose, writeOnly);
        }

        @Override
        public void addItemWalkListener(JsonSchemaWalkListener itemWalkListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addItemWalkListeners(List<JsonSchemaWalkListener> itemWalkListeners) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addKeywordWalkListener(JsonSchemaWalkListener keywordWalkListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addKeywordWalkListener(String keyword, JsonSchemaWalkListener keywordWalkListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addKeywordWalkListeners(List<JsonSchemaWalkListener> keywordWalkListeners) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addKeywordWalkListeners(String keyword, List<JsonSchemaWalkListener> keywordWalkListeners) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPropertyWalkListener(JsonSchemaWalkListener propertyWalkListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPropertyWalkListeners(List<JsonSchemaWalkListener> propertyWalkListeners) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setApplyDefaultsStrategy(ApplyDefaultsStrategy applyDefaultsStrategy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCacheRefs(boolean cacheRefs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCustomMessageSupported(boolean customMessageSupported) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEcma262Validator(boolean ecma262Validator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setExecutionContextCustomizer(ExecutionContextCustomizer executionContextCustomizer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setFailFast(boolean failFast) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setFormatAssertionsEnabled(Boolean formatAssertionsEnabled) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHandleNullableField(boolean handleNullableField) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setJavaSemantics(boolean javaSemantics) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLocale(Locale locale) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLosslessNarrowing(boolean losslessNarrowing) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMessageSource(MessageSource messageSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOpenAPI3StyleDiscriminators(boolean openAPI3StyleDiscriminators) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPathType(PathType pathType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPreloadJsonSchema(boolean preloadJsonSchema) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPreloadJsonSchemaRefMaxNestingDepth(int preloadJsonSchemaRefMaxNestingDepth) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRegularExpressionFactory(RegularExpressionFactory regularExpressionFactory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSchemaIdValidator(JsonSchemaIdValidator schemaIdValidator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStrict(String keyword, boolean strict) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTypeLoose(boolean typeLoose) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setWriteOnly(boolean writeOnly) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLoadCollectors(boolean loadCollectors) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SchemaValidatorsConfig disableUnevaluatedAnalysis() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SchemaValidatorsConfig disableUnevaluatedItems() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SchemaValidatorsConfig disableUnevaluatedProperties() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SchemaValidatorsConfig enableUnevaluatedAnalysis() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SchemaValidatorsConfig enableUnevaluatedItems() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SchemaValidatorsConfig enableUnevaluatedProperties() {
            throw new UnsupportedOperationException();
        }
    }

    /* Below are deprecated for removal */
    @Deprecated
    private boolean loadCollectors = true;
    
    /**
     * Use {@code isReadOnly} or {@code isWriteOnly}
     * @return true if schema is used to write data
     */
    @Deprecated
    public boolean isWriteMode() {
        return null == this.writeOnly || this.writeOnly;
    }
    
    /**
     * Sets if collectors are to be loaded.
     * <p>
     * This is deprecated in favor of the caller calling {@link CollectorContext#loadCollectors()} manually.
     * 
     * @param loadCollectors to load collectors
     */
    @Deprecated
    public void setLoadCollectors(boolean loadCollectors) {
        this.loadCollectors = loadCollectors;
    }

    /**
     * Gets if collectors are to be loaded.
     * 
     * @return if collectors are to be loader
     */
    @Deprecated
    public boolean doLoadCollectors() {
        return this.loadCollectors;
    }

    @Deprecated
    public SchemaValidatorsConfig disableUnevaluatedAnalysis() {
        return this;
    }

    @Deprecated
    public SchemaValidatorsConfig disableUnevaluatedItems() {
        return this;
    }

    @Deprecated
    public SchemaValidatorsConfig disableUnevaluatedProperties() {
        return this;
    }

    @Deprecated
    public SchemaValidatorsConfig enableUnevaluatedAnalysis() {
        return this;
    }

    @Deprecated
    public SchemaValidatorsConfig enableUnevaluatedItems() {
        return this;
    }

    @Deprecated
    public SchemaValidatorsConfig enableUnevaluatedProperties() {
        return this;
    }

    @Deprecated
    public boolean isUnevaluatedItemsAnalysisDisabled() {
        return false;
    }

    @Deprecated
    public boolean isUnevaluatedItemsAnalysisEnabled() {
        return !isUnevaluatedItemsAnalysisDisabled();
    }

    @Deprecated
    public boolean isUnevaluatedPropertiesAnalysisDisabled() {
        return false;
    }

    @Deprecated
    public boolean isUnevaluatedPropertiesAnalysisEnabled() {
        return !isUnevaluatedPropertiesAnalysisDisabled();
    }
}
