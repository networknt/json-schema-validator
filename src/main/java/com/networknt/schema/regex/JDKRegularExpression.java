package com.networknt.schema.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JDKRegularExpression implements RegularExpression {
    private final Pattern pattern;
    private final boolean hasStartAnchor;
    private final boolean hasEndAnchor;

    JDKRegularExpression(String regex) {
        this.pattern = Pattern.compile(regex);
        this.hasStartAnchor = '^' == regex.charAt(0);
        this.hasEndAnchor = '$' == regex.charAt(regex.length() - 1);
    }

    @Override
    public boolean matches(String value) {
        Matcher matcher = this.pattern.matcher(value);
        return matcher.find() && (!this.hasStartAnchor || 0 == matcher.start()) && (!this.hasEndAnchor || matcher.end() == value.length());
    }

}