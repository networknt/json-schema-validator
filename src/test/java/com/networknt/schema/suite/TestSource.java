package com.networknt.schema.suite;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.networknt.schema.serialization.JsonMapperFactory;

public class TestSource {
    protected static final TypeReference<List<TestCase>> testCaseType = new TypeReference<List<TestCase>>() { /* intentionally empty */};
    private static final ObjectMapper mapper = JsonMapperFactory.getInstance();

    /**
     * Indicates whether this test-source should be executed
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     * </p>
     */
    private final boolean disabled;

    /**
     * Describes why this test-source is disabled.
     * <p>
     * This is an extension of the schema used to describe tests in the
     * compliance suite
     * </p>
     */
    private final String reason;

    /**
     * The location of the specification file containing these test-cases.
     */
    private final Path path;

    private final List<TestCase> testCases;

    TestSource(Path path, List<TestCase> testCases, boolean disabled, String reason) {
        this.disabled = disabled;
        this.reason = reason;
        this.path = path;
        this.testCases = testCases;
        if (null != testCases) {
            testCases.forEach(c -> c.setSource(this));
        }
    }

    /**
     * Identifies the specification file containing these test-cases.
     * @return the path to the specification
     */
    public Path getPath() {
        return this.path;
    }

    public String getReason() {
        return this.reason;
    }

    public List<TestCase> getTestCases() {
        return null != this.testCases ? this.testCases : Collections.emptyList();
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public static Optional<TestSource> loadFrom(Path path, boolean disabled, String reason) {
        try (InputStream in = Files.newInputStream(path.toFile().toPath())) {
            List<TestCase> testCases = mapper.readValue(in, testCaseType);
            return Optional.of(new TestSource(path, testCases, disabled, reason));
        } catch (MismatchedInputException e) {
            System.err.append("Not a valid test case: ").println(path);
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
