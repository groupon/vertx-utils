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
 * Test cases for WorkerVerticleDeployment
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @version 2.0.1
 * @since 2.0.1
 */
@ExtendWith(MockitoExtension.class)
public class WorkerVerticleDeploymentTest {
    @Mock
    private Vertx vertx;

    @Mock
    private Handler<AsyncResult<String>> resultHandler;

    @Captor
    private ArgumentCaptor<Handler<AsyncResult<String>>> handlerCaptor;

    @Captor
    private ArgumentCaptor<DeploymentOptions> optionCaptor;

    private JsonObject testConfig = new JsonObject();

    @Test
    public void testDeploy() {
        new WorkerVerticleDeployment(vertx, "foo", "com.groupon.vertx.Foo", true, resultHandler).deploy(100, testConfig);

        verify(vertx).deployVerticle(eq("com.groupon.vertx.Foo"), optionCaptor.capture(), handlerCaptor.capture());

        DeploymentOptions options = optionCaptor.getValue();
        assertEquals(testConfig, options.getConfig());
        assertEquals(100, options.getInstances());
        assertTrue(options.isWorker());
    }
}
