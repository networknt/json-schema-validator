package com.networknt.schema.path;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathTypeTest {

    @Test
    void rejectNull() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            PathType.fromJsonPath(null);
        });
    }

    @Test
    void rejectEmptyString() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            PathType.fromJsonPath("");
        });
    }

    @Test
    void acceptRoot() {
        assertEquals("", PathType.fromJsonPath("$"));
    }

    @Test
    void acceptSimpleIndex() {
        assertEquals("/0", PathType.fromJsonPath("$[0]"));
    }

    @Test
    void acceptSimpleProperty() {
        assertEquals("/a", PathType.fromJsonPath("$.a"));
    }

    @Test
    void acceptEscapedProperty() {
        assertEquals("/a", PathType.fromJsonPath("$['a']"));
    }

    @Test
    void hasSpecialCharacters() {
        assertEquals("/a.b/c-d", PathType.fromJsonPath("$['a.b']['c-d']"));
    }

}
