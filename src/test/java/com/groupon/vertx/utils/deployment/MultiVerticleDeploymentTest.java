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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.groupon.vertx.utils.config.ConfigLoader;

/**
 * Created with IntelliJ IDEA.
 * User: tblease
 * Date: 1/8/14
 * Time: 1:19 PM
 */
public class MultiVerticleDeploymentTest {
    private static final int TEST_TIMEOUT = 500;
    private static final String VERTICLE_NAME_A = "TestVerticleA";
    private static final String VERTICLE_NAME_B = "TestVerticleB";
    private static final String VERTICLE_CLASS = "com.groupon.vertx.utils.TestVerticle";
    private static final JsonObject VERTICLE_CONFIG = new JsonObject();

    @Mock
    private Vertx vertx;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private Context context;

    @Mock
    private DeploymentFactory deploymentFactory;

    @Mock
    private Deployment deployment;

    private MultiVerticleDeployment multiVerticleDeployment;
    private CountDownLatch latch;
    private JsonObject config;

    @Captor
    private ArgumentCaptor<Handler<AsyncResult<String>>> handlerCaptor;

    private JsonObject createConfig() {
        JsonObject testVerticle = new JsonObject();
        testVerticle.put("instances", 4);
        testVerticle.put("class", VERTICLE_CLASS);
        testVerticle.put("config", VERTICLE_CONFIG);
        testVerticle.put("worker", false);
        testVerticle.put("multiThreaded", false);

        JsonObject verticles = new JsonObject();
        verticles.put(VERTICLE_NAME_A, testVerticle);
        verticles.put(VERTICLE_NAME_B, testVerticle);

        JsonObject tmpConfig = new JsonObject();
        tmpConfig.put("verticles", verticles);
        tmpConfig.put("abortOnFailure", true);

        return tmpConfig;
    }

    private Answer<Object> invokeHandlerWithResult(final Future<String> result) {
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                handlerCaptor.getValue().handle(result);
                return null;
            }
        };
    }

    public void stubDeploymentDeployWithResult(Future<String> result) {
        doAnswer(invokeHandlerWithResult(result))
                .when(deployment).deploy(any(Integer.class), any(JsonObject.class));
    }

    public void stubDeploymentAbortWithResult(Future<String> result) {
        doAnswer(invokeHandlerWithResult(result))
                .when(deployment).abort(any(Throwable.class));
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        multiVerticleDeployment = new MultiVerticleDeployment(vertx, deploymentFactory, new ConfigLoader(fileSystem));

        config = createConfig();

        when(deploymentFactory.createVerticle(eq(vertx), any(String.class),
                any(String.class), handlerCaptor.capture())).thenReturn(deployment);

        when(deploymentFactory.createWorkerVerticle(eq(vertx), any(String.class),
                any(String.class), any(Boolean.class), handlerCaptor.capture())).thenReturn(deployment);

        stubDeploymentDeployWithResult(Future.succeededFuture("success"));
        stubDeploymentAbortWithResult(Future.<String>failedFuture(new Exception("failure")));

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
    public void testSuccess() {
        multiVerticleDeployment.deploy(config).setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                assertTrue("Deployment should succeed", result.succeeded());
                latch.countDown();
            }
        });
    }

    @Test
    public void testBadConfig() {
        config.remove("verticles");

        multiVerticleDeployment.deploy(config).setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                assertTrue("Deployment should fail", result.failed());
                latch.countDown();
            }
        });

    }

    @Test
    public void testDeployFailure() {
        stubDeploymentDeployWithResult(Future.<String>failedFuture(new Exception("failure")));

        multiVerticleDeployment.deploy(config).setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                assertTrue("Deployment should fail", result.failed());
                latch.countDown();
            }
        });

    }

    @Test
    public void testBadVerticleConfigA() {
        config.getJsonObject("verticles").getJsonObject(VERTICLE_NAME_A).remove("instances");

        multiVerticleDeployment.deploy(config).setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                assertTrue("Deployment should fail", result.failed());
                latch.countDown();
            }
        });
    }

    @Test
    public void testBadVerticleConfigB() {
        config.getJsonObject("verticles").getJsonObject(VERTICLE_NAME_A).remove("class");

        multiVerticleDeployment.deploy(config).setHandler(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
                assertTrue("Deployment should fail", result.failed());
                latch.countDown();
            }
        });
    }

    @Test
    public void testDeploymentCannotBeReused() {
        multiVerticleDeployment.deploy(config);
        try {
            multiVerticleDeployment.deploy(config);
        } catch (IllegalStateException e) {
            assertNotNull("Throws IllegalStateException when trying to deploy a second time with the same object", e);
        }
        latch.countDown();
    }
}
