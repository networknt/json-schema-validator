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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private static final Path TEST_SOURCES = Paths.get("src", "test", "java");
    private static final Pattern TEST_ANNOTATION = Pattern.compile("@(?:Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\\b");
    // Surefire's default includes, plus *IT, which the verify-packaged-boundary execution runs by name
    private static final Pattern RUNNABLE_CLASS_NAME = Pattern.compile("Test\\w*|\\w*Test|\\w*Tests|\\w*TestCase|\\w*IT");
    private static final Pattern NO_ARGUMENT_VOID_METHOD = Pattern.compile("^\\s*(?:public\\s+)?void\\s+(\\w+)\\s*\\(\\s*\\)");

    @Test
    void testClassesMatchTheSurefireIncludes() throws IOException {
        List<String> misnamed = testSources().stream()
                .filter(source -> TEST_ANNOTATION.matcher(read(source)).find())
                .filter(source -> !read(source).contains("abstract class " + className(source)))
                .filter(source -> !RUNNABLE_CLASS_NAME.matcher(className(source)).matches())
                .map(Path::toString)
                .collect(Collectors.toList());

        assertEquals(Collections.emptyList(), misnamed);
    }

    @Test
    void testShapedMethodsHaveATestAnnotation() throws IOException {
        List<String> unannotated = new ArrayList<>();
        testSources().stream()
                .filter(source -> TEST_ANNOTATION.matcher(read(source)).find())
                .forEach(source -> unannotated.addAll(unannotatedMethods(source)));

        assertEquals(Collections.emptyList(), unannotated);
    }

    /**
     * A package-private or public no-argument void method that has no annotation and is not called from
     * elsewhere in its file is a test that JUnit skips.
     */
    private static List<String> unannotatedMethods(Path source) {
        String content = read(source);
        String[] lines = content.split("\\R", -1);
        List<String> unannotated = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            Matcher method = NO_ARGUMENT_VOID_METHOD.matcher(lines[i]);
            if (method.find() && !isAnnotated(lines, i) && !isCalled(content, method.group(1))) {
                unannotated.add(source + ":" + (i + 1) + " " + method.group(1));
            }
        }
        return unannotated;
    }

    private static boolean isAnnotated(String[] lines, int methodLine) {
        for (int i = methodLine - 1; i >= 0; i--) {
            String previous = lines[i].trim();
            if (!previous.isEmpty()) {
                // A multi-line annotation ends with ")", as in @ValueSource(strings = { ... })
                return previous.startsWith("@") || previous.endsWith(")");
            }
        }
        return false;
    }

    private static boolean isCalled(String content, String methodName) {
        return Pattern.compile("\\b" + methodName + "\\b").matcher(content).results().count() > 1;
    }

    private static List<Path> testSources() throws IOException {
        try (Stream<Path> files = Files.walk(TEST_SOURCES)) {
            return files.filter(file -> file.toString().endsWith(".java"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private static String className(Path source) {
        String fileName = source.getFileName().toString();
        return fileName.substring(0, fileName.length() - ".java".length());
    }

    private static String read(Path source) {
        try {
            return Files.readString(source);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
