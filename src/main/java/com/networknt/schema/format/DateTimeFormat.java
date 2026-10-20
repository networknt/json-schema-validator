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
     * offset beyond 18:00. So the date and the time are checked with the rules of
     * the date and the time format, and a leap second has to be at the end of a
     * June 30 or a December 31 in UTC (RFC 3339, appendix D).
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
            String date = value.substring(0, DATE_LENGTH);
            TimeFormat.FullTime time = TimeFormat.parse(value.substring(DATE_LENGTH + 1));
            if (time == null || !DateFormat.isValid(date)) {
                return false;
            }
            // TimeFormat.parse has checked that a leap second is at 23:59:60 UTC, so only the date is left
            return time.second != 60 || isLastDayOfJuneOrDecemberInUtc(LocalDate.parse(date), time);
        }

        private static boolean isLastDayOfJuneOrDecemberInUtc(LocalDate date, TimeFormat.FullTime time) {
            LocalDate utc = date.atTime(time.hour, time.minute).minusMinutes(time.offsetMinutes).toLocalDate();
            return utc.getMonth() == Month.JUNE && utc.getDayOfMonth() == 30
                    || utc.getMonth() == Month.DECEMBER && utc.getDayOfMonth() == 31;
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
