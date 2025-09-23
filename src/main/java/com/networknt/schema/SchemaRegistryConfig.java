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

import com.networknt.schema.i18n.DefaultMessageSource;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.regex.ECMAScriptRegularExpressionFactory;
import com.networknt.schema.regex.JDKRegularExpressionFactory;
import com.networknt.schema.regex.RegularExpressionFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for SchemaRegistry that applies to all the schemas its
 * validators that is managed by the SchemaRegistry.
 */
public class SchemaRegistryConfig {
    private static class Holder {
        private static final SchemaRegistryConfig INSTANCE = SchemaRegistryConfig.builder().build();
    }

    /**
     * Gets the default config instance.
     * 
     * @return the config
     */
    public static SchemaRegistryConfig getInstance() {
        return Holder.INSTANCE;
    }
    
    public static final int DEFAULT_PRELOAD_SCHEMA_REF_MAX_NESTING_DEPTH = 40;

    /**
     * The execution context customizer that runs by default for all schemas.
     */
    private final ExecutionContextCustomizer executionContextCustomizer;

    /**
     * Controls if schemas loaded from refs will be cached and reused for subsequent runs.
     */
    private final boolean cacheRefs;

    /**
     * When set to true, "messages" provided in schema are used for forming validation errors
     * else default messages are used
     */
    private final String errorMessageKeyword;

    /**
     * When set to true, validator process is stop immediately when a very first
     * validation error is discovered.
     */
    private final boolean failFast;

    /**
     * Since Draft 2019-09 format assertions are not enabled by default.
     */
    private final Boolean formatAssertionsEnabled;

    /**
     * When set to true, use Java-specific semantics rather than native JavaScript
     * semantics
     */
    private final boolean javaSemantics;

    /**
     * The Locale to consider when loading validation messages from the default resource bundle.
     */
    private final Locale locale;

    /**
     * When set to true, can interpret round doubles as integers
     */
    private final boolean losslessNarrowing;

    /**
     * The message source to use for generating localised messages.
     */
    private final MessageSource messageSource;

    /**
     * The approach used to generate paths in reported messages, logs and errors. Default is the legacy "JSONPath-like" approach.
     */
    private final PathType pathType;

    /**
     * Controls if the schema will automatically be preloaded.
     */
    private final boolean preloadSchema;

    /**
     * Controls the max depth of the evaluation path to preload when preloading refs.
     */
    private final int preloadSchemaRefMaxNestingDepth;

    /**
     * Used to create {@link com.networknt.schema.regex.RegularExpression}.
     */
    private final RegularExpressionFactory regularExpressionFactory;

    /**
     * Used to validate the acceptable $id values.
     */
    private final SchemaIdValidator schemaIdValidator;
    
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

