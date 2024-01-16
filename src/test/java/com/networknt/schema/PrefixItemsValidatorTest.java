package com.networknt.schema;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class handles exception case for {@link PrefixItemsValidator}
 */
public class PrefixItemsValidatorTest extends AbstractJsonSchemaTestSuite {

    /**
     * this method create test cases from JSON and run those test cases with assertion
     */
    @Test
    void testEmptyPrefixItemsException() {
        Stream<DynamicNode> dynamicNodeStream = createTests(SpecVersion.VersionFlag.V7, "src/test/suite/tests/prefixItemsException");
        dynamicNodeStream.forEach(
                dynamicNode -> {
                    assertThrows(JsonSchemaException.class, () -> {
                        ((DynamicContainer) dynamicNode).getChildren().forEach(dynamicNode1 -> {
                        });
                    });
                }
        );
    }


}
