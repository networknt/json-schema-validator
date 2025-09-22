package com.networknt.schema;

import com.networknt.schema.Specification.Version;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

@DisplayName("Custom Messages")
class CustomMessageTest extends AbstractJsonSchemaTestSuite {

    @TestFactory
    @DisplayName("Draft 2019-09 - Custom Messages Enabled")
    Stream<DynamicNode> draft201909__customMessagesEnabled() {
        return createTests(Version.DRAFT_2019_09, "src/test/resources/schema/customMessageTests/custom-message-tests.json");
    }

    @TestFactory
    @DisplayName("Draft 2019-09 - Custom Messages Disabled")
    Stream<DynamicNode> draft201909__customMessagesDisabled() {
        return createTests(Version.DRAFT_2019_09, "src/test/resources/schema/customMessageTests/custom-message-disabled-tests.json");
    }

}
