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
 * {@link RFC5892#isValidALabel(String)}.
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
        for (String label : value.split("\\.")) {
            if (label.regionMatches(true, 0, ACE_PREFIX, 0, ACE_PREFIX.length()) && !RFC5892.isValidALabel(label)) {
                return false;
            }
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
