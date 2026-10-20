package com.networknt.schema.format;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.networknt.schema.ExecutionContext;

/**
 * Format for date.
 */
public class DateFormat implements Format {
    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        return isValid(value);
    }

    /**
     * Checks a value against the RFC 3339 full-date grammar: a four-digit year, a two-digit
     * month and day, and a date that exists.
     *
     * @param value the date
     * @return true if the value is a valid full-date
     */
    static boolean isValid(String value) {
        try {
            LocalDate date = LocalDate.parse(value);
            int year = date.getYear();
            return 0 <= year && year <= 9999;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "date";
    }

    @Override
    public String getMessageKey() {
        return "format.date";
    }
}
