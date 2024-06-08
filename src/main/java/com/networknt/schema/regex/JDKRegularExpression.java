package com.networknt.schema.regex;

import java.util.regex.Pattern;

/**
 * JDK {@link RegularExpression}. 
 */
class JDKRegularExpression implements RegularExpression {
    private final Pattern pattern;

    JDKRegularExpression(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(String value) {
        return this.pattern.matcher(value).find();
    }
}