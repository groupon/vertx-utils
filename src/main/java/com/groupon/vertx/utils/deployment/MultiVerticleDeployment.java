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
package com.groupon.vertx.utils.deployment;

import java.util.Iterator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.utils.Logger;
import com.groupon.vertx.utils.config.Config;
import com.groupon.vertx.utils.config.ConfigLoader;
import com.groupon.vertx.utils.config.VerticleConfig;

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
public class MultiVerticleDeployment {
    private static final Logger log = Logger.getLogger(MultiVerticleDeployment.class, "multiVerticleDeployment");

    private final Vertx vertx;
    private final DeploymentFactory deploymentFactory;
    private final ConfigLoader configLoader;
    private boolean started;

    public MultiVerticleDeployment(Vertx vertx, DeploymentFactory deploymentFactory, ConfigLoader configLoader) {
        this.vertx = vertx;
        this.deploymentFactory = deploymentFactory;
        this.configLoader = configLoader;
    }

    /**
     * Deploy all of the verticles
     * @param config config json data
     * @return future representing success or failure for the requested deploys
     */
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    public Future<Void> deploy(final JsonObject config) {
        if (started) {
            throw new IllegalStateException("Deployment already started");
        }

        log.info("deploy", "start");

        started = true;

        final Promise<Void> deploymentPromise = Promise.promise();
        final Future<Void> deploymentResult = deploymentPromise.future();
        final Config deployConfig;

        try {
            deployConfig = new Config(config);
        } catch (Exception e) {
            deploymentPromise.fail(e);
            return deploymentResult;
        }

        final int totalVerticles = deployConfig.size();

        log.info("start", "start", new String[]{"message"}, String.format("Deploying %d verticle(s)", totalVerticles));
        final DeploymentMonitorHandler deploymentMonitorHandler = new DeploymentMonitorHandler(totalVerticles, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                if (result.succeeded()) {
                    deploymentPromise.complete(null);
                } else {
                    deploymentPromise.fail(result.cause());
                }
            }
        });

        final Iterator<VerticleConfig> verticleConfigIterator = deployConfig.iterator();
        VerticleConfig verticleConfig = verticleConfigIterator.next();
        log.info("deploy", "deployFirstVerticle", new String[]{"message"}, String.format("Deploying verticle %s", verticleConfig.getName()));
        deployVerticle(verticleConfig, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if (verticleConfigIterator.hasNext()) {
                    VerticleConfig nextVerticleConfig = verticleConfigIterator.next();
                    log.info("deploy", "deployNextVerticle", new String[]{"message"}, String.format("Deploying verticle %s", nextVerticleConfig.getName()));
                    deployVerticle(nextVerticleConfig, this);
                }

                deploymentMonitorHandler.handle(result);
            }
        });

        return deploymentResult;
    }

    /**
     * Given a name, config, and finished handler, attempt to load the configuration and deploy the verticle
     *
     * @param config VerticleConfig with information about this verticle
     * @param doneHandler handler to invoke upon completion
     */
    protected void deployVerticle(final VerticleConfig config, final Handler<AsyncResult<String>> doneHandler) {

        final Deployment deployment;
        if (config.isWorker()) {
            deployment = deploymentFactory.createWorkerVerticle(vertx, config.getName(), config.getClassName(), doneHandler);
        } else {
            deployment = deploymentFactory.createVerticle(vertx, config.getName(), config.getClassName(), doneHandler);
        }

        // After the verticle config has been found, attempt to deploy the verticle
        configLoader.load(config.getConfig(), configResult -> {
            if (configResult.succeeded()) {
                deployment.deploy(config.getInstances(), configResult.result());
            } else {
                deployment.abort(new Exception(String.format("Failed to load config for verticle %s", config.getName()), configResult.cause()));
            }
        });
    }
}
