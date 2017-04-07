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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

/**
 * Generic handler for meeting the healthcheck endpoint requirement.
 * It has two concrete handlers AsyncHealthcheckHandler and SyncHealthcheckHandler
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public abstract class HealthcheckHandler implements Handler<HttpServerRequest> {
    private static final Logger LOG = Logger.getLogger(HealthcheckHandler.class);
    private static final String CONTENT_TYPE = "text/plain";
    private static final String CACHE_CONTROL = "private, no-cache, no-store, must-revalidate";

    public abstract void handle(final HttpServerRequest request);

    protected void processHeartBeatResponse(Boolean exists, HttpServerRequest request, long startTime) {
        HttpResponseStatus status;
        final boolean includeBody = !request.method().equals(HttpMethod.HEAD);

        if (exists) {
            status = HttpResponseStatus.OK;
        } else {
            status = HttpResponseStatus.SERVICE_UNAVAILABLE;
        }

        setCommonHttpResponse(request, status);

        String responseBody = status.reasonPhrase();
        if (includeBody) {
            request.response().end(responseBody);
        } else {
            request.response().putHeader(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(responseBody.length()));
            request.response().end();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        LOG.debug("handle", "healthcheckResponse", new String[]{"method", "status", "totalTime"},
                request.method(), status.code(), totalTime);
    }

    protected void processExceptionResponse(HttpServerRequest request, Exception ex, long startTime) {
        HttpResponseStatus status = HttpResponseStatus.SERVICE_UNAVAILABLE;
        final boolean includeBody = !request.method().equals(HttpMethod.HEAD);
        String responseBody = status.reasonPhrase() + ": " + ex.getMessage();

        setCommonHttpResponse(request, status);

        if (includeBody) {
            request.response().end(responseBody);
        } else {
            request.response().putHeader(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(responseBody.length()));
            request.response().end();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        LOG.debug("handle", "healthcheckResponse", new String[] {"method", "status", "totalTime"}, request.method(),
                status.code(), totalTime);
    }

    private void setCommonHttpResponse(HttpServerRequest request, HttpResponseStatus status) {
        request.response().putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        request.response().putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        request.response().setStatusCode(status.code());
        request.response().setStatusMessage(status.reasonPhrase());
    }
}
