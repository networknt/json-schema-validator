/*
 * Copyright (c) 2023 the original author or authors.
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
package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class JsonNodePathTest {

    @Test
    void getNameCount() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_POINTER);
        JsonNodePath path = root.append("hello").append("world");
        assertEquals(2, path.getNameCount());
    }

    @Test
    void getName() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_POINTER);
        JsonNodePath path = root.append("hello").append("world");
        assertEquals("hello", path.getName(0));
        assertEquals("world", path.getName(1));
        assertEquals("world", path.getName(-1));
        assertThrows(IllegalArgumentException.class, () -> path.getName(2));
    }

    @Test
    void compareTo() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_POINTER);
        JsonNodePath a = root.append("a");
        JsonNodePath aa = a.append("a");

        JsonNodePath b = root.append("b");
        JsonNodePath bb = b.append("b");
        JsonNodePath b1 = b.append(1);
        JsonNodePath bbb = bb.append("b");

        JsonNodePath c = root.append("c");
        JsonNodePath cc = c.append("c");

        List<JsonNodePath> paths = new ArrayList<>();
        paths.add(cc);
        paths.add(aa);
        paths.add(bb);

        paths.add(b1);

        paths.add(bbb);

        paths.add(b);
        paths.add(a);
        paths.add(c);

        Collections.sort(paths);

        String[] result = paths.stream().map(Object::toString).collect(Collectors.toList()).toArray(new String[0]);

        assertArrayEquals(new String[] { "/a", "/b", "/c", "/a/a", "/b/1", "/b/b", "/c/c", "/b/b/b" }, result);
    }

    @Test
    void equalsEquals() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_POINTER);
        JsonNodePath a1 = root.append("a");
        JsonNodePath a2 = root.append("a");
        assertEquals(a1, a2);
    }

    @Test
    void hashCodeEquals() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_POINTER);
        JsonNodePath a1 = root.append("a");
        JsonNodePath a2 = root.append("a");
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void getPathType() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_POINTER);
        assertEquals(PathType.JSON_POINTER, root.getPathType());
    }

    @Test
    void getElement() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_PATH);
        JsonNodePath path = root.append("hello").append(1).append("world");
        assertEquals("hello", path.getElement(0));
        assertEquals(Integer.valueOf(1), path.getElement(1));
        assertEquals("world", path.getElement(2));
        assertEquals("world", path.getElement(-1));
        assertEquals("$.hello[1].world", path.toString());
        assertThrows(IllegalArgumentException.class, () -> path.getName(3));
    }
    
    @Test
    void startsWith() {
        JsonNodePath root = new JsonNodePath(PathType.JSON_PATH);
        JsonNodePath path = root.append("items");
        JsonNodePath other = root.append("unevaluatedItems");
        assertTrue(path.startsWith(other.getParent()));

        path = root.append("allOf").append(0).append("items");
        other = root.append("allOf").append(1).append("unevaluatedItems");
        assertFalse(path.startsWith(other.getParent()));

        path = root.append("allOf").append(0).append("items");
        other = root.append("allOf").append(0).append("unevaluatedItems");
        assertTrue(path.startsWith(other.getParent()));
        
        path = root.append("items");
        other = root.append("items").append(0);
        assertTrue(path.startsWith(other.getParent()));
        
        path = root.append("allOf");
        other = root.append("allOf").append(0).append("items");
        assertFalse(path.startsWith(other.getParent()));
    }
}
