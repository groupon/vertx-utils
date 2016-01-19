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

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

/**
 * Generic handler for meeting the healthcheck endpoint requirement.
 * This is the Synchronous version which makes a blocking call for heartbeat file
 * Below is an example of how to setup this endpoint in a simple http service:
 *
 * RouteMatcher matcher = new RouteMatcher();
 * matcher.get("/grpn/healthcheck", new SyncHealthcheckHandler(vertx, heartbeatFilePath);
 * matcher.head("/grpn/healthcheck", new SyncHealthcheckHandler(vertx, heartbeatFilePath);
 *
 * HttpServer httpServer = vertx.createHttpServer();
 * httpServer.requestHandler(matcher);
 * httpServer.list(port, host);
 *
 * @author Namrata Lele (nlele at groupon dot com)
 * @since 2.1.7
 */
public class SyncHealthcheckHandler extends HealthcheckHandler {

    private Vertx vertx;
    private String filePath;

    public SyncHealthcheckHandler(Vertx vertx, String filePath) {
        this.vertx = vertx;
        this.filePath = filePath;
    }

    @Override
    public void handle(final HttpServerRequest request) {
        final long startTime = System.currentTimeMillis();

        try {
            processHeartBeatResponse(vertx.fileSystem().existsBlocking(filePath), request, startTime);
        } catch (Exception ex) {
            processExceptionResponse(request, ex, startTime);
        }
    }
}
