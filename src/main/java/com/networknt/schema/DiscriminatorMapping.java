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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.networknt.schema.keyword.DiscriminatorState;
import com.networknt.schema.path.NodePath;

/**
 * Mutable discriminator state with an insertion journal for alternative branches.
 * AbstractMap and Map's default mutators route insertions through put, including
 * putAll, putIfAbsent, compute and merge. The backing map supplies constant-time
 * lookups; branch cleanup visits only entries inserted in that branch.
 */
final class DiscriminatorMapping extends AbstractMap<NodePath, DiscriminatorState> {
    private Map<NodePath, DiscriminatorState> entries;
    private final List<NodePath> insertions = new ArrayList<>();
    private int branchDepth;

    DiscriminatorMapping(Map<NodePath, DiscriminatorState> entries) {
        this.entries = entries;
    }

    @Override
    public Set<Entry<NodePath, DiscriminatorState>> entrySet() {
        return entries.entrySet();
    }

    @Override
    public DiscriminatorState get(Object path) {
        return entries.get(path);
    }

    @Override
    public boolean containsKey(Object path) {
        return entries.containsKey(path);
    }

    @Override
    public DiscriminatorState remove(Object path) {
        return entries.remove(path);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public DiscriminatorState put(NodePath path, DiscriminatorState state) {
        if (branchDepth > 0 && !entries.containsKey(path)) {
            insertions.add(path);
        }
        return entries.put(path, state);
    }

    int begin() {
        branchDepth++;
        return insertions.size();
    }

    void end(NodePath location, int start) {
        int retained = start;
        for (int i = start; i < insertions.size(); i++) {
            NodePath path = insertions.get(i);
            if (path.getNameCount() > location.getNameCount() && path.startsWith(location)) {
                entries.remove(path);
            } else {
                insertions.set(retained++, path);
            }
        }
        insertions.subList(retained, insertions.size()).clear();
        if (--branchDepth == 0) {
            insertions.clear();
        }
    }
}
