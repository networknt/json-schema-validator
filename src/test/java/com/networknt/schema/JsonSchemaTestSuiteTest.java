package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.SpecVersion.VersionFlag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@DisplayName("JSON Schema Test Suite")
class JsonSchemaTestSuiteTest extends AbstractJsonSchemaTestSuite {

    private final Set<Path> disabled;

    public JsonSchemaTestSuiteTest() {
        this.mapper = new ObjectMapper();
        this.disabled = new HashSet<>();

        this.disabled.add(Paths.get("src/test/resources/data"));
        this.disabled.add(Paths.get("src/test/resources/issues"));
        this.disabled.add(Paths.get("src/test/resources/openapi3"));
        this.disabled.add(Paths.get("src/test/resources/remotes"));
        this.disabled.add(Paths.get("src/test/resources/schema"));
        this.disabled.add(Paths.get("src/test/resources/multipleOfScale.json")); // TODO: Used in draft7 tests
        this.disabled.add(Paths.get("src/test/resources/selfRef.json"));

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
        return !this.disabled.contains(path);
    }

    private void disableV202012Tests() {
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/anchor.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/defs.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/dynamicRef.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/id.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/cross-draft.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/float-overflow.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format-assertion.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/ref.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/refRemote.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2020-12/vocabulary.json"));
    }

    private void disableV201909Tests() {
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/anchor.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/defs.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/id.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/cross-draft.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/float-overflow.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/recursiveRef.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/ref.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/refRemote.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft2019-09/vocabulary.json"));
    }

    private void disableV7Tests() {
        this.disabled.add(Paths.get("src/test/suite/tests/draft7/anchor.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft7/defs.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft7/optional/content.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft7/optional/cross-draft.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft7/optional/float-overflow.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft7/ref.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft7/refRemote.json"));
    }

    private void disableV6Tests() {
        this.disabled.add(Paths.get("src/test/suite/tests/draft6/optional/float-overflow.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft6/optional/format.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft6/ref.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft6/refRemote.json"));
   }

    private void disableV4Tests() {
        this.disabled.add(Paths.get("src/test/suite/tests/draft4/ref.json"));
        this.disabled.add(Paths.get("src/test/suite/tests/draft4/refRemote.json"));
    }

}
