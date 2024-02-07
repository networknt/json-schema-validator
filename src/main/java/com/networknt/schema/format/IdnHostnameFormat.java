package com.networknt.schema.format;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Format;
import com.networknt.schema.utils.RFC5892;

/**
 * Format for idn-hostname.
 */
public class IdnHostnameFormat implements Format {
    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        if (null == value || value.isEmpty()) return true;
        return RFC5892.isValid(value);
    }

    @Override
    public String getName() {
        return "idn-hostname";
    }

    @Override
    public String getMessageKey() {
        return "format.idn-hostname";
    }
}
