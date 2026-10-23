package com.networknt.schema.format;

import java.time.LocalDate;
import java.time.Month;
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
    private static final int DATE_LENGTH = "2020-01-01".length();
    private static final Logger logger = LoggerFactory.getLogger(DateTimeFormat.class);

    private static final boolean ETHLO_PRESENT = Classes.isPresent("com.ethlo.time.ITU", DateTimeFormat.class.getClassLoader());

    /**
     * Uses the shared RFC 3339 grammar and Ethlo's historical leap-second table.
     * <p> 
     * This needs to be in a holder class otherwise a ClassNotFoundException will be
     * thrown when the DateTimeFormat is instantiated.
     */
    public static class Ethlo {
        public static boolean isValid(String value) {
            return isValidDateTime(value, true);
        }

        private static boolean isValidLeapSecond(LocalDate utc) {
            if (utc.getYear() < 0 || utc.getYear() > 9999) {
                return false; // Outside Ethlo's four-digit years and its recorded leap dates.
            }
            try {
                ITU.parseDateTime(utc + "T23:59:60Z");
            } catch (LeapSecondException ex) {
                return ex.isVerifiedValidLeapYearMonth();
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
     * offset beyond 18:00. So the date and the time are checked with the rules of
     * the date and the time format, and a leap second has to be at the end of a
     * June 30 or a December 31 in UTC (RFC 3339, appendix D).
     */
    public static class JavaTimeOffsetDateTime {
        public static boolean isValid(String value) {
            return isValidDateTime(value, false);
        }
    }

    private static boolean isValidDateTime(String value, boolean verifyLeapSecondHistory) {
        if (value.length() <= DATE_LENGTH) {
            return false;
        }
        char separator = value.charAt(DATE_LENGTH);
        if (separator != 'T' && separator != 't') {
            return false;
        }
        LocalDate date = DateFormat.parse(value.substring(0, DATE_LENGTH));
        TimeFormat.FullTime time = TimeFormat.parse(value.substring(DATE_LENGTH + 1));
        if (time == null || date == null) {
            return false;
        }
        if (time.second != 60) {
            return true;
        }
        // TimeFormat.parse checks 23:59:60 UTC; compute that UTC date once.
        LocalDate utc = date.plusDays(Math.floorDiv(time.minutesInUtc(), 24 * 60));
        boolean possibleLeapDate = utc.getMonth() == Month.JUNE && utc.getDayOfMonth() == 30
                || utc.getMonth() == Month.DECEMBER && utc.getDayOfMonth() == 31;
        return possibleLeapDate && (!verifyLeapSecondHistory || Ethlo.isValidLeapSecond(utc));
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
