/*
 * Copyright (c) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Test for SetView.
 */
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
    void testIsEmpty() {
        Set<Integer> a = new LinkedHashSet<>();
        a.add(1);
        a.add(2);

        SetView<Integer> view = new SetView<>();
        assertTrue(view.isEmpty());
        view.union(a);
        assertFalse(view.isEmpty());
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
    void testContains() {
        Set<Integer> a = new LinkedHashSet<>();
        Set<Integer> b = new LinkedHashSet<>();
        Set<Integer> c = new LinkedHashSet<>();
        a.add(1);
        a.add(2);
        c.add(3);

        Set<Integer> view = new SetView<Integer>().union(a).union(b).union(c);
        assertTrue(view.contains(1));
        assertTrue(view.contains(2));
        assertTrue(view.contains(3));
        assertFalse(view.contains(4));
    }

    @Test
    void testContainsAll() {
        Set<Integer> a = new LinkedHashSet<>();
        Set<Integer> b = new LinkedHashSet<>();
        Set<Integer> c = new LinkedHashSet<>();
        a.add(1);
        a.add(2);
        c.add(3);

        Set<Integer> view = new SetView<Integer>().union(a).union(b).union(c);
        Set<Integer> result = new HashSet<>();
        result.add(1);
        result.add(2);
        result.add(3);
        assertTrue(view.containsAll(result));
        result.add(4);
        assertFalse(view.containsAll(result));
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

        Object[] result = view.toArray();
        assertEquals(3, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
    }

    @Test
    void testToArrayArray() {
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

    @Test
    void testRetainAll() {
        Set<Integer> view = new SetView<>();
        assertThrows(UnsupportedOperationException.class, () -> view.retainAll(Collections.singleton(1)));
    }

}
