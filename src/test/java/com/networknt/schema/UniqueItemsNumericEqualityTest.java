package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.networknt.schema.serialization.NodeReader;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * uniqueItems compares numbers by mathematical value, so 1 and 1.0 are the same
 * item. Comparison went through JsonNode equality, which is type-sensitive:
 * IntNode(1) does not equal DoubleNode(1.0), so the duplicate slipped through.
 *
 * The official suite's case for this is [1.0, 1.0, 1], which passes either way --
 * the two identical decimals are caught before an integer is ever compared with a
 * decimal. These cases put the integer next to the decimal directly.
 */
class UniqueItemsNumericEqualityTest {

    private boolean valid(String instance) {
        Schema schema = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"type\":\"array\",\"uniqueItems\":true}");
        return schema.validate(instance, InputFormat.JSON).isEmpty();
    }

    @Test
    void integerAndDecimalWithTheSameValueAreDuplicates() {
        assertFalse(valid("[1, 1.0]"), "1 and 1.0 are the same number");
    }

    @Test
    void integerAndTrailingZeroDecimalAreDuplicates() {
        assertFalse(valid("[1, 1.00]"), "1 and 1.00 are the same number");
    }

    @Test
    void decimalsWithDifferentScaleAreDuplicates() {
        assertFalse(valid("[1.0, 1.00]"), "1.0 and 1.00 are the same number");
    }

    @Test
    void largeIntegerAndDecimalWithTheSameValueAreDuplicates() {
        assertFalse(valid("[10000000000, 1.0e10]"), "10000000000 and 1.0e10 are the same number");
    }

    @Test
    void mathematicallyDifferentNumbersStayUnique() {
        assertTrue(valid("[1, 2]"));
        assertTrue(valid("[1, 1.5]"));
    }

    // The suite requires these to stay distinct: a number must not collapse into
    // a boolean of the same "truthiness".
    @Test
    void numbersAndBooleansAreNotInterchangeable() {
        assertTrue(valid("[[[1], \"foo\"], [[true], \"foo\"]]"), "nested 1 and true are unique");
        assertTrue(valid("[[[0], \"foo\"], [[false], \"foo\"]]"), "nested 0 and false are unique");
        assertTrue(valid("[1, true]"));
        assertTrue(valid("[0, false]"));
    }

    @Test
    void numbersAndTheirStringFormAreNotInterchangeable() {
        assertTrue(valid("[1, \"1\"]"));
    }

    // The rule applies wherever the number sits, not only at the top level.
    @Test
    void nestedNumbersFollowTheSameRule() {
        assertFalse(valid("[{\"a\": 1}, {\"a\": 1.0}]"), "nested 1 and 1.0 are the same");
        assertFalse(valid("[[1], [1.0]]"), "1 and 1.0 inside arrays are the same");
        assertTrue(valid("[{\"a\": 1}, {\"a\": 2}]"));
    }

    /**
     * NaN, Infinity and -Infinity are not JSON numbers, but a mapper can be
     * configured to read them. BigDecimal cannot represent them, so they have to
     * stay out of the numeric comparison; they keep comparing as nodes, which is
     * what every item did before numbers were folded by value.
     */
    @Test
    void nonFiniteNumbersDoNotFailValidation() {
        Schema schema = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                        builder -> builder.nodeReader(NodeReader.builder()
                                .jsonMapper(JsonMapper.builder()
                                        .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build())
                                .build()))
                .getSchema("{\"type\":\"array\",\"uniqueItems\":true}");

        assertTrue(schema.validate("[NaN, 1]", InputFormat.JSON).isEmpty());
        assertTrue(schema.validate("[Infinity, -Infinity]", InputFormat.JSON).isEmpty());
        assertTrue(schema.validate("[NaN, 1.0, 2]", InputFormat.JSON).isEmpty());
        assertFalse(schema.validate("[NaN, NaN]", InputFormat.JSON).isEmpty(),
                "non-finite numbers still compare as nodes");
        assertFalse(schema.validate("[Infinity, 1, 1.0]", InputFormat.JSON).isEmpty(),
                "finite numbers next to a non-finite one are still folded by value");
    }
}
