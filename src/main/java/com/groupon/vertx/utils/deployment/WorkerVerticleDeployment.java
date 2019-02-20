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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.groupon.vertx.utils.Logger;

/**
 * Handle deployments of worker verticles
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.1
 * @version 2.0.1
 */
public class WorkerVerticleDeployment extends VerticleDeployment {
    private static final Logger log = Logger.getLogger(WorkerVerticleDeployment.class, "workerDeployment");


    public WorkerVerticleDeployment(Vertx vertx, String name, String className, Handler<AsyncResult<String>> finishedHandler) {
        super(vertx, name, className, finishedHandler);
    }

    @Override
    protected void doDeploy(int instances, JsonObject config, Handler<AsyncResult<String>> handler) {
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setInstances(instances)
                .setConfig(config)
                .setWorker(true);
        vertx.deployVerticle(className, deploymentOptions, handler);
    }
}
