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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test cases for DeploymentMonitorHandler
 *
 * @author Tristan Blease (tblease at groupon dot com)
 * @since 2.0.1
 * @version 2.0.1
 */
public class DeploymentMonitorHandlerTest {
    @Mock
    private AsyncResultHandler<Void> resultHandler;

    @Captor
    private ArgumentCaptor<AsyncResult<Void>> result;

    private DeploymentMonitorHandler deploymentMonitorHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSuccess() {
        deploymentMonitorHandler = new DeploymentMonitorHandler(3, resultHandler);
        deploymentMonitorHandler.handle(Future.succeededFuture("success"));
        deploymentMonitorHandler.handle(Future.succeededFuture("success"));
        deploymentMonitorHandler.handle(Future.succeededFuture("success"));

        verify(resultHandler).handle(result.capture());
        assertTrue(result.getValue().succeeded());
    }

    @Test
    public void testFailure() {
        deploymentMonitorHandler = new DeploymentMonitorHandler(3, resultHandler);
        deploymentMonitorHandler.handle(Future.succeededFuture("success"));
        deploymentMonitorHandler.handle(Future.<String>failedFuture(new Exception("failure")));
        deploymentMonitorHandler.handle(Future.succeededFuture("success"));

        verify(resultHandler).handle(result.capture());
        assertTrue(result.getValue().failed());
        assertTrue(result.getValue().cause().getMessage().contains("Failed to deploy 1 of 3"));
    }
}
