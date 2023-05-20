package com.networknt.schema.format;

import com.networknt.org.apache.commons.validator.routines.DomainValidator;
import com.networknt.org.apache.commons.validator.routines.EmailValidator;

/**
 * This is an extension of the Apache Commons Validator that correctly
 * handles email addresses containing an IPv6 literal as the domain.
 * <p>
 * Apache's {@link EmailValidator} delegates validation of the domain to
 * its {@link DomainValidator}, which is not aware that it is validating
 * an email address, which has a peculiar way of representing an IPv6
 * literal.
 */
class IPv6AwareEmailValidator extends EmailValidator {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new IPv6AwareEmailValidator.
     *
     * @param allowLocal Should local addresses be considered valid?
     * @param allowTld Should TLDs be allowed?
     */
    public IPv6AwareEmailValidator(final boolean allowLocal, final boolean allowTld) {
        super(allowLocal, allowTld);
    }

    @Override
    protected boolean isValidDomain(String domain) {
        return super.isValidDomain(domain.startsWith("[IPv6:") ? domain.replace("IPv6:", "") : domain);
    }

}