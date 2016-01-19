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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Topological sort for a digraph
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.2
 * @version 2.0.2
 */
public class TopSorter<E> {
    private final Digraph<E> digraph;

    public TopSorter(Digraph<E> digraph) {
        this.digraph = digraph;
    }

    public List<E> sort() {
        Set<E> nodes = digraph.getNodes();

        List<E> result = new ArrayList<>(nodes.size());
        Set<E> flagged = new HashSet<>(nodes.size());
        Set<E> visited = new HashSet<>(nodes.size());

        for (E node : nodes) {
            visit(node, result, flagged, visited);
        }

        return result;
    }

    private void visit(E node, List<E> result, Set<E> flagged, Set<E> visited) {
        if (flagged.contains(node)) {
            throw new IllegalStateException("Cycle detected; can only sort directed acyclic graphs");
        }

        if (!visited.contains(node)) {

            flagged.add(node);
            for (E edgeNode : digraph.getAdjacent(node)) {
                visit(edgeNode, result, flagged, visited);
            }

            flagged.remove(node);
            visited.add(node);
            result.add(node);
        }
    }
}
