package com.networknt.schema;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import com.networknt.schema.SpecVersion.VersionFlag;

@DisplayName("JSON Schema Test Suite")
class JsonSchemaTestSuiteTest extends AbstractJsonSchemaTestSuite {

    private final Map<Path, String> disabled;

    JsonSchemaTestSuiteTest() {
        this.disabled = new HashMap<>();

        disableV202012Tests();
        disableV201909Tests();
        disableV7Tests();
        disableV6Tests();
        disableV4Tests();
    }

    @TestFactory
    @DisplayName("Draft 2020-12")
    Stream<DynamicNode> draft2022012() {
        return createTests(VersionFlag.V202012, "src/test/suite/tests/draft2020-12");
    }

    @TestFactory
    @DisplayName("Draft 2019-09")
    Stream<DynamicNode> draft201909() {
        return createTests(VersionFlag.V201909, "src/test/suite/tests/draft2019-09");
    }

    @TestFactory
    @DisplayName("Draft 7")
    Stream<DynamicNode> draft7() {
        return createTests(VersionFlag.V7, "src/test/suite/tests/draft7");
    }

    @TestFactory
    @DisplayName("Draft 6")
    Stream<DynamicNode> draft6() {
        return createTests(VersionFlag.V6, "src/test/suite/tests/draft6");
    }

    @TestFactory
    @DisplayName("Draft 4")
    Stream<DynamicNode> draft4() {
        return createTests(VersionFlag.V4, "src/test/suite/tests/draft4");
    }

    @Override
    protected boolean enabled(Path path) {
        return !this.disabled.containsKey(path);
    }

    @Override
    protected Optional<String> reason(Path path) {
        return Optional.ofNullable(this.disabled.get(path));
    }

    private void disableV202012Tests() {
        // nothing here
    }

    private void disableV201909Tests() {
        // nothing here
    }

    private void disableV7Tests() {
        // nothing here
    }

    private void disableV6Tests() {
        // nothing here
    }

    private void disableV4Tests() {
        // nothing here
    }

}
