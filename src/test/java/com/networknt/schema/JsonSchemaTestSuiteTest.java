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

        disabled.add(Paths.get("src/test/resources/draft2019-09/anchor.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/defs.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/idn-email.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/idn-hostname.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/iri-reference.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/iri.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/json-pointer.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/regex.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/relative-json-pointer.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/time.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/uri-reference.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/uri-template.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/optional/format/uri.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/ref.json"));
        disabled.add(Paths.get("src/test/resources/draft2019-09/refRemote_ignored.json"));

        disabled.add(Paths.get("src/test/resources/draft2020-12/anchor.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/defs.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/content.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/idn-email.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/idn-hostname.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/iri-reference.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/iri.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/json-pointer.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/regex.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/relative-json-pointer.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/time.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/uri-reference.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/uri-template.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/format/uri.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/optional/zeroTerminatedFloats.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/ref.json"));
        disabled.add(Paths.get("src/test/resources/draft2020-12/refRemote_ignored.json"));

        disabled.add(Paths.get("src/test/resources/draft7/anchor.json"));
        disabled.add(Paths.get("src/test/resources/draft7/defs.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/content.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/idn-email.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/idn-hostname.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/iri-reference.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/iri.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/json-pointer.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/regex.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/relative-json-pointer.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/time.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/uri-reference.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/uri-template.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/format/uri.json"));
        disabled.add(Paths.get("src/test/resources/draft7/optional/zeroTerminatedFloats.json"));
        disabled.add(Paths.get("src/test/resources/draft7/ref.json"));
        disabled.add(Paths.get("src/test/resources/draft7/refRemote_ignored.json"));
        
        disabled.add(Paths.get("src/test/resources/draft6/optional/ecmascript-regex.json"));
        disabled.add(Paths.get("src/test/resources/draft6/optional/format.json"));
        disabled.add(Paths.get("src/test/resources/draft6/optional/zeroTerminatedFloats.json"));
        disabled.add(Paths.get("src/test/resources/draft6/ref.json"));
        disabled.add(Paths.get("src/test/resources/draft6/refRemote_ignored.json"));

        disabled.add(Paths.get("src/test/resources/draft4/id.json"));
        disabled.add(Paths.get("src/test/resources/draft4/optional/ecmascript-regex.json")); // TODO: Not included in the original test.
        disabled.add(Paths.get("src/test/resources/draft4/ref.json")); // TODO: Not excluded in the original test.
        disabled.add(Paths.get("src/test/resources/draft4/relativeRefRemote.json")); // TODO: Not excluded in the original test.
    }

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
        return !disabled.contains(path);
    }

}
