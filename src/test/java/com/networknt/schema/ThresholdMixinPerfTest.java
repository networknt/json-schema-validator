package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.lang.String.format;
import static java.lang.System.out;

@Ignore
public class ThresholdMixinPerfTest {
    private static long thresholdIntegral = Long.MAX_VALUE - 1;


    private final LongNode maximumLong = new LongNode(thresholdIntegral);
    private final BigIntegerNode maximumBigInt = new BigIntegerNode(BigInteger.valueOf(thresholdIntegral));

    private final LongNode valueLong = new LongNode(Long.MAX_VALUE);
    private final BigIntegerNode valueBigInt = new BigIntegerNode(BigInteger.valueOf(Long.MAX_VALUE));

    //    private final double threshold = Double.MAX_VALUE - 1;
    private final double threshold = 1797693.134E+5D;
    private final DoubleNode maximumDouble = new DoubleNode(threshold);
    private final DecimalNode maximumDecimal = new DecimalNode(BigDecimal.valueOf(threshold));

    private final double value = threshold + 1;
    private final DoubleNode valueDouble = new DoubleNode(value);
    private final DecimalNode valueDecimal = new DecimalNode(new BigDecimal(value));
    private final TextNode valueTextual = new TextNode(String.valueOf(value));

    private final String maximumText = maximumDouble.asText();
    private final BigDecimal max = new BigDecimal(maximumText);

    private final int executeTimes = 200000;
    private final boolean excludeEqual = false;

    double baseTimeForDouble;
    private double baseTimeForLong;

    @Before
    public void baseTimeEstimate() {
        baseTimeForDouble = getAvgTimeViaMixin(asDouble, valueDouble, executeTimes);
        out.println(format("Base execution time (comparing two DoubleNodes) %f ns", baseTimeForDouble));

        baseTimeForLong = getAvgTimeViaMixin(asLong, valueLong, executeTimes);
        out.println(format("Base execution time (comparing two LongeNodes) %f ns \n", baseTimeForDouble));
    }

    @Test
    public void currentTimeEstimate() {
        out.println("Estimating time for current implementation:");
        double currentAvgTimeOnDouble = getAvgTimeViaMixin(currentImplementationDouble, valueDouble, executeTimes);
        out.println(format("Current double on double execution time %f ns, %f times slower", currentAvgTimeOnDouble, (currentAvgTimeOnDouble / baseTimeForDouble)));

        double currentAvgTimeOnDecimal = getAvgTimeViaMixin(currentImplementationDouble, valueDecimal, executeTimes);
        out.println(format("Current double on decimal execution time %f ns, %f times slower", currentAvgTimeOnDecimal, (currentAvgTimeOnDecimal / baseTimeForDouble)));

        double currentAvgTimeOnText = getAvgTimeViaMixin(currentImplementationDouble, valueTextual, executeTimes);
        out.println(format("Current double on text execution time %f ns, %f times slower", currentAvgTimeOnText, (currentAvgTimeOnText / baseTimeForDouble)));

        double currentAvgTimeDecimalOnDouble = getAvgTimeViaMixin(currentImplementationDecimal, valueDouble, executeTimes);
        out.println(format("Current decimal on double execution time %f ns, %f times slower", currentAvgTimeDecimalOnDouble, (currentAvgTimeDecimalOnDouble / baseTimeForDouble)));

        double currentAvgTimeDecimalOnDecimal = getAvgTimeViaMixin(currentImplementationDecimal, valueDecimal, executeTimes);
        out.println(format("Current decimal on decimal execution time %f ns, %f times slower", currentAvgTimeDecimalOnDecimal, (currentAvgTimeDecimalOnDecimal / baseTimeForDouble)));

        double currentAvgTimeDecimalOnText = getAvgTimeViaMixin(currentImplementationDecimal, valueTextual, executeTimes);
        out.println(format("Current decimal on text execution time %f ns, %f times slower", currentAvgTimeDecimalOnText, (currentAvgTimeDecimalOnText / baseTimeForDouble)));

        out.println(format("Cumulative average: %f\n\n", (currentAvgTimeOnDouble + currentAvgTimeOnDecimal + currentAvgTimeOnText + currentAvgTimeDecimalOnDouble + currentAvgTimeDecimalOnDecimal + currentAvgTimeDecimalOnText) / 6.0d));
    }

