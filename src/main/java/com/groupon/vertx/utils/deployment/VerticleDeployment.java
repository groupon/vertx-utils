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

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.utils.Logger;

/**
 * Handle deployments of verticles
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.1
 * @version 2.0.1
 */
public class VerticleDeployment implements Deployment {
    private static final Logger log = Logger.getLogger(VerticleDeployment.class, "verticleDeployment");

    protected final Vertx vertx;
    protected final String name;
    protected final String className;
    protected final Promise<String> deployId;


    public VerticleDeployment(Vertx vertx, String name, String className, Handler<AsyncResult<String>> finishedHandler) {
        this.vertx = vertx;
        this.name = name;
        this.className = className;

        deployId = Promise.promise();
        deployId.future().onComplete(finishedHandler);
    }

    @Override
    public void deploy(final int instances, JsonObject config) {
        log.info("deploy", "start", new String[]{"instances", "name", "class"}, instances, name, className);

        doDeploy(instances, config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> deployResult) {
                if (deployResult.succeeded() && !deployResult.result().isEmpty()) {
                    log.debug("deploy", "success", new String[]{"message"}, String.format("Deployed verticle %s successfully", name));
                    deployId.complete(deployResult.result());
                } else {
                    String message = String.format("Failed to deploy verticle %s", name);
                    log.debug("deploy", "failure", new String[]{"message"}, message);
                    deployId.fail(new Exception(message, deployResult.cause()));
                }
            }
        });
    }

    protected void doDeploy(int instances, JsonObject config, Handler<AsyncResult<String>> handler) {
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(instances)
                .setConfig(config)
                .setWorker(false);
        vertx.deployVerticle(className, deploymentOptions, handler);
    }

    @Override
    public void abort(Throwable cause) {
        String message = String.format("Aborted deploying verticle %s", name);
        log.debug("abort", "failure", new String[]{"message"}, message);
        deployId.fail(new Exception(message, cause));
    }
}
