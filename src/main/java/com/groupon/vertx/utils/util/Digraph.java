/**
 * Copyright 2015 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.vertx.utils.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Directed graph
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.2
 * @version 2.0.2
 */
public class Digraph<E> {
    private Set<E> nodes;
    private final Map<E, Set<E>> adjacent;

    public Digraph() {
        this(0);
    }

    public Digraph(int initialCapacity) {
        nodes = new HashSet<>(initialCapacity);
        adjacent = new ConcurrentHashMap<>(initialCapacity);
    }

    public void addNode(E v) {
        nodes.add(v);
    }

    public void addEdge(E v, E w) {
        if (!nodes.contains(v) || !nodes.contains(w)) {
            throw new IllegalStateException("Both nodes must already exist in the graph prior to adding a connecting edge");
        }

        Set<E> set = adjacent.get(v);

        if (set == null) {
            set = Collections.synchronizedSet(new HashSet<E>());
            adjacent.put(v, set);
        }

        set.add(w);
    }

    public Set<E> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Iterable<E> getAdjacent(E v) {
        Set<E> set = adjacent.get(v);

        if (set == null) {
            set = Collections.emptySet();
        }

        return Collections.unmodifiableSet(set);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Digraph[");
        for (Map.Entry<E, Set<E>> entry : adjacent.entrySet()) {
            E v = entry.getKey();
            for (E w : entry.getValue()) {
                sb.append("[");
                sb.append(v.toString());
                sb.append(" --> ");
                sb.append(w.toString());
                sb.append("]");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
