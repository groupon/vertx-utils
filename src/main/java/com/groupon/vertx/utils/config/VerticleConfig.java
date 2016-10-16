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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Verticle configuration
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.2
 * @version 2.0.2
 */
public class VerticleConfig {
    private String name;
    private String className;
    private int instances;
    private Object config;
    private Set<String> dependencies;
    private boolean isWorker;
    private boolean isMultiThreaded;

    public VerticleConfig(String name, JsonObject deployConfig) {

        if (name == null) {
            throw new IllegalStateException("Field `name` not specified for verticle");
        }

        if (deployConfig == null) {
            throw new IllegalStateException(String.format("Verticle %s config cannot be null", name));
        }

        this.name = name;
        final Object instancesAsObject = deployConfig.getValue("instances");
        if (instancesAsObject instanceof Integer) {
            instances = (Integer) instancesAsObject;
        } else if (instancesAsObject instanceof String) {
            instances = parseInstances((String) instancesAsObject);
        } else {
            throw new ClassCastException("Unsupported class type for 'instances'");
        }
        className = deployConfig.getString("class");
        config = deployConfig.getValue("config");
        isWorker = deployConfig.getBoolean("worker", false);
        isMultiThreaded = deployConfig.getBoolean("multiThreaded", false);

        JsonArray dependencyJson = deployConfig.getJsonArray("dependencies");
        if (dependencyJson != null) {
            dependencies = new HashSet<>(dependencyJson.size());
            for (Object dep : dependencyJson) {
                if (dep instanceof String) {
                    dependencies.add((String) dep);
                }
            }
        } else {
            dependencies = Collections.emptySet();
        }

        if (instances < 1) {
            throw new IllegalStateException(String.format("Field `instances` not specified or less than 1 for verticle %s", name));
        }

        if (className == null) {
            throw new IllegalStateException(String.format("Field `className` not specified for for verticle %s", name));
        }
    }

    private int parseInstances(final String instancesAsString) {
        if (instancesAsString.endsWith("C")) {
            return Integer.parseInt(instancesAsString.substring(0, instancesAsString.length() - 1)) *
                    Runtime.getRuntime().availableProcessors();
        } else {
            return Integer.parseInt(instancesAsString);
        }
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public int getInstances() {
        return instances;
    }

    public Object getConfig() {
        return config;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public boolean isWorker() {
        return isWorker;
    }

    public boolean isMultiThreaded() {
        return isMultiThreaded;
    }
}
