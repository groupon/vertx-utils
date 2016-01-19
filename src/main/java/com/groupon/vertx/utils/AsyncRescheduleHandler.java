package com.groupon.vertx.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Generic handler wrapper that reschedules itself after async completion.
 *
 * @author Dusty Burwell (dburwell at groupon dot com)
 * @since 2.0.2
 */
public class AsyncRescheduleHandler implements Handler<Long> {

    private static final Logger log = Logger.getLogger(RescheduleHandler.class, "rescheduleHandler");
    private final Vertx vertx;
    private final Handler<Future<Void>> handler;
    private final int interval;

    public AsyncRescheduleHandler(Vertx vertx, Handler<Future<Void>> handler, int interval) {
        if (vertx == null) {
            throw new IllegalArgumentException("Vertx cannot be null");
        }

        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }

        this.vertx = vertx;
        this.handler = handler;
        this.interval = interval;
    }

    @Override
    public void handle(Long timer) {
        log.debug("handle", "started");
        final Handler<Long> that = this;

        Future<Void> handlerFuture = Future.future();
        handlerFuture.setHandler(new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> futureResult) {
                if (futureResult.failed()) {
                    log.error("handle", "exception", "unknown", futureResult.cause());
                }

                vertx.setTimer(interval, that);
                log.debug("handle", "rescheduled");
            }
        });

        handler.handle(handlerFuture);
    }
}
