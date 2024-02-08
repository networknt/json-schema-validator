package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class SetViewTest {

    @Test
    void test() {
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

}
