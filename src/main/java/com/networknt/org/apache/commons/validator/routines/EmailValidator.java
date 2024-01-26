/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.org.apache.commons.validator.routines;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Perform email validations.</p>
 * <p>
 * Based on a script by <a href="mailto:stamhankar@hotmail.com">Sandeep V. Tamhankar</a>
 * https://javascript.internet.com
 * </p>
 * <p>
 * This implementation is not guaranteed to catch all possible errors in an email address.
 * </p>.
 *
 * @since 1.4
 */
public class EmailValidator implements Serializable {

    private static final long serialVersionUID = 1705927040799295880L;

    private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
    private static final String VALID_CHARS = "(\\\\.)|[^\\s" + SPECIAL_CHARS + "]";
    private static final String QUOTED_USER = "(\"(\\\\\"|[^\"])*\")";
    private static final String WORD = "((" + VALID_CHARS + "|')+|" + QUOTED_USER + ")";

    private static final String EMAIL_REGEX = "^(.+)@(\\S+)$";
    private static final String IP_DOMAIN_REGEX = "^\\[(.*)\\]$";
    private static final String USER_REGEX = "^" + WORD + "(\\." + WORD + ")*$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern IP_DOMAIN_PATTERN = Pattern.compile(IP_DOMAIN_REGEX);
    private static final Pattern USER_PATTERN = Pattern.compile(USER_REGEX);

    private static final int MAX_USERNAME_LEN = 64;

    /**
     * Singleton instance of this class, which
     *  doesn't consider local addresses as valid.
     */
    private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator(false, false);

    /**
     * Singleton instance of this class, which
     *  doesn't consider local addresses as valid.
     */
    private static final EmailValidator EMAIL_VALIDATOR_WITH_TLD = new EmailValidator(false, true);

    /**
     * Singleton instance of this class, which does
     *  consider local addresses valid.
     */
    private static final EmailValidator EMAIL_VALIDATOR_WITH_LOCAL = new EmailValidator(true, false);

    /**
     * Singleton instance of this class, which does
     *  consider local addresses valid.
     */
    private static final EmailValidator EMAIL_VALIDATOR_WITH_LOCAL_WITH_TLD = new EmailValidator(true, true);

    /**
     * Returns the Singleton instance of this validator.
     *
     * @return singleton instance of this validator.
     */
    public static EmailValidator getInstance() {
        return EMAIL_VALIDATOR;
    }

    /**
     * Returns the Singleton instance of this validator,
     *  with local validation as required.
     *
     * @param allowLocal Should local addresses be considered valid?
     * @return singleton instance of this validator
     */
    public static EmailValidator getInstance(final boolean allowLocal) {
        return getInstance(allowLocal, false);
    }

    /**
     * Returns the Singleton instance of this validator,
     *  with local validation as required.
     *
     * @param allowLocal Should local addresses be considered valid?
     * @param allowTld Should TLDs be allowed?
     * @return singleton instance of this validator
     */
    public static EmailValidator getInstance(final boolean allowLocal, final boolean allowTld) {
        if (allowLocal) {
            if (allowTld) {
                return EMAIL_VALIDATOR_WITH_LOCAL_WITH_TLD;
            }
            return EMAIL_VALIDATOR_WITH_LOCAL;
        }
        if (allowTld) {
            return EMAIL_VALIDATOR_WITH_TLD;
        }
        return EMAIL_VALIDATOR;
    }

    private final boolean allowTld;

    private final DomainValidator domainValidator;

    /**
     * Protected constructor for subclasses to use.
     *
     * @param allowLocal Should local addresses be considered valid?
     */
    protected EmailValidator(final boolean allowLocal) {
        this(allowLocal, false);
    }

    /**
     * Protected constructor for subclasses to use.
     *
     * @param allowLocal Should local addresses be considered valid?
     * @param allowTld Should TLDs be allowed?
     */
    protected EmailValidator(final boolean allowLocal, final boolean allowTld) {
        this.allowTld = allowTld;
        this.domainValidator = DomainValidator.getInstance(allowLocal);
    }

    /**
     * constructor for creating instances with the specified domainValidator
     *
     * @param allowLocal Should local addresses be considered valid?
     * @param allowTld Should TLDs be allowed?
     * @param domainValidator allow override of the DomainValidator.
     * The instance must have the same allowLocal setting.
     * @since 1.7
     */
    public EmailValidator(final boolean allowLocal, final boolean allowTld, final DomainValidator domainValidator) {
        this.allowTld = allowTld;
        if (domainValidator == null) {
            throw new IllegalArgumentException("DomainValidator cannot be null");
        }
        if (domainValidator.isAllowLocal() != allowLocal) {
            throw new IllegalArgumentException("DomainValidator must agree with allowLocal setting");
        }
        this.domainValidator = domainValidator;
    }

    /**
     * <p>Checks if a field has a valid e-mail address.</p>
     *
     * @param email The value validation is being performed on.  A {@code null}
     *              value is considered invalid.
     * @return true if the email address is valid.
     */
    public boolean isValid(final String email) {
        if (email == null) {
            return false;
        }

        if (email.endsWith(".")) { // check this first - it's cheap!
            return false;
        }

        // Check the whole email address structure
        final Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
        if (!emailMatcher.matches()) {
            return false;
        }

        if (!isValidUser(emailMatcher.group(1))) {
            return false;
        }

        if (!isValidDomain(emailMatcher.group(2))) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the domain component of an email address is valid.
     *
     * @param domain being validated, may be in IDN format
     * @return true if the email address's domain is valid.
     */
    protected boolean isValidDomain(final String domain) {
        // see if domain is an IP address in brackets
        final Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

        if (ipDomainMatcher.matches()) {
            final InetAddressValidator inetAddressValidator =
                    InetAddressValidator.getInstance();
            return inetAddressValidator.isValid(ipDomainMatcher.group(1));
        }
        // Domain is symbolic name
        if (allowTld) {
            return domainValidator.isValid(domain) || !domain.startsWith(".") && domainValidator.isValidTld(domain);
        }
        return domainValidator.isValid(domain);
    }

    /**
     * Returns true if the user component of an email address is valid.
     *
     * @param user being validated
     * @return true if the user name is valid.
     */
    protected boolean isValidUser(final String user) {

        if (user == null || user.length() > MAX_USERNAME_LEN) {
            return false;
        }

        return USER_PATTERN.matcher(user).matches();
    }

}