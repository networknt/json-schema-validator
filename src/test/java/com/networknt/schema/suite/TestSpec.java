package com.networknt.schema.suite;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SchemaValidatorsConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A single test
 */
public class TestSpec {

    /**
     * The test description, briefly explaining which behavior it exercises
     * (Required)
     */
    private final String description;

    /**
     * Any additional comments about the test
     */
    private final String comment;

    /**
     * The instance which should be validated against the schema in "schema".
     * (Required)
     */
    private final JsonNode data;

    /**
     * Whether the validation process of this instance should consider the instance
     * valid or not (Required)
     */
    private final boolean valid;

    /**
     * A mapping of how strict a keyword's validators should be. Defaults to
     * {@literal true}.
     * <p>
     * Each validator has its own understanding of what constitutes strict
     * and permissive.
     * <p>
     * This is an extension of the schema used to describe tests in the compliance suite
     */
    private final Map<String, Boolean> strictness = new HashMap<>(0);

    /**
     * The set of validation messages expected from testing data against the schema
     * <p>
     * This is an extension of the schema used to describe tests in the compliance suite
     * </p>
     */
    private final Set<String> validationMessages;

    /**
     * Indicates whether this test should be executed
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     * </p>
     */
    private final boolean disabled;

    /**
     * Describes why this test is disabled.
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     * </p>
     */
    private final String reason;

    /**
     * Indicates whether the test should consider a strict definition of an
     * enum. This is related to earlier versions of the OpenAPI specification
     * that did not faithfully follow the JSON Schema specification.
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     * </p>
     * 
     * @deprecated This property only appears in a single V4 tests that are not
     *             in the validation suite. It does not appear in the tests
     *             related to any version of the OpenAPI specification.
     */
    private final boolean typeLoose;

    /**
     * Identifies the regular expression engine to use for this test-case.
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     */
    private final RegexKind regex;

    /**
     * Config information to be provided for {@link SchemaValidatorsConfig} with which schema can be validated
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     */
    private final Map<String, Object> config;

    /**
     * The TestCase that contains this TestSpec.
     */
    private TestCase testCase;

    /**
     * Constructs a new TestSpec
     * 
     * @param description The test description, briefly explaining which behavior it exercises (Required)
     * @param comment Any additional comments about the test
     * @param data The instance which should be validated against the schema in "schema" (Required)
     * @param valid Whether the validation process of this instance should consider the instance valid or not (Required)
     * @param strictness A mapping of how strict a keyword's validators should be.
     * @param validationMessages A sequence of validation messages expected from testing data against the schema
     * @param disabled Indicates whether this test should be executed (Defaults to FALSE)
     * @param isTypeLoose Indicates whether the test should consider a strict definition of an enum (Defaults to FALSE)
     */
    @JsonCreator
    public TestSpec(
        @JsonProperty("description") String description,
        @JsonProperty("comment") String comment,
        @JsonProperty("config") Map<String, Object> config,
        @JsonProperty("data") JsonNode data,
        @JsonProperty("valid") boolean valid,
        @JsonProperty("strictness") Map<String, Boolean> strictness,
        @JsonProperty("validationMessages") Set<String> validationMessages,
        @JsonProperty("isTypeLoose") Boolean isTypeLoose,
        @JsonProperty("disabled") Boolean disabled,
        @JsonProperty("reason") String reason,
        @JsonProperty(value = "regex", defaultValue = "unspecified") RegexKind regex
    ) {
        this.description = description;
        this.comment = comment;
        this.config = config;
        this.data = data;
        this.valid = valid;
        this.validationMessages = validationMessages;
        this.disabled = Boolean.TRUE.equals(disabled);
        this.reason = reason;
        this.typeLoose = Boolean.TRUE.equals(isTypeLoose);
        this.regex = regex;
        if (null != strictness) {
            this.strictness.putAll(strictness);
        }
    }

    /**
     * The TestCase that contains this TestSpec.
     * @return the owning TestCase
     */
    public TestCase getTestCase() {
        return this.testCase;
    }

    /**
     * Changes the TestCase that contains this TestSpec.
     * @param testCase the owning TestCase
     */
    void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * The test description, briefly explaining which behavior it exercises
     * (Required)
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Any additional comments about the test
     */
    public String getComment() {
        return this.comment;
    }


    /**
     * Config information to be provided for {@link SchemaValidatorsConfig} with which schema can be validated
     */
    public Map<String, Object> getConfig() {
        return config;
    }

    /**
     * The instance which should be validated against the schema in "schema".
     * (Required)
     */
    public JsonNode getData() {
        return this.data;
    }

    /**
     * Indicates whether this test should be executed
     */
    public boolean isDisabled() {
        return this.disabled || this.testCase.isDisabled();
    }

    /**
     * Describes why this test is disabled.
     */
    public String getReason() {
        return this.disabled ? this.reason : this.testCase.getReason();
    }

    /**
     * Whether the validation process of this instance should consider the instance
     * valid or not (Required)
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @return A mapping of how strict a keyword's validators should be (never null).
     */
    public Map<String, Boolean> getStrictness() {
        return this.strictness;
    }

    /**
     * A sequence of validation messages expected from testing data against the schema.
     * <p>
     * This is an extension of the schema used to describe tests in the compliance suite
     * </p>
     * 
     * @return a non-null list of expected validation messages
     */
    public Set<String> getValidationMessages() {
        return new HashSet<>(null != this.validationMessages ? this.validationMessages : Collections.emptySet());
    }

    /**
     * Indicates whether the test should consider a strict definition of an
     * enum. This is related to earlier versions of the OpenAPI specification
     * that did not faithfully follow the JSON Schema specification.
     * <p>
     * This is an extension of the schema used to describe tests in the compliance suite
     * </p>
     * 
     * @deprecated This property only appears in V4 tests that are not in the
     *             validation suite. It does not appear in the tests related
     *             to any version of the OpenAPI specification.
     */
    @Deprecated
    public boolean isTypeLoose() {
        return this.typeLoose;
    }

    public RegexKind getRegex() {
        return this.regex;
    }

    public enum RegexKind {
        @JsonProperty("unspecified") UNSPECIFIED,
        @JsonProperty("ecma-262") JONI,
        @JsonProperty("jdk") JDK
    }
}
