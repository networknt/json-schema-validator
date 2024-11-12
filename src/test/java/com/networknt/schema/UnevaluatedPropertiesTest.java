package com.networknt.schema;

import com.networknt.schema.SpecVersion.VersionFlag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

@DisplayName("Unevaluated Properties")
class UnevaluatedPropertiesTest extends AbstractJsonSchemaTestSuite {

    @TestFactory
    @DisplayName("Draft 2019-09")
    Stream<DynamicNode> draft201909() {
        return createTests(VersionFlag.V201909, "src/test/resources/schema/unevaluatedTests/unevaluated-tests.json");
    }

}