    protected SchemaRegistryConfig(boolean cacheRefs,
            String errorMessageKeyword, ExecutionContextCustomizer executionContextCustomizer, boolean failFast,
            Boolean formatAssertionsEnabled,
            boolean javaSemantics,
            Locale locale, boolean losslessNarrowing,
            MessageSource messageSource, PathType pathType,
            boolean preloadSchema, int preloadSchemaRefMaxNestingDepth,
            RegularExpressionFactory regularExpressionFactory, SchemaIdValidator schemaIdValidator,
            Map<String, Boolean> strictness, boolean typeLoose) {
        super();
        this.cacheRefs = cacheRefs;
        this.errorMessageKeyword = errorMessageKeyword;
        this.executionContextCustomizer = executionContextCustomizer;
        this.failFast = failFast;
        this.formatAssertionsEnabled = formatAssertionsEnabled;
        this.javaSemantics = javaSemantics;
        this.locale = locale;
        this.losslessNarrowing = losslessNarrowing;
        this.messageSource = messageSource;
        this.pathType = pathType;
        this.preloadSchema = preloadSchema;
        this.preloadSchemaRefMaxNestingDepth = preloadSchemaRefMaxNestingDepth;
        this.regularExpressionFactory = regularExpressionFactory;
        this.schemaIdValidator = schemaIdValidator;
        this.strictness = strictness;
        this.typeLoose = typeLoose;
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
    public int getPreloadSchemaRefMaxNestingDepth() {
        return preloadSchemaRefMaxNestingDepth;
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
    public SchemaIdValidator getSchemaIdValidator() {
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

    public String getErrorMessageKeyword() {
        return this.errorMessageKeyword;
    }

    public boolean isFailFast() {
        return this.failFast;
    }

    public boolean isJavaSemantics() {
        return this.javaSemantics;
    }

    public boolean isLosslessNarrowing() {
        return this.losslessNarrowing;
    }

    /**
     * Gets if the schema should be preloaded.
     * 
     * @return true if it should be preloaded
     */
    public boolean isPreloadSchema() {
        return preloadSchema;
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
	 * Returns whether types are interpreted in a loose manner.
	 * <p>
	 * If set to true, a single value can be interpreted as a size 1 array. Strings
	 * may also be interpreted as number, integer or boolean.
	 *
	 * @return true if type are interpreted in a loose manner
	 */
    public boolean isTypeLoose() {
        return this.typeLoose;
    }

    /**
     * Creates a builder.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Copies values from a configuration to a new builder. 
     *
     * @param config the configuration
     * @return the builder
     */
    public static Builder builder(SchemaRegistryConfig config) {
        Builder builder = new Builder();
        builder.cacheRefs = config.cacheRefs;
        builder.errorMessageKeyword = config.errorMessageKeyword;
        builder.executionContextCustomizer = config.executionContextCustomizer;
        builder.failFast = config.failFast;
        builder.formatAssertionsEnabled = config.formatAssertionsEnabled;
        builder.javaSemantics = config.javaSemantics;
        builder.locale = config.locale;
        builder.losslessNarrowing = config.losslessNarrowing;
        builder.messageSource = config.messageSource;
        builder.pathType = config.pathType;
        builder.preloadSchema = config.preloadSchema;
        builder.preloadSchemaRefMaxNestingDepth = config.preloadSchemaRefMaxNestingDepth;
        builder.regularExpressionFactory = config.regularExpressionFactory;
        builder.schemaIdValidator = config.schemaIdValidator;
        builder.strictness = config.strictness;
        builder.typeLoose = config.typeLoose;
        return builder;
    }

    /**
     * Builder for {@link SchemaRegistryConfig}.
     */
    public static class Builder extends BuilderSupport<Builder> {
		@Override
		protected Builder self() {
			return this;
		}
    }

    /**
     * Builder for {@link SchemaRegistryConfig}.
     */
    public static abstract class BuilderSupport<T> {
        protected boolean cacheRefs = true;
        protected String errorMessageKeyword = null;
        protected ExecutionContextCustomizer executionContextCustomizer = null;
        protected boolean failFast = false;
        protected Boolean formatAssertionsEnabled = null;
        protected boolean javaSemantics = false;
        protected Locale locale = null; // This must be null to use Locale.getDefault() as the default can be changed
        protected boolean losslessNarrowing = false;
        protected MessageSource messageSource = null;
        protected PathType pathType = PathType.JSON_POINTER;
        protected boolean preloadSchema = true;
        protected int preloadSchemaRefMaxNestingDepth = DEFAULT_PRELOAD_SCHEMA_REF_MAX_NESTING_DEPTH;
        protected RegularExpressionFactory regularExpressionFactory = JDKRegularExpressionFactory.getInstance();
        protected SchemaIdValidator schemaIdValidator = SchemaIdValidator.DEFAULT;
        protected Map<String, Boolean> strictness = new HashMap<>(0);
        protected boolean typeLoose = false;

        protected abstract T self();

        /**
         * Sets if schemas loaded from refs will be cached and reused for subsequent runs.
         * <p>
         * Defaults to true.
         *
         * @param cacheRefs true to cache
         * @return the builder
         */
        public T cacheRefs(boolean cacheRefs) {
            this.cacheRefs = cacheRefs;
            return self();
        }
        /**
         * Sets the error message keyword for setting custom messages in the schema.
         * <p>
         * Defaults to null meaning custom messages are not enabled.
         * 
         * @param errorMessageKeyword to use for custom messages in the schema
         * @return the builder
         */
        public T errorMessageKeyword(String errorMessageKeyword) {
            this.errorMessageKeyword = errorMessageKeyword;
            return self();
        }
        /**
         * Sets the execution context customizer that is run before each run.
         *
         * @param executionContextCustomizer the customizer
         * @return the builder
         */
        public T executionContextCustomizer(ExecutionContextCustomizer executionContextCustomizer) {
            this.executionContextCustomizer = executionContextCustomizer;
            return self();
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
        public T failFast(boolean failFast) {
            this.failFast = failFast;
            return self();
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
        public T formatAssertionsEnabled(Boolean formatAssertionsEnabled) {
            this.formatAssertionsEnabled = formatAssertionsEnabled;
            return self();
        }

        public T javaSemantics(boolean javaSemantics) {
            this.javaSemantics = javaSemantics;
            return self();
        }

		/**
		 * Set the locale to consider when generating localized messages.
		 * <p>
		 * Note that this locale is set on a schema registry basis. To configure the
		 * schema on a per execution basis use
		 * {@link com.networknt.schema.ExecutionConfig.Builder#locale(Locale)}.
		 * <p>
		 * Defaults to use {@link Locale#getDefault()}.
		 *
		 * @param locale The locale.
		 * @return the builder
		 */
        public T locale(Locale locale) {
            this.locale = locale;
            return self();
        }

        public T losslessNarrowing(boolean losslessNarrowing) {
            this.losslessNarrowing = losslessNarrowing;
            return self();
        }
        /**
         * Sets the message source to use for generating localised messages.
         *
         * @param messageSource the message source
         * @return the builder
         */
        public T messageSource(MessageSource messageSource) {
            this.messageSource = messageSource;
            return self();
        }
        /**
         * Sets the path type to use when reporting the instance location of errors.
         * <p>
         * Defaults to {@link PathType#JSON_POINTER}.
         *
         * @param pathType the path type
         * @return the path type
         */
        public T pathType(PathType pathType) {
            this.pathType = pathType;
            return self();
        }
        /**
         * Sets if the schema should be preloaded.
         * <p>
         * Defaults to true.
         *
         * @param preloadSchema true to preload
         * @return the builder
         */
        public T preloadSchema(boolean preloadSchema) {
            this.preloadSchema = preloadSchema;
            return self();
        }
        /**
         * Sets the max depth of the evaluation path to preload when preloading refs.
         * <p>
         * Defaults to 40.
         *
         * @param preloadSchemaRefMaxNestingDepth to preload
         * @return the builder
         */
        public T preloadSchemaRefMaxNestingDepth(int preloadSchemaRefMaxNestingDepth) {
            this.preloadSchemaRefMaxNestingDepth = preloadSchemaRefMaxNestingDepth;
            return self();
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
        public T regularExpressionFactory(RegularExpressionFactory regularExpressionFactory) {
            this.regularExpressionFactory = regularExpressionFactory;
            return self();
        }
        /**
         * Sets the schema id validator to use.
         * <p>
         * Defaults to {@link SchemaIdValidator#DEFAULT}.
         *
         * @param schemaIdValidator the builder
         * @return the builder
         */
        public T schemaIdValidator(SchemaIdValidator schemaIdValidator) {
            this.schemaIdValidator = schemaIdValidator;
            return self();
        }
        public T strict(Map<String, Boolean> strict) {
            this.strictness = strict;
            return self();
        }
        public T strict(String keyword, boolean strict) {
            this.strictness.put(Objects.requireNonNull(keyword, "keyword cannot be null"), strict);
            return self();
        }
        public T typeLoose(boolean typeLoose) {
            this.typeLoose = typeLoose;
            return self();
        }
        public SchemaRegistryConfig build() {
            return new SchemaRegistryConfig(cacheRefs, errorMessageKeyword,
                    executionContextCustomizer, failFast, formatAssertionsEnabled, 
                    javaSemantics, locale, losslessNarrowing, messageSource,
                    pathType, preloadSchema, preloadSchemaRefMaxNestingDepth,
                    regularExpressionFactory, schemaIdValidator, strictness, typeLoose
                    );
        }

    }
}
