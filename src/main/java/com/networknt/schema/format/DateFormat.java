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
