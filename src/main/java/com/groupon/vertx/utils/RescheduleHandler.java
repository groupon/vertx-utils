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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Generic handler wrapper that reschedules itself after completion.
 *
 * @author Swati Kumar (swkumar at groupon dot com)
 * @since 1.0.4
 */
public class RescheduleHandler implements Handler<Long> {
    private static final Logger log = Logger.getLogger(RescheduleHandler.class);

    private final Vertx vertx;
    private final Handler<Long> handler;
    private final int interval;

    private long timerId;

    public RescheduleHandler(Vertx vertx, Handler<Long> handler, int interval) {
        if (vertx == null) {
            throw new NullPointerException("Vertx cannot be null");
        }

        if (handler == null) {
            throw new NullPointerException("Handler cannot be null");
        }

        this.vertx = vertx;
        this.handler = handler;
        this.interval = interval;
    }

    public void schedule() {
        if (timerId == 0) {
            timerId = vertx.setTimer(interval, this);
        }
    }

    private void reschedule() {
        timerId = 0;
        schedule();
    }

    public void cancel() {
        if (timerId != 0) {
            vertx.cancelTimer(timerId);
            timerId = 0;
        }
    }

    @Override
    public void handle(Long timer) {
        try {
            handler.handle(timer);
        } catch (Exception ex) {
            log.error("handleRequest", "exception", "unknown", ex);
        } finally {
            reschedule();
        }
    }
}
