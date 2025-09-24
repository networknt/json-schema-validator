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
package com.networknt.schema.path;

import java.util.Objects;

/**
 * Represents a path to a JSON node.
 */
public class NodePath implements Comparable<NodePath> {
    private final PathType type;
    private final NodePath parent;

    private final String pathSegment;
    private final int pathSegmentIndex;

    private volatile String value = null; // computed lazily
    private int hash = 0; // computed lazily

    public NodePath(PathType type) {
        this.type = type;
        this.parent = null;
        this.pathSegment = null;
        this.pathSegmentIndex = -1;
    }

    private NodePath(NodePath parent, String pathSegment) {
        this.parent = parent;
        this.type = parent.type;
        this.pathSegment = pathSegment;
        this.pathSegmentIndex = -1;
    }

    private NodePath(NodePath parent, int pathSegmentIndex) {
        this.parent = parent;
        this.type = parent.type;
        this.pathSegment = null;
        this.pathSegmentIndex = pathSegmentIndex;
    }

    /**
     * Returns the parent path, or null if this path does not have a parent.
     *
     * @return the parent
     */
    public NodePath getParent() {
        return this.parent;
    }

    /**
     * Append the child token to the path.
     *
     * @param token the child token
     * @return the path
     */
    public NodePath append(String token) {
        return new NodePath(this, token);
    }

    /**
     * Append the index to the path.
     * 
     * @param index the index
     * @return the path
     */
    public NodePath append(int index) {
        return new NodePath(this, index);
    }

    /**
     * Gets the {@link PathType}.
     * 
     * @return the path type
     */
    public PathType getPathType() {
        return this.type;
    }

    /**
     * Gets the name element given an index.
     * <p>
     * The index parameter is the index of the name element to return. The element
     * that is closest to the root has index 0. The element that is farthest from
     * the root has index count -1.
     * 
     * @param index to return
     * @return the name element
     */
    public String getName(int index) {
        Object element = getElement(index);
        if (element != null) {
            return element.toString();
        }
        return null;
    }

    /**
     * Gets the element given an index.
     * <p>
     * The index parameter is the index of the element to return. The element that
     * is closest to the root has index 0. The element that is farthest from the
     * root has index count -1.
     * 
     * @param index to return
     * @return the element either a String or Integer
     */
    public Object getElement(int index) {
        if (index == -1) {
            if (this.pathSegmentIndex != -1) {
                return this.pathSegmentIndex;
            } else {
                return this.pathSegment;
            }
        }
        int nameCount = getNameCount();
        if (nameCount - 1 == index) {
            return this.getElement(-1);
        }
        int count = nameCount - index - 1;
        if (count < 0) {
            throw new IllegalArgumentException("");
        }
        NodePath current = this;
        for (int x = 0; x < count; x++) {
            current = current.parent;
        }
        return current.getElement(-1);
    }

    /**
     * Gets the number of name elements in the path.
     * 
     * @return the number of elements in the path or 0 if this is the root element
     */
    public int getNameCount() {
        int current = this.pathSegmentIndex == -1 && this.pathSegment == null ? 0 : 1;
        int parent = this.parent != null ? this.parent.getNameCount() : 0;
        return current + parent;
    }

    /**
     * Tests if this path starts with the other path.
     * 
     * @param other the other path
     * @return true if the path starts with the other path
     */
    public boolean startsWith(NodePath other) {
        int count = getNameCount();
        int otherCount = other.getNameCount();

        if (otherCount > count) {
            return false;
        } else if (otherCount == count) {
            return this.equals(other);
        } else {
            NodePath compare = this;
            int x = count - otherCount;
            while (x > 0) {
                compare = compare.getParent();
                x--;
            }
            return other.equals(compare);
        }
    }

    /**
     * Tests if this path contains a string segment that is an exact match.
     * <p>
     * This will not match if the segment is a number.
     * 
     * @param segment the segment to test
     * @return true if the string segment is found
     */
    public boolean contains(String segment) {
        boolean result = segment.equals(this.pathSegment);
        if (result) {
            return true;
        }
        NodePath path = this.getParent();
        while (path != null) {
            if (segment.equals(path.pathSegment)) {
                return true;
            }
            path = path.getParent();
        }
        return false;
    }

    @Override
    public String toString() {
        if (this.value == null) {
            String parentValue = this.parent == null ? type.getRoot() : this.parent.toString();
            if (pathSegmentIndex != -1) {
                this.value = this.type.append(parentValue, pathSegmentIndex);
            } else if (pathSegment != null) {
                this.value = this.type.append(parentValue, pathSegment);
            } else {
                this.value = parentValue;
            }
        }
        return this.value;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            h = Objects.hash(parent, pathSegment, pathSegmentIndex, type);
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodePath other = (NodePath) obj;
        return Objects.equals(pathSegment, other.pathSegment) && pathSegmentIndex == other.pathSegmentIndex
                && type == other.type && Objects.equals(parent, other.parent);
    }

    @Override
    public int compareTo(NodePath other) {
        if (this.parent != null && other.parent == null) {
            return 1;
        } else if (this.parent == null && other.parent != null) {
            return -1;
        } else if (this.parent != null && other.parent != null) {
            int result = this.parent.compareTo(other.parent);
            if (result != 0) {
                return result;
            }
        }
        String thisValue = this.getName(-1);
        String otherValue = other.getName(-1);
        if (thisValue == null && otherValue == null) {
            return 0;
        } else if (thisValue != null && otherValue == null) {
            return 1;
        } else if (thisValue == null && otherValue != null) {
            return -1;
        } else {
            return thisValue.compareTo(otherValue);
        }
    }
}