    @Test
    public void allInOneAproachTimeEstimate() {
        out.println("Estimating time threshold value agnostic mixin (aka allInOne):");
        double allInOneDoubleOnDouble = getAvgTimeViaMixin(allInOneDouble, valueDouble, executeTimes);
        out.println(format("AllInOne double on double execution time  %f ns, %f times slower", allInOneDoubleOnDouble, (allInOneDoubleOnDouble / baseTimeForDouble)));

        double allInOneDoubleOnDecimal = getAvgTimeViaMixin(allInOneDouble, valueDecimal, executeTimes);
        out.println(format("AllInOne double on decimal execution time %f ns, %f times slower", allInOneDoubleOnDecimal, (allInOneDoubleOnDecimal / baseTimeForDouble)));

        double allInOneDoubleOnText = getAvgTimeViaMixin(allInOneDouble, valueTextual, executeTimes);
        out.println(format("AllInOne double on text execution time %f ns, %f times slower", allInOneDoubleOnText, (allInOneDoubleOnText / baseTimeForDouble)));

        double allInOneDecimalOnDouble = getAvgTimeViaMixin(allInOneDecimal, valueDouble, executeTimes);
        out.println(format("AllInOne decimal on double execution time %f ns, %f times slower", allInOneDecimalOnDouble, (allInOneDecimalOnDouble / baseTimeForDouble)));

        double allInOneDecimalOnDecimal = getAvgTimeViaMixin(allInOneDecimal, valueDecimal, executeTimes);
        out.println(format("AllInOne decimal on decimal execution time %f ns, %f times slower", allInOneDecimalOnDecimal, (allInOneDecimalOnDecimal / baseTimeForDouble)));

        double allInOneDecimalOnText = getAvgTimeViaMixin(allInOneDecimal, valueTextual, executeTimes);
        out.println(format("AllInOne decimal on text execution time %f ns, %f times slower", allInOneDecimalOnText, (allInOneDecimalOnText / baseTimeForDouble)));

        out.println(format("Cumulative average: %f\n\n", (allInOneDoubleOnDouble + allInOneDoubleOnDecimal + allInOneDoubleOnText + allInOneDecimalOnDouble + allInOneDecimalOnDecimal + allInOneDecimalOnText) / 6.0d));
    }

    @Test
    public void specificCaseForEachThresholdValue() {
        out.println("Estimating time for specific cases:");
        double doubleValueAvgTime = getAvgTimeViaMixin(typedThreshold, valueDouble, executeTimes);
        out.println(format("Typed threshold execution time %f ns, %f times slower", doubleValueAvgTime, (doubleValueAvgTime / baseTimeForDouble)));

        double decimalValueAvgTime = getAvgTimeViaMixin(typedThreshold, valueDecimal, executeTimes);
        out.println(format("Typed threshold execution time %f ns, %f times slower", decimalValueAvgTime, (decimalValueAvgTime / baseTimeForDouble)));

        double textValueAvgTime = getAvgTimeViaMixin(typedThreshold, valueTextual, executeTimes);
        out.println(format("Typed threshold execution time %f ns, %f times slower", textValueAvgTime, (textValueAvgTime / baseTimeForDouble)));

        out.println(format("Cumulative average: %f\n\n", (doubleValueAvgTime + decimalValueAvgTime + textValueAvgTime) / 3.0d));
    }

