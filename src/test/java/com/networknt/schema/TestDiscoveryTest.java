/*
 * Copyright (c) 2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Fails when a test compiles but never runs: a test class whose name the Surefire includes don't
 * match, or a test-shaped method that has no test annotation.
 */
class TestDiscoveryTest {
    private static final Pattern TEST_ANNOTATION = Pattern.compile(
            "@(?:org\\.junit\\.(?:jupiter\\.(?:api|params)\\.)?)?(?:Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\\b");
    private static final Pattern JUNIT_METHOD_ANNOTATION = Pattern.compile(
            "@(?:org\\.junit\\.(?:jupiter\\.(?:api|params)\\.)?)?(?:Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate"
                    + "|BeforeEach|AfterEach|BeforeAll|AfterAll)\\b");
    // Surefire's default includes
    private static final Pattern SUREFIRE_CLASS_NAME = Pattern.compile("Test\\w*|\\w*Test|\\w*Tests|\\w*TestCase");
    // Run by name by the verify-packaged-boundary execution in pom.xml
    private static final Set<String> RUN_BY_NAME = Set.of("InternalPackageIT");
    private static final Pattern NO_ARGUMENT_VOID_METHOD = Pattern.compile(
            "^[ \\t]*(?:public[ \\t]+)?void[ \\t]+(\\w+)[ \\t]*\\([ \\t]*\\)", Pattern.MULTILINE);
    private static final Pattern CALL_OR_METHOD_REFERENCE = Pattern.compile("::(\\w+)|\\b(\\w+)\\s*\\(");
    private static final Pattern COMMENT_OR_LITERAL = Pattern.compile(
            "\"\"\"[\\s\\S]*?\"\"\"|\"(?:\\\\.|[^\"\\\\\\n])*\"|'(?:\\\\.|[^'\\\\\\n])*'|/\\*[\\s\\S]*?\\*/|//[^\\n]*");

    /**
     * A test source with its comments and string literals blanked out, keeping line breaks so that
     * offsets still map to line numbers.
     */
    private record TestSource(Path path, String className, String code) {
    }

    @Test
    void testClassesMatchTheSurefireIncludes() {
        List<String> misnamed = testSources().stream()
                .filter(source -> TEST_ANNOTATION.matcher(source.code()).find())
                .filter(source -> !source.code().contains("abstract class " + source.className()))
                .filter(source -> !SUREFIRE_CLASS_NAME.matcher(source.className()).matches())
                .filter(source -> !RUN_BY_NAME.contains(source.className()))
                .map(source -> source.path().toString())
                .collect(Collectors.toList());

        assertEquals(Collections.emptyList(), misnamed);
    }

    @Test
    void testShapedMethodsHaveATestAnnotation() {
        List<String> unannotated = testSources().stream()
                .filter(source -> TEST_ANNOTATION.matcher(source.code()).find())
                .flatMap(source -> unannotatedMethods(source).stream())
                .collect(Collectors.toList());

        assertEquals(Collections.emptyList(), unannotated);
    }

    /**
     * A package-private or public no-argument void method of the test class, without a JUnit annotation and
     * not called anywhere in its file, is a test that JUnit skips. Methods of nested and anonymous classes,
     * such as {@code Runnable.run()}, are not test candidates.
     */
    private static List<String> unannotatedMethods(TestSource source) {
        Map<String, Integer> callCounts = callCounts(source.code());
        List<String> unannotated = new ArrayList<>();
        Matcher method = NO_ARGUMENT_VOID_METHOD.matcher(source.code());
        while (method.find()) {
            String name = method.group(1);
            boolean calledElsewhere = callCounts.getOrDefault(name, 0) > 1; // the declaration itself is one match
            if (braceDepth(source.code(), method.start()) == 1 && !calledElsewhere
                    && !JUNIT_METHOD_ANNOTATION.matcher(annotationsBefore(source.code(), method.start())).find()) {
                unannotated.add(source.path() + ":" + lineNumber(source.code(), method.start(1)) + " " + name);
            }
        }
        return unannotated;
    }

    /**
     * Returns the code between the previous member, statement or block boundary and the method
     * declaration, which holds the method's annotations. Braces inside annotation arguments, as in
     * {@code @ValueSource(strings = {...})}, are skipped by tracking parentheses.
     */
    private static String annotationsBefore(String code, int declarationStart) {
        int depth = 0;
        int i = declarationStart - 1;
        for (; i >= 0; i--) {
            char c = code.charAt(i);
            if (c == ')') {
                depth++;
            } else if (c == '(') {
                depth--;
            } else if (depth == 0 && (c == ';' || c == '{' || c == '}')) {
                break;
            }
        }
        return code.substring(i + 1, declarationStart);
    }

    private static int braceDepth(String code, int offset) {
        int depth = 0;
        for (int i = 0; i < offset; i++) {
            char c = code.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        return depth;
    }

    private static Map<String, Integer> callCounts(String code) {
        Map<String, Integer> counts = new HashMap<>();
        Matcher call = CALL_OR_METHOD_REFERENCE.matcher(code);
        while (call.find()) {
            String name = call.group(1) != null ? call.group(1) : call.group(2);
            counts.merge(name, 1, Integer::sum);
        }
        return counts;
    }

    private static int lineNumber(String code, int offset) {
        int line = 1;
        for (int i = 0; i < offset; i++) {
            if (code.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private static List<TestSource> testSources() {
        try (Stream<Path> files = Files.walk(testSourceRoot())) {
            return files.filter(file -> file.toString().endsWith(".java"))
                    .sorted()
                    .map(TestDiscoveryTest::load)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Resolves src/test/java from the compiled test classes (target/test-classes), so the test does not
     * depend on the working directory.
     */
    private static Path testSourceRoot() {
        try {
            Path testClasses = Paths.get(TestDiscoveryTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return testClasses.getParent().getParent().resolve(Paths.get("src", "test", "java"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static TestSource load(Path path) {
        try {
            String fileName = path.getFileName().toString();
            String className = fileName.substring(0, fileName.length() - ".java".length());
            return new TestSource(path, className, withoutCommentsAndLiterals(Files.readString(path)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String withoutCommentsAndLiterals(String source) {
        return COMMENT_OR_LITERAL.matcher(source)
                .replaceAll(match -> Matcher.quoteReplacement(match.group().replaceAll("[^\\n]", " ")));
    }
}
