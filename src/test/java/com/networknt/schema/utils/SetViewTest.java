package com.networknt.schema.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class SetViewTest {

    @Test
    void testUnion() {
        Set<Integer> a = new LinkedHashSet<>();
        Set<Integer> b = new LinkedHashSet<>();
        Set<Integer> c = new LinkedHashSet<>();
        a.add(1);
        a.add(2);
        c.add(3);

        Set<Integer> view = new SetView<Integer>().union(a).union(b).union(c);
        assertEquals(3, view.size());
        List<Integer> values = view.stream().collect(Collectors.toList());
        assertEquals(1, values.get(0));
        assertEquals(2, values.get(1));
        assertEquals(3, values.get(2));
    }

    @Test
    void testToString() {
        Set<Integer> a = new LinkedHashSet<>();
        Set<Integer> b = new LinkedHashSet<>();
        Set<Integer> c = new LinkedHashSet<>();
        a.add(1);
        a.add(2);
        c.add(3);

        Set<Integer> view = new SetView<Integer>().union(a).union(b).union(c);
        String value = view.toString();
        assertEquals("[1, 2, 3]", value);
    }

    @Test
    void testEquals() {
        Set<Integer> a = new LinkedHashSet<>();
        Set<Integer> b = new LinkedHashSet<>();
        Set<Integer> c = new LinkedHashSet<>();
        a.add(1);
        a.add(2);
        c.add(3);

        Set<Integer> view = new SetView<Integer>().union(a).union(b).union(c);
        assertEquals(3, view.size());

        Set<Integer> result = new HashSet<>();
        result.add(1);
        result.add(2);
        result.add(3);
        assertEquals(result, view);
    }
    
    @Test
    void testToArray() {
        Set<Integer> a = new LinkedHashSet<>();
        Set<Integer> b = new LinkedHashSet<>();
        Set<Integer> c = new LinkedHashSet<>();
        a.add(1);
        a.add(2);
        c.add(3);

        Set<Integer> view = new SetView<Integer>().union(a).union(b).union(c);
        assertEquals(3, view.size());
        
        Integer[] result = view.toArray(new Integer[0]);
        assertEquals(3, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
    }

    @Test
    void testAddAll() {
        Set<Integer> view = new SetView<>();
        assertThrows(UnsupportedOperationException.class, () -> view.addAll(Collections.singleton(1)));
    }

    @Test
    void testAdd() {
        Set<Integer> view = new SetView<>();
        assertThrows(UnsupportedOperationException.class, () -> view.add(1));
    }

    @Test
    void testClear() {
        Set<Integer> view = new SetView<>();
        assertThrows(UnsupportedOperationException.class, () -> view.clear());
    }
    
    @Test
    void testRemove() {
        Set<Integer> view = new SetView<>();
        assertThrows(UnsupportedOperationException.class, () -> view.remove(1));
    }

    @Test
    void testRemoveAll() {
        Set<Integer> view = new SetView<>();
        assertThrows(UnsupportedOperationException.class, () -> view.removeAll(Collections.singleton(1)));
    }

}
