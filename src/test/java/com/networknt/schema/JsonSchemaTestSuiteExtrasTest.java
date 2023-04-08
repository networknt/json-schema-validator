package com.networknt.schema;

import com.networknt.schema.SpecVersion.VersionFlag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@DisplayName("JSON Schema Test Suite Extras")
class JsonSchemaTestSuiteExtrasTest extends AbstractJsonSchemaTestSuite {

    private static final Path excluded = Paths.get("src/test/resources/draft4/relativeRefRemote.json");

    @TestFactory
    @DisplayName("Draft 2020-12")
    Stream<DynamicNode> draft2022012() {
        return createTests(VersionFlag.V202012, "src/test/resources/draft2020-12");
    }

    @TestFactory
    @DisplayName("Draft 2019-09")
    Stream<DynamicNode> draft201909() {
        return createTests(VersionFlag.V201909, "src/test/resources/draft2019-09");
    }

    @TestFactory
    @DisplayName("Draft 7")
    Stream<DynamicNode> draft7() {
        return createTests(VersionFlag.V7, "src/test/resources/draft7");
    }

    @TestFactory
    @DisplayName("Draft 6")
    Stream<DynamicNode> draft6() {
        return createTests(VersionFlag.V6, "src/test/resources/draft6");
    }

    @TestFactory
    @DisplayName("Draft 4")
    Stream<DynamicNode> draft4() {
        return createTests(VersionFlag.V4, "src/test/resources/draft4");
    }

    @Override
    protected boolean enabled(Path path) {
        return !excluded.equals(path);
    }

}
