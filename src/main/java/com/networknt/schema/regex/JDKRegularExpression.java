package com.networknt.schema.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JDKRegularExpression implements RegularExpression {
    private final Pattern pattern;
    private final boolean hasStartAnchor;
    private final boolean hasEndAnchor;

    JDKRegularExpression(String regex) {
        // The patterns in JSON Schema are not implicitly anchored so we must
        // use Matcher.find(). However, this method does not honor the end
        // anchor when immediately preceded by a quantifier (e.g., ?, *, +).
        // To make this work in all cases, we wrap the pattern in a group. 
        this.hasStartAnchor = '^' == regex.charAt(0);
        this.hasEndAnchor = '$' == regex.charAt(regex.length() - 1);
        String pattern = regex;
        if (this.hasEndAnchor) {
            pattern = pattern.substring(this.hasStartAnchor ? 1 : 0, pattern.length() - 1);
            pattern = '(' + pattern + ")$";
            if (this.hasStartAnchor) pattern = '^' + pattern;
        }
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean matches(String value) {
        Matcher matcher = this.pattern.matcher(value);
        return matcher.find() && (!this.hasStartAnchor || 0 == matcher.start()) && (!this.hasEndAnchor || matcher.end() == value.length());
    }

}