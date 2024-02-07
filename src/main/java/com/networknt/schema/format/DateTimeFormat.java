package com.networknt.schema.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.time.ITU;
import com.ethlo.time.LeapSecondException;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Format;

/**
 * Format for date-time.
 */
public class DateTimeFormat implements Format {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeFormat.class);

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
        return isLegalDateTime(value);
    }

    private static boolean isLegalDateTime(String string) {
        try {
            try {
                ITU.parseDateTime(string);
            } catch (LeapSecondException ex) {
                if (!ex.isVerifiedValidLeapYearMonth()) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            logger.debug("Invalid {}: {}", "date-time", ex.getMessage());
            return false;
        }
    }
}
