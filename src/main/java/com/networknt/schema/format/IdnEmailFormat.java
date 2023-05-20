package com.networknt.schema.format;

import com.networknt.org.apache.commons.validator.routines.EmailValidator;

public class IdnEmailFormat extends AbstractFormat {

    private final EmailValidator emailValidator;

    public IdnEmailFormat() {
        super("idn-email", "must be a valid RFC 6531 Mailbox");
        this.emailValidator = new IPv6AwareEmailValidator(true, true);
    }

    @Override
    public boolean matches(String value) {
        return this.emailValidator.isValid(value);
    }

}
