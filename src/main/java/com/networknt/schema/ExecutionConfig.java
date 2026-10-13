/*
 * Copyright (c) 2023 the original author or authors.
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

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Configuration per execution.
 */
public class ExecutionConfig {
    private static class Holder {
        private static final ExecutionConfig INSTANCE = ExecutionConfig.builder().build();
    }

    public static ExecutionConfig getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * The locale to use for formatting messages.
     */
    private final Locale locale;

    /**
     * Determines if annotation collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     */
    private final boolean annotationCollectionEnabled;

    /**
     * If annotation collection is enabled, determine which annotations to collect.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     */
    private final Predicate<String> annotationCollectionFilter;

    /**
     * Since Draft 2019-09 format assertions are not enabled by default.
     */
    private final Boolean formatAssertionsEnabled;

    /**
     * Determine if the validation execution can fail fast.
     */
    private final boolean failFast;

    /**
     * When set to true assumes that schema is used to validate incoming data from
     * an API.
     */
    private final Boolean readOnly;

    /**
     * When set to true assumes that schema is used to validate outgoing data from
     * an API.
     */
    private final Boolean writeOnly;

    private final long maxEvaluationSteps;
    private final int maxEvaluationDepth;

    /**
     * Legacy constructor with unlimited evaluation. Subclass builders that support
     * limits must use the builder constructor or the overload accepting both limits.
     *
     * @param locale the locale
     * @param annotationCollectionEnabled whether to report annotations
     * @param annotationCollectionFilter which annotations to report
     * @param formatAssertionsEnabled whether to assert formats
     * @param failFast whether to fail fast
     * @param readOnly the read-only policy
     * @param writeOnly the write-only policy
     */
    protected ExecutionConfig(Locale locale, boolean annotationCollectionEnabled,
            Predicate<String> annotationCollectionFilter, Boolean formatAssertionsEnabled, boolean failFast,
            Boolean readOnly, Boolean writeOnly) {
        this(locale, annotationCollectionEnabled, annotationCollectionFilter, formatAssertionsEnabled, failFast,
                readOnly, writeOnly, 0, 0);
    }

    /**
     * Creates configuration from all inherited builder options, including limits.
     * Subclass builders overriding {@code build()} can pass themselves to this
     * constructor rather than manually forwarding individual properties.
     *
     * @param builder the builder to snapshot
     */
    protected ExecutionConfig(BuilderSupport<?> builder) {
        this(builder.locale == null ? Locale.getDefault() : builder.locale, builder.annotationCollectionEnabled,
                Objects.requireNonNull(builder.annotationCollectionFilter, "annotationCollectionFilter must not be null"),
                builder.formatAssertionsEnabled, builder.failFast, builder.readOnly, builder.writeOnly,
                builder.maxEvaluationSteps, builder.maxEvaluationDepth);
    }

    protected ExecutionConfig(Locale locale, boolean annotationCollectionEnabled,
            Predicate<String> annotationCollectionFilter, Boolean formatAssertionsEnabled, boolean failFast,
            Boolean readOnly, Boolean writeOnly, long maxEvaluationSteps, int maxEvaluationDepth) {
        super();
        if (maxEvaluationSteps < 0 || maxEvaluationDepth < 0) {
            throw new IllegalArgumentException("Evaluation limits must not be negative");
        }
        this.locale = locale;
        this.annotationCollectionEnabled = annotationCollectionEnabled;
        this.annotationCollectionFilter = annotationCollectionFilter;
        this.formatAssertionsEnabled = formatAssertionsEnabled;
        this.failFast = failFast;
        this.readOnly = readOnly;
        this.writeOnly = writeOnly;
        this.maxEvaluationSteps = maxEvaluationSteps;
        this.maxEvaluationDepth = maxEvaluationDepth;
    }

    /** @return maximum admitted schema-entry and keyword-dispatch steps; zero means unlimited */
    public long getMaxEvaluationSteps() {
        return maxEvaluationSteps;
    }

    /** @return maximum active schema evaluation frames, including the root; zero means unlimited */
    public int getMaxEvaluationDepth() {
        return maxEvaluationDepth;
    }

    /**
     * Gets the locale to use for formatting messages.
     * 
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
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
     * Return if fast fail is enabled.
     * 
     * @return if fast fail is enabled
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * Return if annotation collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     * <p>
     * The annotations to collect can be customized using the annotation collection
     * predicate.
     * 
     * @return if annotation collection is enabled
     */
    public boolean isAnnotationCollectionEnabled() {
        return annotationCollectionEnabled;
    }

    /**
     * Gets the predicate to determine if annotation collection is allowed for a
     * particular keyword. This only has an effect if annotation collection is
     * enabled.
     * <p>
     * The default value is to not collect any annotation keywords if annotation
     * collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     * 
     * @return the predicate to determine if annotation collection is allowed for
     *         the keyword
     */
    public Predicate<String> getAnnotationCollectionFilter() {
        return annotationCollectionFilter;
    }

