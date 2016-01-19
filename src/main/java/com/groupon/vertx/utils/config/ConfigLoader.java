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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

/**
 * Asynchronous config loading for verticles
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.1
 * @version 2.0.1
 */
public class ConfigLoader {
    private final ConcurrentMap<String, JsonObject> loadedConfigs = new ConcurrentHashMap<>();
    private FileSystem fileSystem;

    /**
     * @param fileSystem Shared Vertx reference
     */
    public ConfigLoader(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * Check if the configuration has already been loaded, and if so return that, otherwise
     * attempt to load the configuration from the filesystem and save the result
     *
     * @param field   JsonObject or String
     * @param handler AsyncResultHandler to be called when the config is ready
     */
    public void load(Object field, AsyncResultHandler<JsonObject> handler) {
        Future<JsonObject> configFuture = load(field);
        configFuture.setHandler(handler);
    }

    public Future<JsonObject> load(Object field) {
        Future<JsonObject> configFuture;

        if (field != null) {
            if (field instanceof String) {
                configFuture = getOrLoadConfig((String) field);
            } else if (field instanceof JsonObject) {
                configFuture = Future.future();
                configFuture.complete((JsonObject) field);
            } else {
                configFuture = Future.future();
                configFuture.fail(new IllegalStateException("Field `config` must contain an object or a string (path to the JSON config file)"));
            }
        } else {
            configFuture = Future.future();
            configFuture.complete(new JsonObject());
        }

        return configFuture;
    }

    /**
     * Check if the configuration has already been loaded, and if so return that, otherwise
     * attempt to load the configuration from the filesystem and save the result
     *
     * @param path path to the configuration file
     * @return future that eventually contains the JsonObject representing the configuration
     */
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    private Future<JsonObject> getOrLoadConfig(final String path) {
        final Future<JsonObject> configFuture = Future.future();

        if (loadedConfigs.containsKey(path)) {
            configFuture.complete(loadedConfigs.get(path));
        } else {
            final Future<JsonObject> loadedConfigFuture = loadAndParseConfigFromFilesystem(path);
            loadedConfigFuture.setHandler(new AsyncResultHandler<JsonObject>() {
                @Override
                public void handle(AsyncResult<JsonObject> result) {
                    if (result.succeeded()) {
                        JsonObject loadedConfig = result.result();
                        loadedConfigs.put(path, loadedConfig);
                        configFuture.complete(loadedConfig);
                    } else {
                        configFuture.fail(result.cause());
                    }
                }
            });
        }

        return configFuture;
    }

    /**
     * Load configuration from the filesystem and parse it into a JsonObject
     *
     * @param path path to the configuration file
     * @return future that eventually contains the JsonObject representing the configuration
     */
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    private Future<JsonObject> loadAndParseConfigFromFilesystem(final String path) {
        final Future<JsonObject> configFuture = Future.future();

        fileSystem.readFile(path, new AsyncResultHandler<Buffer>() {
            @Override
            public void handle(AsyncResult<Buffer> result) {
                if (result.succeeded()) {
                    try {
                        JsonObject loadedConfig = new JsonObject(result.result().toString());
                        configFuture.complete(loadedConfig);
                    } catch (Exception e) {
                        configFuture.fail(e);
                    }
                } else {
                    configFuture.fail(result.cause());
                }
            }
        });

        return configFuture;
    }
}
