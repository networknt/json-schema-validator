package com.networknt.schema.regex;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Issue814Test {

    @Test
    void jdkTypePattern() {
        JDKRegularExpression ex = new JDKRegularExpression("^list|date|time|string|enum|int|double|long|boolean|number$");
        assertTrue(ex.matches("list"));
        assertTrue(ex.matches("string"));
        assertTrue(ex.matches("boolean"));
        assertTrue(ex.matches("number"));
        assertTrue(ex.matches("enum"));
    }

    @Test
    void jdkOptionsPattern() {
        JDKRegularExpression ex = new JDKRegularExpression("^\\d*|[a-zA-Z_]+$");
        assertTrue(ex.matches("external"));
        assertTrue(ex.matches("external_gte"));
        assertTrue(ex.matches("force"));
        assertTrue(ex.matches("internal"));
    }

    @Test
    void joniTypePattern() {
        JoniRegularExpression ex = new JoniRegularExpression("^list|date|time|string|enum|int|double|long|boolean|number$");
        assertTrue(ex.matches("list"));
        assertTrue(ex.matches("string"));
        assertTrue(ex.matches("boolean"));
        assertTrue(ex.matches("number"));
        assertTrue(ex.matches("enum"));
    }

    @Test
    void joniOptionsPattern() {
        JoniRegularExpression ex = new JoniRegularExpression("^\\d*|[a-zA-Z_]+$");
        assertTrue(ex.matches("internal"));
        assertTrue(ex.matches("external"));
        assertTrue(ex.matches("external_gte"));
        assertTrue(ex.matches("force"));
    }

}
