package com.networknt.schema.keyword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.LongNode;

class JsonNodeValueTest {
    @Test
    void hashCollisionDoesNotMakeDifferentNumbersEqual() {
        JsonNodeValue zero = JsonNodeValue.of(LongNode.valueOf(0));
        JsonNodeValue collision = JsonNodeValue.of(LongNode.valueOf(0x100000001L));
        assertEquals(zero.hashCode(), collision.hashCode());
        Set<JsonNodeValue> values = new HashSet<>();
        values.add(zero);
        assertFalse(values.contains(collision));
        values.add(collision);
        assertEquals(2, values.size());
        assertTrue(values.contains(JsonNodeValue.of(DecimalNode.valueOf(new BigDecimal("4294967297.00")))));
    }
}