    /**
     * Returns the value of the read only flag.
     *
     * @return the value of read only flag or null if not set
     */
    public Boolean getReadOnly() {
        return this.readOnly;
    }

    /**
     * Returns the value of the write only flag.
     *
     * @return the value of the write only flag or null if not set
     */
    public Boolean getWriteOnly() {
        return this.writeOnly;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ExecutionConfig config) {
        Builder copy = new Builder();
        copy.locale = config.locale;
        copy.annotationCollectionEnabled = config.annotationCollectionEnabled;
        copy.annotationCollectionFilter = config.annotationCollectionFilter;
        copy.formatAssertionsEnabled = config.formatAssertionsEnabled;
        copy.failFast = config.failFast;
        copy.readOnly = config.readOnly;
        copy.writeOnly = config.writeOnly;
        copy.maxEvaluationSteps = config.maxEvaluationSteps;
        copy.maxEvaluationDepth = config.maxEvaluationDepth;
        return copy;
    }

    /**
     * Builder for {@link ExecutionConfig}.
     */
    public static class Builder extends BuilderSupport<Builder> {

        @Override
        protected Builder self() {
            return this;
        }
    }

    /**
     * Builder for {@link ExecutionConfig}.
     */
    public static abstract class BuilderSupport<T> {
        protected Locale locale = Locale.ROOT;
        protected boolean annotationCollectionEnabled = false;
        protected Predicate<String> annotationCollectionFilter = keyword -> false;
        protected Boolean formatAssertionsEnabled = null;
        protected boolean failFast = false;
        protected Boolean readOnly = null;
        protected Boolean writeOnly = null;
        protected long maxEvaluationSteps = 0;
        protected int maxEvaluationDepth = 0;

        protected abstract T self();

        /**
         * Sets the locale to use for formatting messages.
         * 
         * @param locale the locale
         * @return the builder
         */
        public T locale(Locale locale) {
            this.locale = locale;
            return self();
        }

        /**
         * Sets whether the annotation collection is enabled.
         * <p>
         * This does not affect annotation collection required for evaluating keywords
         * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
         * <p>
         * The annotations to collect can be customized using the annotation collection
         * predicate.
         * 
         * @param annotationCollectionEnabled true to enable annotation collection
         * @return the builder
         */
        public T annotationCollectionEnabled(boolean annotationCollectionEnabled) {
            this.annotationCollectionEnabled = annotationCollectionEnabled;
            return self();
        }

        /**
         * Predicate to determine if annotation collection is allowed for a particular
         * keyword. This only has an effect if annotation collection is enabled.
         * <p>
         * The default value is to not collect any annotation keywords if annotation
         * collection is enabled.
         * <p>
         * This does not affect annotation collection required for evaluating keywords
         * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
         *
         * @param annotationCollectionFilter the predicate accepting the keyword
         * @return the builder
         */
        public T annotationCollectionFilter(Predicate<String> annotationCollectionFilter) {
            this.annotationCollectionFilter = annotationCollectionFilter;
            return self();
        }

        /**
         * Sets the format assertion enabled flag.
         * 
         * @param formatAssertionsEnabled the format assertions enabled flag
         * @return the builder
         */
        public T formatAssertionsEnabled(Boolean formatAssertionsEnabled) {
            this.formatAssertionsEnabled = formatAssertionsEnabled;
            return self();
        }

        /**
         * Sets whether fast fail is enabled.
         * 
         * @param failFast true to fast fail
         * @return the builder
         */
        public T failFast(boolean failFast) {
            this.failFast = failFast;
            return self();
        }

        public T readOnly(Boolean readOnly) {
            this.readOnly = readOnly;
            return self();
        }

        public T writeOnly(Boolean writeOnly) {
            this.writeOnly = writeOnly;
            return self();
        }

        /**
         * Sets the maximum admitted schema-entry and keyword-dispatch steps per execution.
         * This does not interrupt work within a keyword. Zero (the default) means unlimited.
         *
         * @param maxEvaluationSteps nonnegative step capacity
         * @return the builder
         */
        public T maxEvaluationSteps(long maxEvaluationSteps) {
            this.maxEvaluationSteps = maxEvaluationSteps;
            return self();
        }

        /**
         * Sets the maximum simultaneously active schema frames, including the root.
         * This includes references and schema wrappers, rather than just JSON nesting.
         * Zero (the default) means unlimited.
         *
         * @param maxEvaluationDepth nonnegative depth capacity
         * @return the builder
         */
        public T maxEvaluationDepth(int maxEvaluationDepth) {
            this.maxEvaluationDepth = maxEvaluationDepth;
            return self();
        }

        /**
         * Builds the {@link ExecutionConfig}.
         * 
         * @return the execution configuration
         */
        public ExecutionConfig build() {
            return new ExecutionConfig(this);
        }
    }
}
