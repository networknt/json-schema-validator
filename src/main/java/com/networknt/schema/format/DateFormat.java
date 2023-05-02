package com.networknt.schema.format;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateFormat extends AbstractFormat {

    public DateFormat() {
        super("date", "must be a valid RFC 3339 full-date");
    }

    @Override
    public boolean matches(String value) {
        try {
            LocalDate date = LocalDate.parse(value);
            int year = date.getYear();
            return 0 <= year && year <= 9999;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
