package com.networknt.schema.regex;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Issue814Test {

    @Test
    void jdkTypePattern() {
        JDKRegularExpression ex = new JDKRegularExpression("^(list|date|time|string|enum|int|double|long|boolean|number)$");
        assertTrue(ex.matches("list"));
        assertTrue(ex.matches("string"));
        assertTrue(ex.matches("boolean"));
        assertTrue(ex.matches("number"));
        assertTrue(ex.matches("enum"));
        assertFalse(ex.matches("listZ"));
        assertFalse(ex.matches("AenumZ"));
        assertFalse(ex.matches("Anumber"));
    }

    @Test
    void jdkOptionsPattern() {
        JDKRegularExpression ex = new JDKRegularExpression("^\\d|[a-zA-Z_]$");
        assertTrue(ex.matches("5"));
        assertTrue(ex.matches("55"));
        assertTrue(ex.matches("5%"));
        assertTrue(ex.matches("a"));
        assertTrue(ex.matches("aa"));
        assertTrue(ex.matches("%a"));
        assertTrue(ex.matches("%_"));
        assertTrue(ex.matches("55aa"));
        assertTrue(ex.matches("5%%a"));
        assertFalse(ex.matches(""));
        assertFalse(ex.matches("%"));
        assertFalse(ex.matches("a5"));
    }

    @Test
    void joniTypePattern() {
        JoniRegularExpression ex = new JoniRegularExpression("^(list|date|time|string|enum|int|double|long|boolean|number)$");
        assertTrue(ex.matches("list"));
        assertTrue(ex.matches("string"));
        assertTrue(ex.matches("boolean"));
        assertTrue(ex.matches("number"));
        assertTrue(ex.matches("enum"));
        assertFalse(ex.matches("listZ"));
        assertFalse(ex.matches("AenumZ"));
        assertFalse(ex.matches("Anumber"));
    }

    @Test
    void joniOptionsPattern() {
        JoniRegularExpression ex = new JoniRegularExpression("^\\d|[a-zA-Z_]$");
        assertTrue(ex.matches("5"));
        assertTrue(ex.matches("55"));
        assertTrue(ex.matches("5%"));
        assertTrue(ex.matches("a"));
        assertTrue(ex.matches("aa"));
        assertTrue(ex.matches("%a"));
        assertTrue(ex.matches("%_"));
        assertTrue(ex.matches("55aa"));
        assertTrue(ex.matches("5%%a"));
        assertFalse(ex.matches(""));
        assertFalse(ex.matches("%"));
        assertFalse(ex.matches("a5"));
    }

}
