package com.groupon.vertx.utils.deployment;

import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Vertx;

/**
 * Instantiates Deployments;
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.1
 * @version 2.0.1
 */
public class DeploymentFactory {
    public Deployment createWorkerVerticle(Vertx vertx, String name, String className, boolean isMultiThreaded, AsyncResultHandler<String> doneHandler) {
        return new WorkerVerticleDeployment(vertx, name, className, isMultiThreaded, doneHandler);
    }


    public Deployment createVerticle(Vertx vertx, String name, String className, AsyncResultHandler<String> doneHandler) {
        return new VerticleDeployment(vertx, name, className, doneHandler);
    }
}
