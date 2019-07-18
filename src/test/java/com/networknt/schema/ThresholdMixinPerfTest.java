package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

@Ignore
public class ThresholdMixinPerfTest {

//    private final double threshold = Double.MAX_VALUE - 1;
    private final double threshold = 1797693.134E+5D;
    private final DoubleNode maximumDouble   = new DoubleNode(threshold);
    private final DecimalNode maximumDecimal = new DecimalNode(BigDecimal.valueOf(threshold));

    private final double value = threshold+1;
    private final DoubleNode valueDouble = new DoubleNode(value);
    private final DecimalNode valueDecimal = new DecimalNode(new BigDecimal(value));
    private final TextNode valueTextual = new TextNode(String.valueOf(value));

    private final String maximumText = maximumDouble.asText();
    private final BigDecimal max = new BigDecimal(maximumText);

    private final int executeTimes = 200000;
    private final boolean excludeEqual = false;

    @Test
    public void testDoubleVsBigDecimalOnCompareTimeViaMixins() throws InvocationTargetException, IllegalAccessException {

        double baseTime = getAvgTimeViaMixin(asDouble, valueDouble, executeTimes);
        System.out.println(String.format("Base execution time (comparing two DoubleNodes) %f ns \n", baseTime));

        double currentAvgTimeOnDouble = getAvgTimeViaMixin(currentImplementationDouble, valueDouble, executeTimes);
        System.out.println(String.format("Current double on double execution time %f ns, %f times slower", currentAvgTimeOnDouble, (currentAvgTimeOnDouble/baseTime)));

        double currentAvgTimeOnDecimal = getAvgTimeViaMixin(currentImplementationDouble, valueDecimal, executeTimes);
        System.out.println(String.format("Current double on decimal execution time %f ns, %f times slower", currentAvgTimeOnDecimal, (currentAvgTimeOnDecimal/baseTime)));

        double currentAvgTimeOnText = getAvgTimeViaMixin(currentImplementationDouble, valueTextual, executeTimes);
        System.out.println(String.format("Current double on text execution time %f ns, %f times slower", currentAvgTimeOnText, (currentAvgTimeOnText/baseTime)));

        double currentAvgTimeDecimalOnDouble = getAvgTimeViaMixin(currentImplementationDecimal, valueDouble, executeTimes);
        System.out.println(String.format("Current decimal on double execution time %f ns, %f times slower", currentAvgTimeDecimalOnDouble, (currentAvgTimeDecimalOnDouble/baseTime)));

        double currentAvgTimeDecimalOnDecimal = getAvgTimeViaMixin(currentImplementationDecimal, valueDecimal, executeTimes);
        System.out.println(String.format("Current decimal on decimal execution time %f ns, %f times slower", currentAvgTimeDecimalOnDecimal, (currentAvgTimeDecimalOnDecimal/baseTime)));

        double currentAvgTimeDecimalOnText = getAvgTimeViaMixin(currentImplementationDecimal, valueTextual, executeTimes);
        System.out.println(String.format("Current decimal on text execution time %f ns, %f times slower", currentAvgTimeDecimalOnText, (currentAvgTimeDecimalOnText/baseTime)));

        System.out.println(String.format("Cumulative average: %f\n\n", (currentAvgTimeOnDouble+currentAvgTimeOnDecimal+currentAvgTimeOnText+currentAvgTimeDecimalOnDouble+currentAvgTimeDecimalOnDecimal+currentAvgTimeDecimalOnText)/6.0d));

        double allInOneDoubleOnDouble = getAvgTimeViaMixin(allInOneDouble, valueDouble, executeTimes);
        System.out.println(String.format("AllInOne double on double execution time  %f ns, %f times slower", allInOneDoubleOnDouble, (allInOneDoubleOnDouble/baseTime)));

        double allInOneDoubleOnDecimal = getAvgTimeViaMixin(allInOneDouble, valueDecimal, executeTimes);
        System.out.println(String.format("AllInOne double on decimal execution time %f ns, %f times slower", allInOneDoubleOnDecimal, (allInOneDoubleOnDecimal/baseTime)));

        double allInOneDoubleOnText = getAvgTimeViaMixin(allInOneDouble, valueTextual, executeTimes);
        System.out.println(String.format("AllInOne double on text execution time %f ns, %f times slower", allInOneDoubleOnText, (allInOneDoubleOnText/baseTime)));

        double allInOneDecimalOnDouble = getAvgTimeViaMixin(allInOneDecimal, valueDouble, executeTimes);
        System.out.println(String.format("AllInOne decimal on double execution time %f ns, %f times slower", allInOneDecimalOnDouble, (allInOneDecimalOnDouble/baseTime)));

        double allInOneDecimalOnDecimal = getAvgTimeViaMixin(allInOneDecimal, valueDecimal, executeTimes);
        System.out.println(String.format("AllInOne decimal on decimal execution time %f ns, %f times slower", allInOneDecimalOnDecimal, (allInOneDecimalOnDecimal/baseTime)));

        double allInOneDecimalOnText = getAvgTimeViaMixin(allInOneDecimal, valueTextual, executeTimes);
        System.out.println(String.format("AllInOne decimal on text execution time %f ns, %f times slower", allInOneDecimalOnText, (allInOneDecimalOnText/baseTime)));

        System.out.println(String.format("Cumulative average: %f\n\n", (allInOneDoubleOnDouble+allInOneDoubleOnDecimal+allInOneDoubleOnText+allInOneDecimalOnDouble+allInOneDecimalOnDecimal+allInOneDecimalOnText)/6.0d));

        double typedThresholdOnDouble = getAvgTimeViaMixin(typedThreshold, valueDouble, executeTimes);
        System.out.println(String.format("Typed threshold execution time %f ns, %f times slower", typedThresholdOnDouble, (typedThresholdOnDouble/baseTime)));

        double typedThresholdOnDecimal = getAvgTimeViaMixin(typedThreshold, valueDecimal, executeTimes);
        System.out.println(String.format("Typed threshold execution time %f ns, %f times slower", typedThresholdOnDecimal, (typedThresholdOnDecimal/baseTime)));

        double typedThresholdOnText = getAvgTimeViaMixin(typedThreshold, valueTextual, executeTimes);
        System.out.println(String.format("Typed threshold execution time %f ns, %f times slower", typedThresholdOnText, (typedThresholdOnText/baseTime)));

        System.out.println(String.format("Cumulative average: %f\n\n", (typedThresholdOnDouble+typedThresholdOnDecimal+typedThresholdOnText)/3.0d));
    }

    ThresholdMixin allInOneDouble = new AllInOneThreshold(maximumDouble, false);
    ThresholdMixin allInOneDecimal = new AllInOneThreshold(maximumDecimal, false);

    public static class AllInOneThreshold implements ThresholdMixin {

        private final BigDecimal bigDecimalMax;
        JsonNode maximum;
        private boolean excludeEqual;

        AllInOneThreshold(JsonNode maximum, boolean exludeEqual){
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
    };

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

    private double getAvgTimeViaMixin(ThresholdMixin mixin, JsonNode value, int iterations) {
        boolean excludeEqual = false;
        long totalTime = 0;
        for(int i = 0; i < iterations; i++) {
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
