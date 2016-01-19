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
package com.groupon.vertx.utils.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test cases for ConfigLoader
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @version 2.0.1
 * @since 2.0.1
 */
public class ConfigLoaderTest {
    private static final int TEST_TIMEOUT = 500;
    private static final String TEST_PATH = "conf/foo/bar.json";
    private static final Buffer TEST_BUFFER_GOOD = Buffer.buffer("{\"foo\":\"bar\", \"baz\": false}");
    private static final Buffer TEST_BUFFER_BAD = Buffer.buffer("{\"foobarbazqux}");

    @Mock
    private FileSystem fileSystem;

    @Captor
    private ArgumentCaptor<AsyncResultHandler<Buffer>> handlerCaptor;

    private CountDownLatch latch;
    private ConfigLoader loader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        loader = new ConfigLoader(fileSystem);
        latch = new CountDownLatch(1);
    }

    @After
    public void ensureFinish() throws Exception {
        latch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);

        if (latch.getCount() != 0) {
            fail("Timed out; test did not finish in " + TEST_TIMEOUT + "ms");
        }
    }

    @Test
    public void testNullValues() {
        loader.load(null, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.succeeded());
                    assertNotNull(result.result());
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    @Test
    public void testJsonObjectValues() {
        final JsonObject testConfig = new JsonObject();
        loader.load(testConfig, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.succeeded());
                    assertNotNull(result.result());
                    assertEquals(testConfig, result.result());
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    @Test
    public void testBadNonNullValues() {
        loader.load(new Object(), new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.failed());
                    assertTrue(result.cause() instanceof IllegalStateException);
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    @Test
    public void testStringValuesAsFiles() throws Exception {
        loader.load(TEST_PATH, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.succeeded());
                    assertNotNull(result.result());
                } finally {
                    latch.countDown();
                }
            }
        });

        verify(fileSystem).readFile(eq(TEST_PATH), handlerCaptor.capture());
        handlerCaptor.getValue().handle(Future.succeededFuture(TEST_BUFFER_GOOD));
    }

    @Test
    public void testCachesStringValuesAsFiles() throws Exception {
        latch = new CountDownLatch(2);

        loader.load(TEST_PATH, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.succeeded());
                    assertNotNull(result.result());
                } finally {
                    latch.countDown();
                }
            }
        });

        verify(fileSystem).readFile(eq(TEST_PATH), handlerCaptor.capture());
        handlerCaptor.getValue().handle(Future.succeededFuture(TEST_BUFFER_GOOD));
        reset(fileSystem);

        loader.load(TEST_PATH, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.succeeded());
                    assertNotNull(result.result());
                    verifyZeroInteractions(fileSystem);
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    @Test
    public void testStringValuesAsBadFiles() throws Exception {
        loader.load(TEST_PATH, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.failed());
                    assertTrue(result.cause() instanceof FileSystemException);
                } finally {
                    latch.countDown();
                }
            }
        });

        verify(fileSystem).readFile(eq(TEST_PATH), handlerCaptor.capture());
        handlerCaptor.getValue().handle(Future.<Buffer>failedFuture(new FileSystemException("bad file")));
    }

    @Test
    public void testStringValuesAsFilesWithBadContent() throws Exception {
        loader.load(TEST_PATH, new AsyncResultHandler<JsonObject>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
                try {
                    assertTrue(result.failed());
                    assertTrue(result.cause() instanceof DecodeException);
                } finally {
                    latch.countDown();
                }
            }
        });

        verify(fileSystem).readFile(eq(TEST_PATH), handlerCaptor.capture());
        AsyncResult<Buffer> result = Future.succeededFuture(TEST_BUFFER_BAD);
        handlerCaptor.getValue().handle(result);
    }
}
