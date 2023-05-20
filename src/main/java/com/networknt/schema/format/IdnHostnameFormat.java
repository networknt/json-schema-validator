package com.networknt.schema.format;

import com.networknt.schema.utils.RFC5892;

public class IdnHostnameFormat extends AbstractFormat {

    public IdnHostnameFormat() {
        super("idn-hostname", "must be a valid RFC 5890 internationalized hostname");
    }

    @Override
    public boolean matches(String value) {
        if (null == value || value.isEmpty()) return true;
        return RFC5892.isValid(value);
    }
}
