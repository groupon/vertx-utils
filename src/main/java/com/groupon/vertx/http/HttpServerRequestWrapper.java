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

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

/**
 * Base class for wrapping a Vert.x HttpServerRequest so that methods on it can be intercepted.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @author Trevor Mack (tmack at groupon dot com)
 * @since 2.1.6
 */
@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
public class HttpServerRequestWrapper implements HttpServerRequest {

    private HttpServerRequest serverRequest;
    private HttpServerResponse serverResponse;

    public HttpServerRequestWrapper(HttpServerRequest serverRequest) {
        this(serverRequest, serverRequest.response());
    }

    protected HttpServerRequestWrapper(HttpServerRequest serverRequest, HttpServerResponse serverResponse) {
        this.serverRequest = serverRequest;
        this.serverResponse = serverResponse;
    }

    public HttpServerRequest getServerRequest() {
        return this.serverRequest;
    }

    @Override
    public HttpVersion version() {
        return serverRequest.version();
    }

    @Override
    public HttpMethod method() {
        return serverRequest.method();
    }

    @Override
    public String uri() {
        return serverRequest.uri();
    }

    @Override
    public String path() {
        return serverRequest.path();
    }

    @Override
    public String query() {
        return serverRequest.query();
    }

    @Override
    public SocketAddress localAddress() {
        return serverRequest.localAddress();
    }

    @Override
    public HttpServerResponse response() {
        return serverResponse;
    }

    @Override
    public SocketAddress remoteAddress() {
        return serverRequest.remoteAddress();
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        return serverRequest.peerCertificateChain();
    }

    @Override
    public String absoluteURI() {
        return serverRequest.absoluteURI();
    }

    @Override
    public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
        serverRequest.bodyHandler(bodyHandler);
        return this;
    }

    @Override
    public NetSocket netSocket() {
        return serverRequest.netSocket();
    }

    @Override
    public HttpServerRequest setExpectMultipart(boolean expect) {
        serverRequest.setExpectMultipart(expect);
        return this;
    }

    @Override
    public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        serverRequest.uploadHandler(uploadHandler);
        return this;
    }

    @Override
    public MultiMap formAttributes() {
        return serverRequest.formAttributes();
    }

    @Override
    public MultiMap headers() {
        return serverRequest.headers();
    }

    @Override
    public MultiMap params() {
        return serverRequest.params();
    }

    @Override
    public HttpServerRequest pause() {
        serverRequest.pause();
        return this;
    }

    @Override
    public HttpServerRequest resume() {
        serverRequest.resume();
        return this;
    }

    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        serverRequest.exceptionHandler(handler);
        return this;
    }

    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        serverRequest.endHandler(endHandler);
        return this;
    }

    @Override
    public HttpServerRequest handler(Handler<Buffer> handler) {
        serverRequest.handler(handler);
        return this;
    }

    @Override
    public String getHeader(String s) {
        return serverRequest.getHeader(s);
    }

    @Override
    public String getHeader(CharSequence charSequence) {
        return serverRequest.getHeader(charSequence);
    }

    @Override
    public String getParam(String s) {
        return serverRequest.getParam(s);
    }

    @Override
    public boolean isExpectMultipart() {
        return serverRequest.isExpectMultipart();
    }

    @Override
    public String getFormAttribute(String s) {
        return serverRequest.getFormAttribute(s);
    }

    @Override
    public ServerWebSocket upgrade() {
        return serverRequest.upgrade();
    }

    @Override
    public boolean isEnded() {
        return serverRequest.isEnded();
    }
}
