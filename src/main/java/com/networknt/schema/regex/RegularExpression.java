package com.networknt.schema.regex;

import com.networknt.schema.ValidationContext;

/**
 * Regular expression.
 */
@FunctionalInterface
public interface RegularExpression {
    boolean matches(String value);

    static RegularExpression compile(String regex, ValidationContext validationContext) {
        if (null == regex) return s -> true;
        return validationContext.getConfig().isEcma262Validator()
            ? new JoniRegularExpression(regex)
            : new JDKRegularExpression(regex);
    }

}