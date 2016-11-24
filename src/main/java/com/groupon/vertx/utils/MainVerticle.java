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
package com.groupon.vertx.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.utils.config.ConfigLoader;
import com.groupon.vertx.utils.deployment.DeploymentFactory;
import com.groupon.vertx.utils.deployment.MultiVerticleDeployment;

/**
 * Main verticle used to deploy the appropriate number of instances of the different verticles that
 * make up the push service.  This is done instead of providing the number of instances on the command line
 * so we can have a single instance of the metrics reporter and have greater control of the number of instances
 * of downstream verticles that we need.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 1.0.0
 * @version 2.0.1
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger log = Logger.getLogger(MainVerticle.class, "mainVerticle");
    private static final String ABORT_ON_FAILURE_FIELD = "abortOnFailure";
    private static final String MESSAGE_CODECS_FIELD = "messageCodecs";

    /**
     * @param startedResult future indicating when all verticles have been deployed successfully
     */
    @Override
    public void start(final Future<Void> startedResult) {
        final JsonObject config = config();
        final boolean abortOnFailure = config.getBoolean(ABORT_ON_FAILURE_FIELD, true);

        try {
            registerMessageCodecs(vertx, config, abortOnFailure);
        } catch (final CodecRegistrationException e) {
            log.error("start", "abort", "Shutting down due to one or more errors", e);
            vertx.close();
            return;
        }

        Future<Void> deployResult = deployVerticles(config);
        deployResult.setHandler(new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                if (result.succeeded()) {
                    startedResult.complete(null);
                } else {
                    if (abortOnFailure) {
                        log.warn("start", "abort", new String[]{"message"}, "Shutting down due to one or more errors");
                        vertx.close();
                    } else {
                        startedResult.fail(result.cause());
                    }
                }
            }
        });
    }

    public Future<Void> deployVerticles(JsonObject config) {
        return new MultiVerticleDeployment(vertx, new DeploymentFactory(), new ConfigLoader(vertx.fileSystem())).deploy(config);
    }

    /* package private */ static void registerMessageCodecs(
            final Vertx vertx,
            final JsonObject config,
            final boolean abortOnFailure)
            throws CodecRegistrationException {

        final JsonArray messageCodecs = config.getJsonArray(MESSAGE_CODECS_FIELD);
        if (messageCodecs != null) {
            for (final Object messageCodecClassNameObject : messageCodecs.getList()) {
                if (messageCodecClassNameObject instanceof String) {
                    final String messageCodecClassName = (String) messageCodecClassNameObject;
                    try {
                        final MessageCodec<?, ?>  messageCodec = (MessageCodec<?, ?>) Class.forName(messageCodecClassName).newInstance();
                        vertx.eventBus().registerCodec(messageCodec);
                    } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                        log.warn("registerMessageCodecs", "start", new String[]{"message", "messageCodecClassName"}, "Failed to instantiate message codec", messageCodecClassName, e);
                        if (abortOnFailure) {
                            throw new CodecRegistrationException(
                                    String.format(
                                            "Failed to instantiate message codec %s",
                                            messageCodecClassName),
                                    e);
                        }
                    }
                } else {
                    log.warn("registerMessageCodecs", "start", new String[]{"message", "messageCodecClassName"}, "Ignoring non-string message codec class name", messageCodecClassNameObject);
                    if (abortOnFailure) {
                        throw new CodecRegistrationException("Ignoring non-string message codec class name");
                    }
                }
            }
        }
    }

    /**
     * Exception for failures registering a Vert.x message codec.
     */
    /* package private */ static final class CodecRegistrationException extends Exception {

        private static final long serialVersionUID = 7375493553868519673L;

        /* package private */ CodecRegistrationException(final String message) {
            super(message);
        }

        /* package private */ CodecRegistrationException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
