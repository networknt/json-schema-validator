package com.networknt.schema.regex;

import com.networknt.schema.SchemaContext;

/**
 * Regular expression.
 */
@FunctionalInterface
public interface RegularExpression {
    boolean matches(String value);

    static RegularExpression compile(String regex, SchemaContext schemaContext) {
        if (null == regex) return s -> true;
        return schemaContext.getSchemaRegistryConfig().getRegularExpressionFactory().getRegularExpression(regex);
    }

}