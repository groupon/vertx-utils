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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import com.groupon.vertx.utils.Logger;

/**
 * Handler that tracks the total number of verticles remaining and number of verticles that failed.
 * After all verticles have been deployed (successful or otherwise), it invokes the provided handler
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.1
 * @version 2.0.1
 */
public class DeploymentMonitorHandler implements Handler<AsyncResult<String>> {
    private static final Logger log = Logger.getLogger(DeploymentMonitorHandler.class, "verticleDeployHandler");

    private final ConcurrentLinkedQueue<Throwable> failures;
    private final AtomicInteger deploymentsRemaining;
    private final int totalVerticles;
    private final Future<Void> future;

    /**
     * @param totalVerticles number of verticles to wait for before invoking the finished handler
     * @param finishedHandler handler to invoke after all verticles have deployed
     */
    public DeploymentMonitorHandler(int totalVerticles, Handler<AsyncResult<Void>> finishedHandler) {
        this.totalVerticles = totalVerticles;

        failures = new ConcurrentLinkedQueue<>();
        deploymentsRemaining = new AtomicInteger(totalVerticles);

        future = Future.future();
        future.setHandler(finishedHandler);
    }

    @Override
    public void handle(AsyncResult<String> result) {
        checkForFailures(result);
        checkForCompletion();
    }

    private void checkForFailures(AsyncResult<String> result) {
        if (result.failed()) {
            failures.add(result.cause());
        } else if (result.result().isEmpty()) {
            failures.add(new Exception("Empty deployment ID; failed to deploy verticle"));
        }
    }

    private void checkForCompletion() {
        if (deploymentsRemaining.decrementAndGet() == 0) {
            handleCompletion();
        }
    }

    private void handleCompletion() {
        if (failures.isEmpty()) {
            log.info("handleCompletion", "success", new String[]{"message"}, String.format("Deployed %d verticle(s) successfully", totalVerticles));
            future.complete(null);
        } else {
            String reason = String.format("Failed to deploy %d of %d verticle(s)", failures.size(), totalVerticles);

            Exception cause = new Exception(reason);
            for (Throwable failure : failures) {
                cause.addSuppressed(failure);
            }

            log.error("handleCompletion", "error", reason, cause);

            future.fail(cause);
        }
    }
}
