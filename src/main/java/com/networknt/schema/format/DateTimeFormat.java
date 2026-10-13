package com.networknt.schema.format;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.time.ITU;
import com.ethlo.time.LeapSecondException;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.utils.Classes;

/**
 * Format for date-time.
 */
public class DateTimeFormat implements Format {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeFormat.class);

    private static final boolean ETHLO_PRESENT = Classes.isPresent("com.ethlo.time.ITU", DateTimeFormat.class.getClassLoader());

    /**
     * Uses etho.
     * <p> 
     * This needs to be in a holder class otherwise a ClassNotFoundException will be
     * thrown when the DateTimeFormat is instantiated.
     */
    public static class Ethlo {
        public static boolean isValid(String value) {
            try {
                ITU.parseDateTime(value);
            } catch (LeapSecondException ex) {
                if (!ex.isVerifiedValidLeapYearMonth()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Uses java time. 
     */
    public static class JavaTimeOffsetDateTime {
        public static boolean isValid(String value) {
            try {
                OffsetDateTime.parse(value);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }
    }
    
    private static final Predicate<String> VALIDATE = ETHLO_PRESENT ? Ethlo::isValid : JavaTimeOffsetDateTime::isValid;

    @Override
    public String getName() {
        return "date-time";
    }

    @Override
    public String getMessageKey() {
        return "format.date-time";
    }

    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        return isValid(value);
    }

    private static boolean isValid(String value) {
        try {
            return VALIDATE.test(value);
        } catch (Exception ex) {
            logger.debug("Invalid {}: {}", "date-time", ex.getMessage());
            return false;
        }
    }
}
