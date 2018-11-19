/**
 * Copyright 2015 Groupon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.vertx.utils.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test cases for VerticleDeployment
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @version 2.0.1
 * @since 2.0.1
 */
@ExtendWith(MockitoExtension.class)
public class VerticleDeploymentTest {
    @Mock
    private Vertx vertx;

    @Mock
    private Handler<AsyncResult<String>> resultHandler;

    @Captor
    private ArgumentCaptor<Handler<AsyncResult<String>>> resultHandlerCaptor;

    @Captor
    private ArgumentCaptor<AsyncResult<String>> resultCaptor;

    @Captor
    private ArgumentCaptor<DeploymentOptions> optionCaptor;

    private JsonObject testConfig = new JsonObject();
    private Deployment deployment;

    @Test
    public void testDeploySuccess() {
        deployment = new VerticleDeployment(vertx, "foo", "com.groupon.vertx.Foo", resultHandler);
        deployment.deploy(100, testConfig);

        verify(vertx).deployVerticle(eq("com.groupon.vertx.Foo"), optionCaptor.capture(), resultHandlerCaptor.capture());

        DeploymentOptions options = optionCaptor.getValue();
        assertEquals(testConfig, options.getConfig());
        assertEquals(100, options.getInstances());

        resultHandlerCaptor.getValue().handle(Future.succeededFuture("success"));
        verify(resultHandler).handle(resultCaptor.capture());

        assertTrue(resultCaptor.getValue().succeeded());
        assertEquals("success", resultCaptor.getValue().result());
    }

    @Test
    public void testDeployFailure() {
        deployment = new VerticleDeployment(vertx, "foo", "com.groupon.vertx.Foo", resultHandler);
        deployment.deploy(100, testConfig);

        verify(vertx).deployVerticle(eq("com.groupon.vertx.Foo"), optionCaptor.capture(), resultHandlerCaptor.capture());

        DeploymentOptions options = optionCaptor.getValue();
        assertEquals(testConfig, options.getConfig());
        assertEquals(100, options.getInstances());

        resultHandlerCaptor.getValue().handle(Future.<String>failedFuture(new Exception("failure")));
        verify(resultHandler).handle(resultCaptor.capture());

        assertTrue(resultCaptor.getValue().failed());
        assertTrue(resultCaptor.getValue().cause().getMessage().contains("Failed to deploy verticle foo"));
    }

    @Test
    public void testAbort() {
        deployment = new VerticleDeployment(vertx, "foo", "com.groupon.vertx.Foo", resultHandler);
        deployment.abort(new Exception("failure"));

        verify(resultHandler).handle(resultCaptor.capture());
        assertTrue(resultCaptor.getValue().failed());
        assertTrue(resultCaptor.getValue().cause().getMessage().contains("Aborted deploying verticle foo"));

    }
}
