package com.networknt.schema.format;

import java.time.LocalDate;
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
     * <p>
     * RFC 3339 date-time is full-date "T" full-time. OffsetDateTime.parse(..) is
     * not strict enough for it: it accepts a time without seconds, an offset
     * without minutes and an empty fraction, and it rejects a leap second and an
     * offset beyond 18:00. So the date is parsed on its own and the time is
     * checked with the same rules as the time format.
     */
    public static class JavaTimeOffsetDateTime {
        private static final int DATE_LENGTH = "2020-01-01".length();

        public static boolean isValid(String value) {
            if (value.length() <= DATE_LENGTH) {
                return false;
            }
            char separator = value.charAt(DATE_LENGTH);
            if (separator != 'T' && separator != 't') {
                return false;
            }
            try {
                // ISO_LOCAL_DATE is strict: four-digit year, two-digit month and day, and a date that exists
                LocalDate.parse(value.substring(0, DATE_LENGTH));
            } catch (DateTimeParseException e) {
                return false;
            }
            return TimeFormat.isValid(value.substring(DATE_LENGTH + 1));
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
