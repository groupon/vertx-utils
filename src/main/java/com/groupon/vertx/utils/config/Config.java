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
package com.groupon.vertx.utils.config;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.json.JsonObject;

import com.groupon.vertx.utils.util.Digraph;
import com.groupon.vertx.utils.util.TopSorter;

/**
 * Deployment configuration
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.2
 * @version 2.0.2
 */
public class Config implements Iterable<VerticleConfig> {
    private static final String VERTICLES_FIELD = "verticles";

    private int total;
    private Map<String, VerticleConfig> verticles;
    private List<VerticleConfig> orderedVerticles;

    public Config(JsonObject config) {
        final JsonObject verticleJson = config.getJsonObject(VERTICLES_FIELD);
        if (verticleJson == null) {
            throw new IllegalStateException("Required config field `" + VERTICLES_FIELD + "` is missing");
        }

        Set<String> verticleNames = verticleJson.fieldNames();

        total = verticleNames.size();
        verticles = new ConcurrentHashMap<>(total);

        for (String verticleName : verticleNames) {
            JsonObject verticleConfig = verticleJson.getJsonObject(verticleName);
            verticles.put(verticleName, new VerticleConfig(verticleName, verticleConfig));
        }

        determineLoadOrder();
    }

    private void determineLoadOrder() {
        Digraph<VerticleConfig> dependencyGraph = new Digraph<>(verticles.size());

        for (VerticleConfig verticle : verticles.values()) {
            dependencyGraph.addNode(verticle);

            if (verticle.getDependencies().size() > 0) {
                for (String dependencyName : verticle.getDependencies()) {
                    VerticleConfig dependency = verticles.get(dependencyName);

                    if (dependency != null) {
                        dependencyGraph.addNode(dependency);
                        dependencyGraph.addEdge(verticle, dependency);
                    } else {
                        throw new IllegalStateException(String.format("Verticle '%s' depends on unknown dependency '%s'", verticle.getName(), dependencyName));
                    }
                }
            }
        }

        orderedVerticles = new TopSorter<>(dependencyGraph).sort();
    }

    public int size() {
        return total;
    }

    @Override
    public Iterator<VerticleConfig> iterator() {
        return orderedVerticles.iterator();
    }
}
