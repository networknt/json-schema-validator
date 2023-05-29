package com.networknt.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * An individual test case, containing multiple testSpecs of a single schema's
 * behavior
 */
public class TestCase {

    /**
     * The test case description (Required)
     */
    private final String description;

    /**
     * Any additional comments about the test case
     */
    private final String comment;

    /**
     * A valid JSON Schema (one written for the corresponding version directory that
     * the file sits within). (Required)
     */
    private final JsonNode schema;

    /**
     * A set of related tests all using the same schema (Required)
     */
    private final List<TestSpec> tests;

    /**
     * Indicates whether this test-case should be executed
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     * </p>
     */
    private final boolean disabled;

    /**
     * Describes why this test-case is disabled.
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     * </p>
     */
    private final String reason;

    /**
     * The location of the specification file containing this test-case.
     */
    private Path specification;

    /**
     * Constructs a new TestCase
     * 
     * @param description The test case description (Required)
     * @param comment Any additional comments about the test case
     * @param schema A valid JSON Schema (one written for the corresponding version directory that the file sits within) (Required)
     * @param tests A set of related tests all using the same schema (Required)
     */
    @JsonCreator
    public TestCase(
        @JsonProperty("description") String description,
        @JsonProperty("comment") String comment,
        @JsonProperty("schema") JsonNode schema,
        @JsonProperty("disabled") Boolean disabled,
        @JsonProperty("reason") String reason,
        @JsonProperty("tests") List<TestSpec> tests
    ) {
        this.description = description;
        this.comment = comment;
        this.schema = schema;
        this.disabled = Boolean.TRUE.equals(disabled);
        this.reason = reason;

        this.tests = tests;
        if (null != tests) {
            tests.forEach(test -> test.setTestCase(this));
        }
    }

    /**
     * Identifies the specification file containing this test-case.
     * @return the path to the specification
     */
    public Path getSpecification() {
        return this.specification;
    }

    /**
     * Sets the location of the specification file containing this test-case.
     * @param specification the path to the specification
     */
    public void setSpecification(Path specification) {
        this.specification = specification;
    }

    /**
     * The test case description (Required)
     */
    public String getDescription() {
        return this.description;
    }

    public String getDisplayName() {
        return String.format("%s (%s)", getDescription(), getSpecification());
    }

    /**
     * Any additional comments about the test case
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * A valid JSON Schema (one written for the corresponding version directory that
     * the file sits within). (Required)
     */
    public JsonNode getSchema() {
        return this.schema;
    }

    /**
     * Indicates whether this test-case should be executed
     */
    public boolean isDisabled() {
        return this.disabled;
    }

    /**
     * Describes why this test is disabled.
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * A set of related tests all using the same schema (Required)
     */
    public List<TestSpec> getTests() {
        return null != this.tests ? this.tests : Collections.emptyList();
    }

}
