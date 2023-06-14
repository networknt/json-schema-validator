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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Regular Expression</b> validation (using the JRE's regular expression support).
 * <p>
 * Construct the validator either for a single regular expression or a set (array) of
 * regular expressions. By default validation is <i>case sensitive</i> but constructors
 * are provided to allow  <i>case in-sensitive</i> validation. For example to create
 * a validator which does <i>case in-sensitive</i> validation for a set of regular
 * expressions:
 * </p>
 * <pre>
 * <code>
 * String[] regexs = new String[] {...};
 * RegexValidator validator = new RegexValidator(regexs, false);
 * </code>
 * </pre>
 *
 * <ul>
 *   <li>Validate {@code true} or {@code false}:</li>
 *   <li>
 *     <ul>
 *       <li><code>boolean valid = validator.isValid(value);</code></li>
 *     </ul>
 *   </li>
 *   <li>Validate returning an aggregated String of the matched groups:</li>
 *   <li>
 *     <ul>
 *       <li><code>String result = validator.validate(value);</code></li>
 *     </ul>
 *   </li>
 *   <li>Validate returning the matched groups:</li>
 *   <li>
 *     <ul>
 *       <li><code>String[] result = validator.match(value);</code></li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <b>Note that patterns are matched against the entire input.</b>
 *
 * <p>
 * Cached instances pre-compile and re-use {@link Pattern}(s) - which according
 * to the {@link Pattern} API are safe to use in a multi-threaded environment.
 * </p>
 *
 * @since 1.4
 */
public class RegexValidator implements Serializable {

    private static final long serialVersionUID = -8832409930574867162L;

    private final Pattern[] patterns;

    /**
     * Constructs a <i>case sensitive</i> validator that matches any one
     * in the list of regular expressions.
     *
     * @param regexs The set of regular expressions this validator will
     * validate against
     */
    RegexValidator(final List<String> regexs) {
        this(regexs.toArray(new String[] {}), true);
    }

    /**
     * Constructs a <i>case sensitive</i> validator for a single
     * regular expression.
     *
     * @param regex The regular expression this validator will
     * validate against
     */
    public RegexValidator(final String regex) {
        this(regex, true);
    }

    /**
     * Constructs a validator for a single regular expression
     * with the specified case sensitivity.
     *
     * @param regex The regular expression this validator will
     * validate against
     * @param caseSensitive when {@code true} matching is <i>case
     * sensitive</i>, otherwise matching is <i>case in-sensitive</i>
     */
    public RegexValidator(final String regex, final boolean caseSensitive) {
        this(new String[] { regex }, caseSensitive);
    }

    /**
     * Constructs a <i>case sensitive</i> validator that matches any one
     * in the array of regular expressions.
     *
     * @param regexs The set of regular expressions this validator will
     * validate against
     */
    public RegexValidator(final String... regexs) {
        this(regexs, true);
    }

    /**
     * Constructs a validator that matches any one of the set of regular
     * expressions with the specified case sensitivity.
     *
     * @param regexs The set of regular expressions this validator will
     * validate against
     * @param caseSensitive when {@code true} matching is <i>case
     * sensitive</i>, otherwise matching is <i>case in-sensitive</i>
     */
    public RegexValidator(final String[] regexs, final boolean caseSensitive) {
        if (regexs == null || regexs.length == 0) {
            throw new IllegalArgumentException("Regular expressions are missing");
        }
        patterns = new Pattern[regexs.length];
        final int flags = (caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < regexs.length; i++) {
            if (regexs[i] == null || regexs[i].isEmpty()) {
                throw new IllegalArgumentException("Regular expression[" + i + "] is missing");
            }
            patterns[i] = Pattern.compile(regexs[i], flags);
        }
    }

    /**
     * Gets a copy of the Patterns.
     *
     * @return a copy of the Patterns.
     * @since 1.8
     */
    public Pattern[] getPatterns() {
        return patterns.clone();
    }

    /**
     * Validates a value against the set of regular expressions.
     *
     * @param value The value to validate.
     * @return {@code true} if the value is valid
     * otherwise {@code false}.
     */
    public boolean isValid(final String value) {
        if (value == null) {
            return false;
        }
        for (final Pattern pattern : patterns) {
            if (pattern.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates a value against the set of regular expressions
     * returning the array of matched groups.
     *
     * @param value The value to validate.
     * @return String array of the <i>groups</i> matched if
     * valid or <code>null</code> if invalid
     */
    public String[] match(final String value) {
        if (value == null) {
            return null;
        }
        for (final Pattern pattern : patterns) {
            final Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                final int count = matcher.groupCount();
                final String[] groups = new String[count];
                for (int j = 0; j < count; j++) {
                    groups[j] = matcher.group(j + 1);
                }
                return groups;
            }
        }
        return null;
    }


    /**
     * Validates a value against the set of regular expressions
     * returning a String value of the aggregated groups.
     *
     * @param value The value to validate.
     * @return Aggregated String value comprised of the
     * <i>groups</i> matched if valid or <code>null</code> if invalid
     */
    public String validate(final String value) {
        if (value == null) {
            return null;
        }
        for (final Pattern pattern : patterns) {
            final Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                final int count = matcher.groupCount();
                if (count == 1) {
                    return matcher.group(1);
                }
                final StringBuilder buffer = new StringBuilder();
                for (int j = 0; j < count; j++) {
                    final String component = matcher.group(j+1);
                    if (component != null) {
                        buffer.append(component);
                    }
                }
                return buffer.toString();
            }
        }
        return null;
    }

    /**
     * Provides a String representation of this validator.
     * @return A String representation of this validator.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("RegexValidator{");
        for (int i = 0; i < patterns.length; i++) {
            if (i > 0) {
                buffer.append(",");
            }
            buffer.append(patterns[i].pattern());
        }
        buffer.append("}");
        return buffer.toString();
    }

}