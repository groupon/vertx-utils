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
package com.groupon.vertx.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.StreamPriority;

/**
 * Base class for wrapping a Vert.x HttpServerResponse so method can be intercepted.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @author Trevor Mack (tmack at groupon dot com)
 * @since 2.1.6
 */
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public class HttpServerResponseWrapper implements HttpServerResponse {

    private HttpServerResponse serverResponse;

    public HttpServerResponseWrapper(HttpServerResponse serverResponse) {
        this.serverResponse = serverResponse;
    }

    @Override
    public int getStatusCode() {
        return serverResponse.getStatusCode();
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        serverResponse.setStatusCode(statusCode);
        return this;
    }

    @Override
    public String getStatusMessage() {
        return serverResponse.getStatusMessage();
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        serverResponse.setStatusMessage(statusMessage);
        return this;
    }

    @Override
    public boolean isChunked() {
        return serverResponse.isChunked();
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        serverResponse.setChunked(chunked);
        return this;
    }

    @Override
    public MultiMap headers() {
        return serverResponse.headers();
    }

    @Override
    public HttpServerResponse putHeader(String name, String value) {
        serverResponse.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        serverResponse.putHeader(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, CharSequence value) {
        serverResponse.putHeader(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
        serverResponse.putHeader(name, values);
        return this;
    }

    @Override
    public MultiMap trailers() {
        return serverResponse.trailers();
    }

    @Override
    public HttpServerResponse putTrailer(String name, String value) {
        serverResponse.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        serverResponse.putTrailer(name, values);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, CharSequence value) {
        serverResponse.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value) {
        serverResponse.putTrailer(name, value);
        return this;
    }

    @Override
    public HttpServerResponse write(Buffer chunk) {
        serverResponse.write(chunk);
        return this;
    }

    @Override
    public HttpServerResponse write(Buffer buffer, Handler<AsyncResult<Void>> handler) {
        serverResponse.write(buffer, handler);
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk, String enc) {
        serverResponse.write(chunk, enc);
        return this;
    }

    @Override
    public HttpServerResponse write(String s, String s1, Handler<AsyncResult<Void>> handler) {
        serverResponse.write(s, s1, handler);
        return this;
    }

    @Override
    public HttpServerResponse write(String chunk) {
        serverResponse.write(chunk);
        return this;
    }

    @Override
    public HttpServerResponse write(String s, Handler<AsyncResult<Void>> handler) {
        serverResponse.write(s, handler);
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String filename) {
        serverResponse.sendFile(filename);
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
        serverResponse.sendFile(filename, resultHandler);
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String s, long l, long l1) {
        serverResponse.sendFile(s, l, l1);
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String s, long l, long l1, Handler<AsyncResult<Void>> handler) {
        serverResponse.sendFile(s, l, l1);
        return this;
    }

    @Override
    public HttpServerResponse writeContinue() {
        serverResponse.writeContinue();
        return this;
    }

    @Override
    public boolean ended() {
        return serverResponse.ended();
    }

    @Override
    public boolean closed() {
        return serverResponse.closed();
    }

    @Override
    public boolean headWritten() {
        return serverResponse.headWritten();
    }

    @Override
    public HttpServerResponse headersEndHandler(Handler<Void> handler) {
        serverResponse.headersEndHandler(handler);
        return this;
    }

    @Override
    public HttpServerResponse bodyEndHandler(Handler<Void> handler) {
        serverResponse.bodyEndHandler(handler);
        return this;
    }

    @Override
    public void end(String chunk) {
        serverResponse.end(chunk);
    }

    @Override
    public void end(String s, Handler<AsyncResult<Void>> handler) {
        serverResponse.end(s, handler);
    }

    @Override
    public void end(String chunk, String enc) {
        serverResponse.end(chunk, enc);
    }

    @Override
    public void end(String s, String s1, Handler<AsyncResult<Void>> handler) {
        serverResponse.end(s, s1, handler);
    }

    @Override
    public void end(Buffer chunk) {
        serverResponse.end(chunk);
    }

    @Override
    public void end(Buffer buffer, Handler<AsyncResult<Void>> handler) {
        serverResponse.end(buffer, handler);
    }

    @Override
    public void end() {
        serverResponse.end();
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        serverResponse.end(handler);
    }

    @Override
    public void close() {
        serverResponse.close();
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        serverResponse.setWriteQueueMaxSize(maxSize);
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return serverResponse.writeQueueFull();
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        serverResponse.drainHandler(handler);
        return this;
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        serverResponse.exceptionHandler(handler);
        return this;
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        serverResponse.closeHandler(handler);
        return this;
    }

    @Override
    public long bytesWritten() {
        return serverResponse.bytesWritten();
    }

    @Override
    public int streamId() {
        return serverResponse.streamId();
    }

    @Override
    public HttpServerResponse push(final HttpMethod httpMethod, final String host, final String path, final Handler<AsyncResult<HttpServerResponse>> handler) {
        return serverResponse.push(httpMethod, host, path, handler);
    }

    @Override
    public HttpServerResponse push(final HttpMethod httpMethod, final String path, final MultiMap headers, final Handler<AsyncResult<HttpServerResponse>> handler) {
        return serverResponse.push(httpMethod, path, headers, handler);
    }

    @Override
    public HttpServerResponse push(final HttpMethod httpMethod, final String path, final Handler<AsyncResult<HttpServerResponse>> handler) {
        return serverResponse.push(httpMethod, path, handler);
    }

    @Override
    public HttpServerResponse push(final HttpMethod httpMethod, final String host, final String path, final MultiMap headers, final Handler<AsyncResult<HttpServerResponse>> handler) {
        return serverResponse.push(httpMethod, host, path, headers, handler);
    }

    @Override
    public void reset() {
        serverResponse.reset();
    }

    @Override
    public void reset(final long code) {
        serverResponse.reset(code);
    }

    @Override
    public HttpServerResponse writeCustomFrame(final int type, final int flags, final Buffer payload) {
        return serverResponse.writeCustomFrame(type, flags, payload);
    }

    @Override
    public HttpServerResponse writeCustomFrame(HttpFrame frame) {
        serverResponse.writeCustomFrame(frame);
        return this;
    }

    @Override
    public HttpServerResponse setStreamPriority(StreamPriority streamPriority) {
        serverResponse.setStreamPriority(streamPriority);
        return this;
    }

    @Override
    public HttpServerResponse addCookie(Cookie cookie) {
        addCookie(cookie);
        return this;
    }

    @Override
    public @Nullable Cookie removeCookie(String name) {
        return serverResponse.removeCookie(name);
    }

    @Override
    public @Nullable Cookie removeCookie(String s, boolean b) {
        return serverResponse.removeCookie(s, b);
    }

    @Override
    public HttpServerResponse sendFile(String filename, long offset) {
        serverResponse.sendFile(filename, offset);
        return this;
    }

    @Override
    public HttpServerResponse sendFile(String filename, long offset, Handler<AsyncResult<Void>> resultHandler) {
        serverResponse.sendFile(filename, offset, resultHandler);
        return this;
    }

    @Override
    public HttpServerResponse endHandler(@Nullable Handler<Void> handler) {
        serverResponse.endHandler(handler);
        return null;
    }
}
