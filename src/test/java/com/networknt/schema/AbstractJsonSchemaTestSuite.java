/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import static com.networknt.schema.SpecVersionDetector.detectOptionalVersion;

import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.uri.URITranslator;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.DynamicNode;
import org.opentest4j.AssertionFailedError;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractJsonSchemaTestSuite extends HTTPServiceSupport {
    protected static final TypeReference<List<TestCase>> testCaseType = new TypeReference<List<TestCase>>() {};
    protected static final Map<String, VersionFlag> supportedVersions = new HashMap<>();
    static {
        supportedVersions.put("draft2019-09", VersionFlag.V201909);
        supportedVersions.put("draft2020-12", VersionFlag.V202012);
        supportedVersions.put("draft4", VersionFlag.V4);
        supportedVersions.put("draft6", VersionFlag.V6);
        supportedVersions.put("draft7", VersionFlag.V7);
    }

    protected ObjectMapper mapper = new ObjectMapper();

    protected Stream<DynamicNode> createTests(VersionFlag defaultVersion, String basePath) {
        return findTestCases(basePath)
            .stream()
            .peek(System.out::println)
            .flatMap(path -> buildContainers(defaultVersion, path));
    }

    protected boolean enabled(@SuppressWarnings("unused") Path path) {
        return true;
    }

    protected boolean enabled(TestCase testCase) {
        return !testCase.isDisabled();
    }

    protected boolean enabled(TestSpec testSpec) {
        return !testSpec.isDisabled();
    }

    private Stream<DynamicNode> buildContainers(VersionFlag defaultVersion, Path path) {
        return loadTestCases(path)
            .map(testCase -> buildContainer(defaultVersion, testCase));
    }

    private DynamicNode buildContainer(VersionFlag defaultVersion, TestCase testCase) {
        try {
            JsonSchemaFactory validatorFactory = buildValidatorFactory(defaultVersion, testCase);

            return dynamicContainer(testCase.getDisplayName(), testCase.getTests().stream().filter(this::enabled).map(testSpec -> {
                return buildTest(validatorFactory, testSpec);
            }));
        } catch (JsonSchemaException e) {
            String msg = e.getMessage();
            if (msg.endsWith("' is unrecognizable schema")) {
                return dynamicContainer(testCase.getDisplayName(), unsupportedMetaSchema(testCase));
            };
            throw e;
        }
    }

    private JsonSchemaFactory buildValidatorFactory(VersionFlag defaultVersion, TestCase testCase) {
        VersionFlag specVersion = detectVersion(testCase, defaultVersion);
        JsonSchemaFactory base = JsonSchemaFactory.getInstance(specVersion);
        return JsonSchemaFactory
    		.builder(base)
    		.objectMapper(mapper)
    		.addUriTranslator(URITranslator.combine(
				URITranslator.prefix("http://json-schema.org", "resource:"),
				URITranslator.prefix("https://json-schema.org", "resource:")
			))
    		.build();
    }

    private DynamicNode buildTest(JsonSchemaFactory validatorFactory, TestSpec testSpec) {
        // Configure the schemaValidator to set typeLoose's value based on the test file,
        // if test file do not contains typeLoose flag, use default value: false.
        @SuppressWarnings("deprecation") boolean typeLoose = testSpec.isTypeLoose();

        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setTypeLoose(typeLoose);
        testSpec.getStrictness().forEach(config::setStrict);
        URI testCaseFileUri = URI.create("classpath:" + testSpec.getTestCase().getSpecification());
        JsonSchema schema = validatorFactory.getSchema(testCaseFileUri, testSpec.getTestCase().getSchema(), config);

        return dynamicTest(testSpec.getDescription(), () -> executeAndReset(schema, testSpec));
    }

    // For 2019-09 and later published drafts, implementations that are able to
    // detect the draft of each schema via $schema SHOULD be configured to do so
    private VersionFlag detectVersion(TestCase testCase, VersionFlag defaultVersion) {
        return Stream.of(
            detectOptionalVersion(testCase.getSchema()),
            detectVersionFromPath(testCase.getSpecification())
        )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElse(defaultVersion);
    }

    // For draft-07 and earlier, draft-next, and implementations unable to
    // detect via $schema, implementations MUST be configured to expect the
    // draft matching the test directory name
    private Optional<VersionFlag> detectVersionFromPath(Path path) {
        return StreamSupport.stream(path.spliterator(), false)
            .map(Path::toString)
            .map(supportedVersions::get)
            .filter(Objects::nonNull)
            .findAny();
    }

    private void executeAndReset(JsonSchema schema, TestSpec testSpec) {
        try {
            executeTest(schema, testSpec);
        } finally {
            cleanup();
        }
    }

    private void executeTest(JsonSchema schema, TestSpec testSpec) {
        Set<ValidationMessage> errors = schema.validate(testSpec.getData());

        if (testSpec.isValid()) {
            if (!errors.isEmpty()) {
                String msg = new StringBuilder("Expected success")
                    .append("\n  description: ")
                    .append(testSpec.getDescription())
                    .append("\n  schema: ")
                    .append(schema)
                    .append("\n  data: ")
                    .append(testSpec.getData())
                    .toString();

                AssertionFailedError t = AssertionFailureBuilder.assertionFailure()
                    .message(msg)
                    .reason(errors.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\n    ", "\n  errors:\n    ", "")))
                    .build();
                t.setStackTrace(new StackTraceElement[0]);
                throw t;
            }
        } else {
            if (errors.isEmpty()) {
                String msg = new StringBuilder("Expected failure")
                    .append("\n  description: ")
                    .append(testSpec.getDescription())
                    .append("\n  schema: ")
                    .append(schema)
                    .append("\n  data: ")
                    .append(testSpec.getData())
                    .toString();

                AssertionFailedError t = AssertionFailureBuilder.assertionFailure()
                    .message(msg)
                    .build();
                t.setStackTrace(new StackTraceElement[0]);
                throw t;
            }
        }

        // Expected Validation Messages need not be exactly same as actual errors.
        // This code checks if expected validation message is subset of actual errors
        Set<String> actual = errors.stream().map(ValidationMessage::getMessage).collect(Collectors.toSet());
        Set<String> expected = testSpec.getValidationMessages();
        expected.removeAll(actual);
        if (!expected.isEmpty()) {
            String msg = new StringBuilder("Expected Validation Messages")
                .append("\n  description: ")
                .append(testSpec.getDescription())
                .append("\n  schema: ")
                .append(schema)
                .append("\n  data: ")
                .append(testSpec.getData())
                .append(actual.stream().collect(Collectors.joining("\n    ", "\n  errors:\n    ", "")))
                .toString();

            AssertionFailedError t = AssertionFailureBuilder.assertionFailure()
                .message(msg)
                .reason(expected.stream().collect(Collectors.joining("\n    ", "\n  expected:\n    ", "")))
                .build();
            t.setStackTrace(new StackTraceElement[0]);
            throw t;
        }
    }

    private List<Path> findTestCases(String basePath) {
        try (Stream<Path> paths = Files.walk(Paths.get(basePath))) {
            return paths
                .filter(this::enabled)
                .filter(path -> path.toString().endsWith(".json"))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Stream<TestCase> loadTestCases(Path testCaseFile) {
        try (InputStream in = new FileInputStream(testCaseFile.toFile())) {
            return mapper.readValue(in, testCaseType)
                .stream()
                .peek(testCase -> testCase.setSpecification(testCaseFile))
                .filter(this::enabled);
        } catch (MismatchedInputException e) {
            System.err.append("Not a valid test case: ").println(testCaseFile);
            return Stream.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Iterable<? extends DynamicNode> unsupportedMetaSchema(TestCase testCase) {
        return Collections.singleton(
            dynamicTest("Detected an unsupported schema", () -> {
                String schema = testCase.getSchema().asText();
                AssertionFailedError t = AssertionFailureBuilder.assertionFailure()
                    .message("Detected an unsupported schema: " + schema)
                    .reason("Future and custom meta-schemas are not supported")
                    .build();
                t.setStackTrace(new StackTraceElement[0]);
                throw t;
            })
        );
    }

}
