package com.networknt.schema.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationFormat extends AbstractFormat {
    private static final Pattern STRICT = Pattern.compile("^(?:P\\d+W)|(?:P(?:\\d+Y)?(?:\\d+M)?(?:\\d+D)?(?:T(?:\\d+H)?(?:\\d+M)?(?:\\d+S)?)?)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LAX = Pattern.compile("^(?:[-+]?)P(?:[-+]?[0-9]+Y)?(?:[-+]?[0-9]+M)?(?:[-+]?[0-9]+W)?(?:[-+]?[0-9]+D)?(?:T(?:[-+]?[0-9]+H)?(?:[-+]?[0-9]+M)?(?:[-+]?[0-9]+(?:[.,][0-9]{0,9})?S)?)?$", Pattern.CASE_INSENSITIVE);

    private final boolean strict;

    public DurationFormat(boolean strict) {
        super("duration", "must be a valid ISO 8601 duration");
        this.strict = strict;
    }

    @Override
    public boolean matches(String duration) {
        if (null == duration) {
            return true;
        }

        if (duration.endsWith("P") || duration.endsWith("T")) {
            return false;
        }

        Pattern pattern = strict ? STRICT : LAX;
        Matcher matcher = pattern.matcher(duration);
        return matcher.matches();
    }

}
