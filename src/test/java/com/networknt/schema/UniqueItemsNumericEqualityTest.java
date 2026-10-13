package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.networknt.schema.serialization.NodeReader;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;

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

    private static Schema schema() {
        return SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"type\":\"array\",\"uniqueItems\":true}");
    }

    private boolean valid(String instance) {
        return schema().validate(instance, InputFormat.JSON).isEmpty();
    }

    private boolean valid(ArrayNode instance) {
        return schema().validate(instance).isEmpty();
    }

    private static ArrayNode array() {
        return JsonNodeFactory.instance.arrayNode();
    }

    @Test
    void integerAndDecimalWithTheSameValueAreDuplicates() {
        assertFalse(valid("[1, 1.0]"), "1 and 1.0 are the same number");
    }

    @Test
    void integerAndTrailingZeroDecimalAreDuplicates() {
        assertFalse(valid("[1, 1.00]"), "1 and 1.00 are the same number");
    }

    /**
     * The default mapper reads 1.0 and 1.00 as the same DoubleNode, so they are
     * read as BigDecimals here to keep their different scales.
     */
    @Test
    void decimalsWithDifferentScaleAreDuplicates() {
        Schema schema = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                        builder -> builder.nodeReader(NodeReader.builder()
                                .jsonMapper(JsonMapper.builder()
                                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build())
                                .build()))
                .getSchema("{\"type\":\"array\",\"uniqueItems\":true}");
        assertFalse(schema.validate("[1.0, 1.00]", InputFormat.JSON).isEmpty(), "1.0 and 1.00 are the same number");
        assertFalse(schema.validate("[2.50, 2.5]", InputFormat.JSON).isEmpty(), "2.50 and 2.5 are the same number");
        assertTrue(schema.validate("[2.5, 2.25]", InputFormat.JSON).isEmpty());
    }

    @Test
    void doubleAndBigDecimalWithTheSameValueAreDuplicates() {
        assertFalse(valid(array().add(0.5).add(new BigDecimal("0.50"))), "0.5 and 0.50 are the same number");
        assertTrue(valid(array().add(0.5).add(new BigDecimal("0.51"))));
    }

    @Test
    void zeroAndNegativeZeroAreDuplicates() {
        assertFalse(valid("[0, -0.0]"), "0 and -0.0 are the same number");
    }

    /**
     * Numbers outside the long range are compared by value too. Their doubles can
     * be equal when the numbers are not, which must not make them duplicates.
     */
    @Test
    void numbersOutsideTheLongRangeAreComparedByValue() {
        BigInteger twoTo63 = BigInteger.ONE.shiftLeft(63);
        BigInteger twoTo70 = BigInteger.ONE.shiftLeft(70);
        assertTrue(valid(array().add(twoTo63).add(twoTo63.add(BigInteger.ONE))), "2^63 and 2^63 + 1 differ");
        assertTrue(valid(array().add(twoTo70).add(twoTo70.add(BigInteger.ONE))), "2^70 and 2^70 + 1 differ");
        assertFalse(valid(array().add(twoTo70).add(new BigDecimal(twoTo70))), "2^70 as a BigInteger and a BigDecimal");
        assertTrue(valid(array().add(Long.MAX_VALUE).add(0x1p63)), "Long.MAX_VALUE is not 2^63");
    }

    /**
     * A double's value is its shortest decimal form, as for {@code const}. For
     * whole doubles above 2^53 that is not always the exact binary value: 1e30 is
     * 10^30, and the shortest form of 2^60 is not 2^60.
     */
    @Test
    void largeWholeDoublesAreComparedByTheirShortestDecimalForm() {
        assertFalse(valid("[1e30, 1000000000000000000000000000000]"), "1e30 and 10^30 are the same number");
        assertFalse(valid("[[1e30], [1000000000000000000000000000000]]"), "nested 1e30 and 10^30 are the same");
        assertFalse(valid("[1e18, 1000000000000000000]"), "1e18 and 10^18 are the same number");
        long twoTo60 = 1L << 60;
        assertTrue(valid(array().add(twoTo60).add((double) twoTo60)), "2^60 as a long and as a double differ");
        BigInteger twoTo63 = BigInteger.ONE.shiftLeft(63);
        assertTrue(valid(array().add(twoTo63).add(0x1p63)), "2^63 and the double 2^63 differ");
    }

    /**
     * The double -2^63 is compared by its shortest form, which can lie below
     * Long.MIN_VALUE (-9223372036854776000 on JDK 19+), so it must not be read as
     * a long.
     */
    @Test
    void minusTwoToSixtyThreeAsADoubleIsComparedByItsShortestDecimalForm() {
        BigInteger shortest = new BigDecimal(Double.toString(-0x1p63)).toBigIntegerExact();
        assertFalse(valid(array().add(-0x1p63).add(shortest)), "-2^63 and its shortest form are the same number");
        assertTrue(valid(array().add(-0x1p63).add(new BigInteger("9223372036854775616"))),
                "-2^63 is not 9223372036854775616");
    }

    /**
     * Integers that round to the same double must not be treated as equal. Runs of
     * 2^18 consecutive integers near 2^70 share a double. This checks the verdict
     * only; the hash of a big integer that is not exactly a double keeps such runs
     * from all landing in one bucket.
     */
    @Test
    void consecutiveIntegersSharingADoubleAreUnique() {
        BigInteger twoTo70 = BigInteger.ONE.shiftLeft(70);
        ArrayNode items = array();
        for (int i = 0; i < 1000; i++) {
            items.add(twoTo70.add(BigInteger.valueOf(i)));
        }
        assertTrue(valid(items));
        items.add(twoTo70.add(BigInteger.valueOf(500)));
        assertFalse(valid(items), "a repeated integer is still a duplicate");
    }

    /**
     * Integers beyond the double range have no double value. Jackson 3 throws if
     * one is asked for, so they must be compared without it.
     */
    @Test
    void numbersBeyondTheDoubleRangeDoNotFailValidation() {
        String tenTo400 = "1" + String.join("", Collections.nCopies(400, "0"));
        assertTrue(valid("[" + tenTo400 + "]"));
        assertTrue(valid("[[" + tenTo400 + "], {\"a\": " + tenTo400 + "}]"));
        assertFalse(valid("[" + tenTo400 + ", " + tenTo400 + "]"), "10^400 twice is a duplicate");
        assertTrue(valid("[" + tenTo400 + ", 1" + tenTo400 + "]"));

        Schema schema = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                        builder -> builder.nodeReader(NodeReader.builder()
                                .jsonMapper(JsonMapper.builder()
                                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build())
                                .build()))
                .getSchema("{\"type\":\"array\",\"uniqueItems\":true}");
        assertTrue(schema.validate("[1e400, 1e401]", InputFormat.JSON).isEmpty());
        assertFalse(schema.validate("[1e400, 1e400]", InputFormat.JSON).isEmpty(), "1e400 twice is a duplicate");
        assertFalse(schema.validate("[1e400, 10e399]", InputFormat.JSON).isEmpty(), "1e400 and 10e399 are the same");
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

    @Test
    void objectPropertyOrderDoesNotMatter() {
        assertFalse(valid("[{\"a\": 1, \"b\": [2]}, {\"b\": [2.0], \"a\": 1.0}]"), "same properties in another order");
        assertTrue(valid("[{\"a\": 1, \"b\": 2}, {\"a\": 1, \"c\": 2}]"));
        assertTrue(valid("[[1, 2], [2, 1]]"), "array order does matter");
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