    @Test
    public void noMixinsFloatingTimeEstimate() {
        out.println("Estimating time no mixins at all (floating point values):");
        double doubleValueAvgTime = getAvgTimeViaMixin(oneMixinForIntegerAndNumber, valueDecimal, executeTimes);
        out.println(format("No mixins with double value time %f ns, %f times slower", doubleValueAvgTime, (doubleValueAvgTime / baseTimeForDouble)));

        double decimalValueAvgTime = getAvgTimeViaMixin(oneMixinForIntegerAndNumber, valueDecimal, executeTimes);
        out.println(format("No mixins with decimal value time %f ns, %f times slower", decimalValueAvgTime, (decimalValueAvgTime / baseTimeForDouble)));

        double textValueAvgTime = getAvgTimeViaMixin(oneMixinForIntegerAndNumber, valueTextual, executeTimes);
        out.println(format("No mixins with text value time %f ns, %f times slower", textValueAvgTime, (textValueAvgTime / baseTimeForDouble)));
        out.println(format("Cumulative average: %f\n\n",
                (doubleValueAvgTime + decimalValueAvgTime + textValueAvgTime) / 3.0d));
    }

    @Test
    public void noMixinsIntegralTimeEstimate() {
        double longValueAvgTime = getAvgTimeViaMixin(oneMixinForIntegerAndNumber, new LongNode((long) value), executeTimes);
        out.println(format("No mixins with long value time %f ns, %f times slower", longValueAvgTime, (longValueAvgTime / baseTimeForLong)));

        double bigIntValueAvgTime = getAvgTimeViaMixin(oneMixinForIntegerAndNumber, new BigIntegerNode(BigInteger.valueOf((long) value)), executeTimes);
        out.println(format("No mixins with big int value time %f ns, %f times slower", bigIntValueAvgTime, (bigIntValueAvgTime / baseTimeForLong)));

        double textIntValueAvgTime = getAvgTimeViaMixin(oneMixinForIntegerAndNumber, new TextNode(String.valueOf((long) value)), executeTimes);
        out.println(format("No mixins with text value time %f ns, %f times slower", textIntValueAvgTime, (textIntValueAvgTime / baseTimeForLong)));
        out.println(format("Cumulative average: %f\n\n",
                (longValueAvgTime + bigIntValueAvgTime + textIntValueAvgTime) / 3.0d));
    }

    ThresholdMixin allInOneDouble = new AllInOneThreshold(maximumDouble, false);
    ThresholdMixin allInOneDecimal = new AllInOneThreshold(maximumDecimal, false);

    public static class AllInOneThreshold implements ThresholdMixin {

        private final BigDecimal bigDecimalMax;
        JsonNode maximum;
        private boolean excludeEqual;

        AllInOneThreshold(JsonNode maximum, boolean exludeEqual) {
            this.maximum = maximum;
            this.excludeEqual = exludeEqual;
            this.bigDecimalMax = new BigDecimal(maximum.asText());
        }

        @Override
        public boolean crossesThreshold(JsonNode node) {
            if (maximum.isDouble() && maximum.doubleValue() == Double.POSITIVE_INFINITY) {
                return false;
            }
            if (maximum.isDouble() && maximum.doubleValue() == Double.NEGATIVE_INFINITY) {
                return true;
            }
            if (maximum.isDouble() && node.isDouble()) {
                double lm = maximum.doubleValue();
                double val = node.doubleValue();
                return lm < val || (excludeEqual && lm == val);
            }

            if (maximum.isFloatingPointNumber() && node.isFloatingPointNumber()) {
                BigDecimal value = node.decimalValue();
                int compare = value.compareTo(bigDecimalMax);
                return compare > 0 || (excludeEqual && compare == 0);
            }

            BigDecimal value = new BigDecimal(node.asText());
            int compare = value.compareTo(bigDecimalMax);
            return compare > 0 || (excludeEqual && compare == 0);
        }

        @Override
        public String thresholdValue() {
            return maximum.asText();
        }
    }

    ;

