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
        mapper = new ObjectMapper();
        disabled = new HashSet<>();

        disabled.add(Paths.get("src/test/resources/data"));
        disabled.add(Paths.get("src/test/resources/issues"));
        disabled.add(Paths.get("src/test/resources/openapi3"));
        disabled.add(Paths.get("src/test/resources/remotes"));
        disabled.add(Paths.get("src/test/resources/schema"));
        disabled.add(Paths.get("src/test/resources/multipleOfScale.json")); // TODO: Used in draft7 tests
        disabled.add(Paths.get("src/test/resources/selfRef.json"));

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
        return !disabled.contains(path);
    }

    private void disableV202012Tests() {
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/anchor.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/defs.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/contains.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/dynamicRef.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/id.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/items.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/not.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/cross-draft.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/float-overflow.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format-assertion.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/idn-email.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/idn-hostname.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/iri-reference.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/iri.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/json-pointer.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/relative-json-pointer.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/uri-reference.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/optional/format/uri-template.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/ref.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/refRemote.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/unevaluatedItems.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/unevaluatedProperties.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2020-12/vocabulary.json"));
    }

    private void disableV201909Tests() {
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/anchor.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/defs.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/id.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/not.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/cross-draft.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/float-overflow.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/idn-email.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/idn-hostname.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/iri-reference.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/iri.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/json-pointer.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/relative-json-pointer.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/uri-reference.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/optional/format/uri-template.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/recursiveRef.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/ref.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/refRemote.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/unevaluatedItems.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/unevaluatedProperties.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft2019-09/vocabulary.json"));
    }

    private void disableV7Tests() {
        disabled.add(Paths.get("src/test/suite/tests/draft7/anchor.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/defs.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/content.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/cross-draft.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/float-overflow.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/idn-email.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/idn-hostname.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/iri-reference.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/iri.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/json-pointer.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/relative-json-pointer.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/uri-reference.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/optional/format/uri-template.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/ref.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft7/refRemote.json"));
    }

    private void disableV6Tests() {
        disabled.add(Paths.get("src/test/suite/tests/draft6/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft6/optional/float-overflow.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft6/optional/format.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft6/optional/format/json-pointer.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft6/optional/format/uri-reference.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft6/optional/format/uri-template.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft6/ref.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft6/refRemote.json"));
   }

    private void disableV4Tests() {
        disabled.add(Paths.get("src/test/suite/tests/draft4/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft4/ref.json"));
        disabled.add(Paths.get("src/test/suite/tests/draft4/refRemote.json"));
    }

}
