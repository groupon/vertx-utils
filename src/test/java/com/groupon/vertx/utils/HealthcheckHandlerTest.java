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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class HealthcheckHandlerTest {
    private static final HttpResponseStatus OK = HttpResponseStatus.OK;
    private static final HttpResponseStatus SERVICE_UNAVAILABLE = HttpResponseStatus.SERVICE_UNAVAILABLE;
    private static final String CONTENT_TYPE = "plain/text";
    private static final String CACHE_CONTROL = "private, no-cache, no-store, must-revalidate";

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private HttpServerRequest request;

    @Mock
    private HttpServerResponse response;

    @Mock
    private AsyncResult<Boolean> existsResult;

    @Captor
    private ArgumentCaptor<Handler<AsyncResult<Boolean>>> existCaptor;

    private HealthcheckHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        stub(vertx.fileSystem()).toReturn(fileSystem);
        stub(request.response()).toReturn(response);
        stub(request.method()).toReturn(HttpMethod.GET);

        handler = new AsyncHealthcheckHandler(vertx, "filepath");
    }

    @Test
    public void testHandle() {
        stub(existsResult.result()).toReturn(true);
        handler.handle(request);

        verify(vertx, times(1)).fileSystem();

        verify(fileSystem, times(1)).exists(eq("filepath"), existCaptor.capture());

        existCaptor.getValue().handle(existsResult);

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).setStatusCode(OK.code());
        verify(response, times(1)).setStatusMessage(OK.reasonPhrase());
        verify(response, times(1)).end(OK.reasonPhrase());
    }

    @Test
    public void testSyncHandle() {
        handler = new SyncHealthcheckHandler(vertx, "filepath");
        stub(fileSystem.existsBlocking(eq("filepath"))).toReturn(true);
        stub(vertx.fileSystem()).toReturn(fileSystem);

        handler.handle(request);
        verify(vertx, times(1)).fileSystem();
        verify(fileSystem, times(1)).existsBlocking(eq("filepath"));

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).setStatusCode(OK.code());
        verify(response, times(1)).setStatusMessage(OK.reasonPhrase());
        verify(response, times(1)).end(OK.reasonPhrase());
    }

    @Test
    public void testHandleNotExists() {
        stub(existsResult.result()).toReturn(false);
        handler.handle(request);

        verify(vertx, times(1)).fileSystem();

        verify(fileSystem, times(1)).exists(eq("filepath"), existCaptor.capture());

        existCaptor.getValue().handle(existsResult);

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end(SERVICE_UNAVAILABLE.reasonPhrase());
    }

    @Test
    public void testSyncHandleNotExists() {
        handler = new SyncHealthcheckHandler(vertx, "filepath");
        stub(fileSystem.existsBlocking(eq("filepath"))).toReturn(false);
        stub(vertx.fileSystem()).toReturn(fileSystem);

        handler.handle(request);

        verify(vertx, times(1)).fileSystem();

        verify(fileSystem, times(1)).existsBlocking(eq("filepath"));

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end(SERVICE_UNAVAILABLE.reasonPhrase());
    }

    @Test
    public void testHandleExsistsException() {
        IllegalArgumentException exception = new IllegalArgumentException("Failed");

        doThrow(exception).when(fileSystem).exists(eq("filepath"), existCaptor.capture());

        handler.handle(request);

        verify(vertx, times(1)).fileSystem();
        verify(fileSystem, times(1)).exists(eq("filepath"), existCaptor.capture());

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end(SERVICE_UNAVAILABLE.reasonPhrase() + ": " + exception.getMessage());
    }

    @Test
    public void testSyncHandleExsistsException() {
        IllegalArgumentException exception = new IllegalArgumentException("Failed");

        doThrow(exception).when(fileSystem).existsBlocking(eq("filepath"));

        handler = new SyncHealthcheckHandler(vertx, "filepath");
        stub(vertx.fileSystem()).toReturn(fileSystem);

        handler.handle(request);

        verify(vertx, times(1)).fileSystem();
        verify(fileSystem, times(1)).existsBlocking(eq("filepath"));

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end(SERVICE_UNAVAILABLE.reasonPhrase() + ": " + exception.getMessage());
    }

    @Test
    public void testHandleHead() {
        stub(request.method()).toReturn(HttpMethod.HEAD);
        stub(existsResult.result()).toReturn(true);
        handler.handle(request);

        verify(vertx, times(1)).fileSystem();

        verify(fileSystem, times(1)).exists(eq("filepath"), existCaptor.capture());

        existCaptor.getValue().handle(existsResult);

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_LENGTH, "" + OK.reasonPhrase().length());
        verify(response, times(1)).setStatusCode(OK.code());
        verify(response, times(1)).setStatusMessage(OK.reasonPhrase());
        verify(response, times(1)).end();
    }

    @Test
    public void testSyncHandleHead() {
        stub(request.method()).toReturn(HttpMethod.HEAD);
        stub(existsResult.result()).toReturn(true);

        handler = new SyncHealthcheckHandler(vertx, "filepath");
        stub(fileSystem.existsBlocking(eq("filepath"))).toReturn(true);
        stub(vertx.fileSystem()).toReturn(fileSystem);

        handler.handle(request);


        verify(vertx, times(1)).fileSystem();

        verify(fileSystem, times(1)).existsBlocking(eq("filepath"));

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_LENGTH, "" + OK.reasonPhrase().length());
        verify(response, times(1)).setStatusCode(OK.code());
        verify(response, times(1)).setStatusMessage(OK.reasonPhrase());
        verify(response, times(1)).end();
    }

    @Test
    public void testHandleNotExistsHead() {
        stub(request.method()).toReturn(HttpMethod.HEAD);
        stub(existsResult.result()).toReturn(false);

        handler.handle(request);

        verify(vertx, times(1)).fileSystem();

        verify(fileSystem, times(1)).exists(eq("filepath"), existCaptor.capture());

        existCaptor.getValue().handle(existsResult);

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_LENGTH, "" + SERVICE_UNAVAILABLE.reasonPhrase().length());
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end();
    }

    @Test
    public void testSyncHandleNotExistsHead() {
        stub(request.method()).toReturn(HttpMethod.HEAD);

        handler = new SyncHealthcheckHandler(vertx, "filepath");
        stub(fileSystem.existsBlocking(eq("filepath"))).toReturn(false);
        stub(vertx.fileSystem()).toReturn(fileSystem);

        handler.handle(request);

        verify(vertx, times(1)).fileSystem();

        verify(fileSystem, times(1)).existsBlocking(eq("filepath"));

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_LENGTH, "" + SERVICE_UNAVAILABLE.reasonPhrase().length());
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end();
    }

    @Test
    public void testHandleExistsExceptionHead() {
        stub(request.method()).toReturn(HttpMethod.HEAD);

        IllegalArgumentException exception = new IllegalArgumentException("Failed");
        String body = SERVICE_UNAVAILABLE.reasonPhrase() + ": " + exception.getMessage();

        doThrow(exception).when(fileSystem).exists(eq("filepath"), existCaptor.capture());

        handler.handle(request);

        verify(vertx, times(1)).fileSystem();
        verify(fileSystem, times(1)).exists(eq("filepath"), existCaptor.capture());

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_LENGTH, "" + body.length());
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end();
    }

    @Test
    public void testSyncHandleExistsExceptionHead() {
        stub(request.method()).toReturn(HttpMethod.HEAD);

        IllegalArgumentException exception = new IllegalArgumentException("Failed");
        String body = SERVICE_UNAVAILABLE.reasonPhrase() + ": " + exception.getMessage();

        doThrow(exception).when(fileSystem).existsBlocking(eq("filepath"));

        handler = new SyncHealthcheckHandler(vertx, "filepath");
        stub(vertx.fileSystem()).toReturn(fileSystem);

        handler.handle(request);

        verify(vertx, times(1)).fileSystem();
        verify(fileSystem, times(1)).existsBlocking(eq("filepath"));

        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE);
        verify(response, times(1)).putHeader(HttpHeaderNames.CACHE_CONTROL, CACHE_CONTROL);
        verify(response, times(1)).putHeader(HttpHeaderNames.CONTENT_LENGTH, "" + body.length());
        verify(response, times(1)).setStatusCode(SERVICE_UNAVAILABLE.code());
        verify(response, times(1)).setStatusMessage(SERVICE_UNAVAILABLE.reasonPhrase());
        verify(response, times(1)).end();
    }
}
