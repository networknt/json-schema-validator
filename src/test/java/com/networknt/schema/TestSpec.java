package com.networknt.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashSet;
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
     * @param validationMessages A sequence of validation messages expected from testing data against the schema
     * @param disabled Indicates whether this test should be executed (Defaults to FALSE)
     * @param isTypeLoose Indicates whether the test should consider a strict definition of an enum (Defaults to FALSE)
     */
    @JsonCreator
    public TestSpec(
        @JsonProperty("description") String description,
        @JsonProperty("comment") String comment,
        @JsonProperty("data") JsonNode data,
        @JsonProperty("valid") boolean valid,
        @JsonProperty("validationMessages") Set<String> validationMessages,
        @JsonProperty("isTypeLoose") Boolean isTypeLoose,
        @JsonProperty("disabled") Boolean disabled
    ) {
        this.description = description;
        this.comment = comment;
        this.data = data;
        this.valid = valid;
        this.validationMessages = validationMessages;
        this.disabled = Boolean.TRUE.equals(disabled);
        this.typeLoose = Boolean.TRUE.equals(isTypeLoose);
    }

    /**
     * The TestCase that contains this TestSpec.
     * @return the owning TestCase
     */
    public TestCase getTestCase() {
        return testCase;
    }

    /**
     * Changes the TestCase that contains this TestSpec.
     * @param testCase the owning TestCase
     */
    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * The test description, briefly explaining which behavior it exercises
     * (Required)
     */
    public String getDescription() {
        return description;
    }

    /**
     * Any additional comments about the test
     */
    public String getComment() {
        return comment;
    }

    /**
     * The instance which should be validated against the schema in "schema".
     * (Required)
     */
    public JsonNode getData() {
        return data;
    }

    /**
     * Indicates whether this test should be executed
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Whether the validation process of this instance should consider the instance
     * valid or not (Required)
     */
    public boolean isValid() {
        return valid;
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
        return new HashSet<>(null != validationMessages ? validationMessages : Collections.emptySet());
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
        return typeLoose;
    }

}
