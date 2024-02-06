package com.networknt.schema.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.ValidationContext;

public class DurationFormat extends BaseFormat {
    private static final String DURATION = "duration";

    private static final Pattern STRICT = Pattern.compile("^(?:P\\d+W)|(?:P(?:\\d+Y)?(?:\\d+M)?(?:\\d+D)?(?:T(?:\\d+H)?(?:\\d+M)?(?:\\d+S)?)?)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LAX = Pattern.compile("^(?:[-+]?)P(?:[-+]?[0-9]+Y)?(?:[-+]?[0-9]+M)?(?:[-+]?[0-9]+W)?(?:[-+]?[0-9]+D)?(?:T(?:[-+]?[0-9]+H)?(?:[-+]?[0-9]+M)?(?:[-+]?[0-9]+(?:[.,][0-9]{0,9})?S)?)?$", Pattern.CASE_INSENSITIVE);

    public DurationFormat() {
        super("duration", "must be a valid ISO 8601 duration");
    }

    @Override
    public boolean matches(ExecutionContext executionContext, ValidationContext validationContext, String duration) {
        if (null == duration) {
            return true;
        }

        if (duration.endsWith("P") || duration.endsWith("T")) {
            return false;
        }

        Pattern pattern = isStrictValidation(validationContext) ? STRICT : LAX;
        Matcher matcher = pattern.matcher(duration);
        return matcher.matches();
    }

    protected boolean isStrictValidation(ValidationContext validationContext) {
        return validationContext.getConfig().isStrict(DURATION);
    }

}
