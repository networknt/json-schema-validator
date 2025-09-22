package com.networknt.schema.benchmark;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.regex.JoniRegularExpressionFactory;
import com.networknt.schema.resource.InputStreamSource;
import com.networknt.schema.resource.SchemaLoader;
import com.networknt.schema.suite.TestCase;
import com.networknt.schema.suite.TestSource;

public class NetworkntTestSuiteTestCases {
    private static String toForwardSlashPath(Path file) {
        return file.toString().replace('\\', '/');
    }

    private static List<Path> findTestCasePaths(String basePath, Predicate<? super Path> filter) {
        try (Stream<Path> paths = Files.walk(Paths.get(basePath))) {
            return paths.filter(path -> path.toString().endsWith(".json")).filter(filter).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<NetworkntTestSuiteTestCase> findTestCases(Version defaultVersion, String basePath) {
        return findTestCases(defaultVersion, basePath, path -> true);
    }

    public static List<NetworkntTestSuiteTestCase> findTestCases(Version defaultVersion, String basePath,
            Predicate<? super Path> filter) {
        SchemaLoader schemaLoader = new SchemaLoader() {
            @Override
            public InputStreamSource getSchema(AbsoluteIri absoluteIri) {
                String iri = absoluteIri.toString();
                if (iri.startsWith("http://localhost:1234")) {
                    return () -> {
                      String path = iri.substring("http://localhost:1234".length());
                      return new FileInputStream("src/test/suite/remotes" + path);
                    };
                }
                return null;
            }
        };
        List<NetworkntTestSuiteTestCase> results = new ArrayList<>();
        List<Path> testCasePaths = findTestCasePaths(basePath, filter);
        for (Path path : testCasePaths) {
            Optional<TestSource> optionalTestSource = TestSource.loadFrom(path, false, "");
            if (optionalTestSource.isPresent()) {
                TestSource testSource = optionalTestSource.get();
                for (TestCase testCase : testSource.getTestCases()) {
                    SchemaLocation testCaseFileUri = SchemaLocation
                            .of("classpath:" + toForwardSlashPath(testCase.getSpecification()));
                    JsonSchema schema = JsonSchemaFactory
                            .getInstance(defaultVersion,
                                    builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.add(schemaLoader)))
                            .getSchema(testCaseFileUri, testCase.getSchema(), SchemaValidatorsConfig.builder()
                                    .regularExpressionFactory(JoniRegularExpressionFactory.getInstance()).build());
                    results.add(new NetworkntTestSuiteTestCase(schema, testCase,
                            testCase.getSource().getPath().getParent().toString().endsWith("format") ? true : null));
                }
            }
        }
        return results;
    }
}
