/**
 * Copyright 2015 Groupon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.vertx.utils.config;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
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

    private static final ConfigParser DEFAULT_CONFIG_PARSER = new DefaultConfigParser();

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
    public void load(Object field, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> configFuture = load(field);
        configFuture.onComplete(handler);
    }

    public Future<JsonObject> load(Object field) {
        Future<JsonObject> configFuture;

        if (field != null) {
            if (field instanceof String) {
                configFuture = getOrLoadConfig((String) field);
            } else if (field instanceof JsonObject) {
                Promise<JsonObject> configPromise = Promise.promise();
                configFuture = configPromise.future();
                configPromise.complete((JsonObject) field);
            } else {
                Promise<JsonObject> configPromise = Promise.promise();
                configFuture = configPromise.future();
                configPromise.fail(new IllegalStateException("Field `config` must contain an object or a string (path to the JSON config file)"));
            }
        } else {
            Promise<JsonObject> configPromise = Promise.promise();
            configFuture = configPromise.future();
            configPromise.complete(new JsonObject());
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
        final Promise<JsonObject> configPromise = Promise.promise();

        if (loadedConfigs.containsKey(path)) {
            configPromise.complete(loadedConfigs.get(path));
        } else {
            final Future<JsonObject> loadedConfigFuture = loadAndParseConfigFromFilesystem(path);
            loadedConfigFuture.onComplete(result -> {
                if (result.succeeded()) {
                    JsonObject loadedConfig = result.result();
                    loadedConfigs.put(path, loadedConfig);
                    configPromise.complete(loadedConfig);
                } else {
                    configPromise.fail(result.cause());
                }
            });
        }

        return configPromise.future();
    }

    /**
     * Load configuration from the filesystem and parse it into a JsonObject
     *
     * @param path path to the configuration file
     * @return future that eventually contains the JsonObject representing the configuration
     */
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    private Future<JsonObject> loadAndParseConfigFromFilesystem(final String path) {
        final Promise<JsonObject> configPromise = Promise.promise();

        fileSystem.readFile(path, result -> {
            if (result.succeeded()) {
                try {
                    final ConfigParser configParser = getConfigParser();
                    JsonObject loadedConfig = configParser.parse(result.result().toString());
                    configPromise.complete(loadedConfig);
                } catch (Throwable e) {
                    configPromise.fail(e);
                }
            } else {
                configPromise.fail(result.cause());
            }
        });

        return configPromise.future();
    }

    @SuppressWarnings("unchecked")
    private ConfigParser getConfigParser()
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        final String configParserClassName = System.getProperty("vertx-utils.config-parser-class-name");
        if (configParserClassName != null) {
            return (ConfigParser) Class.forName(configParserClassName).getDeclaredConstructor().newInstance();
        }
        return DEFAULT_CONFIG_PARSER;
    }
}
