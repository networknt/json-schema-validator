package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.keyword.EnumValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Checks the optimized equality against decimal comparison through real schemas. */
class KeywordNumericEqualityTest {
    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
    private static final SchemaRegistry REGISTRY = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);

    static Stream<Arguments> numbers() {
        BigInteger large = BigInteger.ONE.shiftLeft(70);
        List<JsonNode> values = Arrays.asList(
                IntNode.valueOf(1), LongNode.valueOf(1), DoubleNode.valueOf(1),
                DecimalNode.valueOf(new BigDecimal("1.00")), BigIntegerNode.valueOf(BigInteger.ONE),
                DoubleNode.valueOf(-0.0), FloatNode.valueOf(-0.0f),
                DecimalNode.valueOf(new BigDecimal(BigInteger.ZERO, Integer.MAX_VALUE)),
                LongNode.valueOf(Long.MIN_VALUE), DoubleNode.valueOf(-0x1p63),
                LongNode.valueOf(Long.MAX_VALUE), DoubleNode.valueOf(0x1p63),
                LongNode.valueOf(1L << 60), DoubleNode.valueOf(0x1p60),
                BigIntegerNode.valueOf(large), BigIntegerNode.valueOf(large.add(BigInteger.ONE)),
                DecimalNode.valueOf(new BigDecimal(large)),
                FloatNode.valueOf(0.1f), DoubleNode.valueOf(0.1), DecimalNode.valueOf(new BigDecimal("0.10")),
                DecimalNode.valueOf(new BigDecimal("1e400")),
                DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE)),
                DecimalNode.valueOf(new BigDecimal(BigInteger.TEN, Integer.MAX_VALUE)),
                DecimalNode.valueOf(new BigDecimal(BigInteger.valueOf(100), Integer.MIN_VALUE)),
                DecimalNode.valueOf(new BigDecimal(BigInteger.valueOf(1000), Integer.MIN_VALUE + 1)),
                DoubleNode.valueOf(Double.NaN), FloatNode.valueOf(Float.NaN),
                DoubleNode.valueOf(Double.POSITIVE_INFINITY), FloatNode.valueOf(Float.POSITIVE_INFINITY),
                DoubleNode.valueOf(Double.NEGATIVE_INFINITY));
        return values.stream().flatMap(a -> values.stream().map(b -> Arguments.of(a, b, equalNumbers(a, b))));
    }

    private static boolean equalNumbers(JsonNode a, JsonNode b) {
        boolean aNonFinite = (a.isDouble() || a.isFloat()) && !Double.isFinite(a.doubleValue());
        boolean bNonFinite = (b.isDouble() || b.isFloat()) && !Double.isFinite(b.doubleValue());
        return aNonFinite || bNonFinite
                ? aNonFinite && bNonFinite && Double.compare(a.doubleValue(), b.doubleValue()) == 0
                : a.decimalValue().compareTo(b.decimalValue()) == 0;
    }

    @ParameterizedTest
    @MethodSource("numbers")
    void keywordsAgreeWithDecimalComparison(JsonNode a, JsonNode b, boolean equal) {
        assertKeywords(a, b, equal);
        assertKeywords(NODES.arrayNode().add(a), NODES.arrayNode().add(b), equal);
        ObjectNode left = NODES.objectNode();
        left.set("value", NODES.arrayNode().add(a));
        left.put("other", true);
        ObjectNode right = NODES.objectNode();
        right.put("other", true);
        right.set("value", NODES.arrayNode().add(b));
        assertKeywords(left, right, equal);
    }

    private static void assertKeywords(JsonNode a, JsonNode b, boolean equal) {
        ObjectNode constant = NODES.objectNode();
        constant.set("const", a);
        assertEquals(equal, REGISTRY.getSchema(constant).validate(b).isEmpty(), "const");
        ObjectNode enumeration = NODES.objectNode();
        enumeration.set("enum", NODES.arrayNode().add(a));
        assertEquals(equal, REGISTRY.getSchema(enumeration).validate(b).isEmpty(), "enum");
        ArrayNode pair = NODES.arrayNode().add(a).add(b);
        assertEquals(!equal, REGISTRY.getSchema("{\"uniqueItems\":true}").validate(pair).isEmpty(), "uniqueItems");
    }

    @Test
    void numbersDoNotEqualStringsOrBooleans() {
        assertKeywords(IntNode.valueOf(1), NODES.textNode("1"), false);
        assertKeywords(IntNode.valueOf(1), NODES.booleanNode(true), false);
        assertKeywords(IntNode.valueOf(0), NODES.booleanNode(false), false);
        assertKeywords(NODES.arrayNode().add(1), NODES.arrayNode().add(true), false);
    }

    @Test
    void enumTypeLooseStillUsesTheConvertedNumberText() {
        SchemaRegistry loose = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
        Schema schema = loose.getSchema("{\"enum\":[100]}");
        assertTrue(schema.validate("\"100\"", InputFormat.JSON).isEmpty());
        assertFalse(schema.validate("\"100.0\"", InputFormat.JSON).isEmpty());
        ObjectNode exactSchema = NODES.objectNode();
        exactSchema.set("enum", NODES.arrayNode().add(new BigDecimal("1E+2")));
        assertTrue(loose.getSchema(exactSchema).validate("\"100\"", InputFormat.JSON).isEmpty());
    }

    @Test
    void enumStillUsesProtectedConversionOverrides() {
        Schema parent = REGISTRY.getSchema("{}");
        int[] calls = {0};
        EnumValidator validator = new EnumValidator(SchemaLocation.of("#/enum"),
                NODES.arrayNode().add(1), parent, parent.getSchemaContext()) {
            @Override
            protected JsonNode processNumberNode(JsonNode node) {
                calls[0]++;
                return IntNode.valueOf(node.intValue() % 2);
            }
        };
        ExecutionContext context = parent.createExecutionContext();
        JsonNode instance = IntNode.valueOf(3);
        validator.validate(context, instance, instance, new NodePath(PathType.JSON_POINTER));
        assertTrue(context.getErrors().isEmpty());
        assertEquals(2, calls[0], "schema and instance conversion hooks");
    }
}
