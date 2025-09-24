package com.networknt.schema.format;

import com.networknt.org.apache.commons.validator.routines.EmailValidator;
import com.networknt.schema.ExecutionContext;

/**
 * Format for idn-email.
 */
public class IdnEmailFormat implements Format {
    private final EmailValidator emailValidator;

    public IdnEmailFormat() {
        this(new IPv6AwareEmailValidator(true, true));
    }

    public IdnEmailFormat(EmailValidator emailValidator) {
        this.emailValidator = emailValidator;
    }

    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        return this.emailValidator.isValid(value);
    }

    @Override
    public String getName() {
        return "idn-email";
    }

    @Override
    public String getMessageKey() {
        return "format.idn-email";
    }
}