    ThresholdMixin asDouble = new ThresholdMixin() {
        @Override
        public boolean crossesThreshold(JsonNode node) {
            double lm = maximumDouble.doubleValue();
            double val = node.doubleValue();
            return lm < val || (excludeEqual && lm == val);
        }

        @Override
        public String thresholdValue() {
            return maximumText;
        }
    };

    ThresholdMixin asLong = new ThresholdMixin() {
        @Override
        public boolean crossesThreshold(JsonNode node) {
            long lm = maximumLong.longValue();
            long val = node.longValue();
            return lm < val || (excludeEqual && lm == val);
        }

        @Override
        public String thresholdValue() {
            return maximumText;
        }
    };

    ThresholdMixin typedThreshold = new ThresholdMixin() {
        @Override
        public boolean crossesThreshold(JsonNode node) {
            if (node.isDouble()) {
                double lm = maximumDouble.doubleValue();
                double val = node.doubleValue();
                return lm < val || (excludeEqual && lm == val);
            }

            if (node.isBigDecimal()) {
                BigDecimal value = node.decimalValue();
                int compare = value.compareTo(max);
                return compare > 0 || (excludeEqual && compare == 0);
            }

            BigDecimal value = new BigDecimal(node.asText());
            int compare = value.compareTo(max);
            return compare > 0 || (excludeEqual && compare == 0);
        }

        @Override
        public String thresholdValue() {
            return maximumText;
        }
    };

    ThresholdMixin currentImplementationDouble = new ThresholdMixin() {
        @Override
        public boolean crossesThreshold(JsonNode node) {
            if (maximumDouble.isDouble() && maximumDouble.doubleValue() == Double.POSITIVE_INFINITY) {
                return false;
            }
            if (maximumDouble.isDouble() && maximumDouble.doubleValue() == Double.NEGATIVE_INFINITY) {
                return true;
            }
            if (node.isDouble() && node.doubleValue() == Double.NEGATIVE_INFINITY) {
                return false;
            }
            if (node.isDouble() && node.doubleValue() == Double.POSITIVE_INFINITY) {
                return true;
            }
            final BigDecimal max = new BigDecimal(maximumText);
            BigDecimal value = new BigDecimal(node.asText());
            int compare = value.compareTo(max);
            return compare > 0 || (excludeEqual && compare == 0);
        }

        @Override
        public String thresholdValue() {
            return maximumText;
        }
    };


    ThresholdMixin currentImplementationDecimal = new ThresholdMixin() {
        @Override
        public boolean crossesThreshold(JsonNode node) {
            if (maximumDecimal.isDouble() && maximumDecimal.doubleValue() == Double.POSITIVE_INFINITY) {
                return false;
            }
            if (maximumDecimal.isDouble() && maximumDecimal.doubleValue() == Double.NEGATIVE_INFINITY) {
                return true;
            }
            if (node.isDouble() && node.doubleValue() == Double.NEGATIVE_INFINITY) {
                return false;
            }
            if (node.isDouble() && node.doubleValue() == Double.POSITIVE_INFINITY) {
                return true;
            }
            final BigDecimal max = new BigDecimal(maximumText);
            BigDecimal value = new BigDecimal(node.asText());
            int compare = value.compareTo(max);
            return compare > 0 || (excludeEqual && compare == 0);
        }

        @Override
        public String thresholdValue() {
            return maximumText;
        }
    };

    ThresholdMixin oneMixinForIntegerAndNumber = new ThresholdMixin() {
        @Override
        public boolean crossesThreshold(JsonNode node) {
            BigDecimal value = new BigDecimal(node.asText());
            int compare = value.compareTo(max);
            return compare > 0 || (excludeEqual && compare == 0);
        }

        @Override
        public String thresholdValue() {
            return null;
        }
    };

    private double getAvgTimeViaMixin(ThresholdMixin mixin, JsonNode value, int iterations) {
        boolean excludeEqual = false;
        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            try {
                mixin.crossesThreshold(value);
            } finally {
                totalTime += System.nanoTime() - start;
            }
        }
        return totalTime / (iterations * 1.0D);
    }
}
