package com.networknt.schema.format;

import java.util.regex.Pattern;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.utils.RFC5892;

/**
 * Format for hostname.
 * <p>
 * A hostname is a sequence of LDH labels (RFC 1123, section 2.1) of at most 63 characters
 * each, separated by dots, with an overall length of at most 253 characters. A label that
 * starts with the ACE prefix {@code xn--}, in any case, is an A-label and must also be valid
 * Punycode that decodes to a valid U-label (RFC 5890, section 2.3.2.1); see
 * {@link RFC5892#isValidALabel(String)}. The RFC 5893 Bidi Rule applies within each A-label
 * only: the other labels are RFC 1123 LDH labels, so a digit-first label such as {@code 3com} stays
 * valid beside a right-to-left A-label. The cross-label Bidi Rule applies to {@code idn-hostname}.
 */
public class HostnameFormat implements Format {
    private static final int MAX_LENGTH = 253;

    private static final String LABEL = "[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?";

    private static final Pattern PATTERN = Pattern.compile(LABEL + "(\\." + LABEL + ")*");

    private static final String ACE_PREFIX = "xn--";

    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        if (null == value) return true;
        if (value.length() > MAX_LENGTH || !PATTERN.matcher(value).matches()) {
            return false;
        }
        // Walk the labels without splitting, so a name with no A-label allocates nothing.
        for (int start = 0; start < value.length(); ) {
            int end = value.indexOf('.', start);
            if (end < 0) end = value.length();
            if (value.regionMatches(true, start, ACE_PREFIX, 0, ACE_PREFIX.length())) {
                if (!RFC5892.isValidALabel(value.substring(start, end))) return false;
            }
            start = end + 1;
        }
        return true;
    }

    @Override
    public String getName() {
        return "hostname";
    }

    @Override
    public String getMessageKey() {
        return "format.hostname";
    }
}
