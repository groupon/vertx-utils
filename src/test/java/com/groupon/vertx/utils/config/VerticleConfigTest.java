/**
 * Copyright 2016 Inscope Metrics Inc.
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
package com.groupon.vertx.utils.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

/**
 * Test cases for VerticleConfig
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @version 3.2.0
 * @since 3.2.0
 */
public class VerticleConfigTest {

    @Test
    public void testInstances() {
        final VerticleConfig verticleConfig = new VerticleConfig(
                "testInstances",
                new JsonObject("{" +
                        "\"class\":\"com.example.MyVerticle\"," +
                        "\"config\":\"config/MyVerticleConfig.json\"," +
                        "\"instances\":20" +
                        "}"));
        assertEquals(20, verticleConfig.getInstances());
    }

    @Test
    public void testInstancesPerCore() {
        final int cores = Runtime.getRuntime().availableProcessors();
        final VerticleConfig verticleConfig = new VerticleConfig(
                "testInstancesPerCore",
                new JsonObject("{" +
                        "\"class\":\"com.example.MyVerticle\"," +
                        "\"config\":\"config/MyVerticleConfig.json\"," +
                        "\"instances\":\"21C\"" +
                        "}"));
        assertEquals(cores * 21, verticleConfig.getInstances());
    }

    @Test
    public void testInstancesTooFew() {
        assertThrows(IllegalStateException.class, () -> {
            new VerticleConfig(
                    "testInstancesTooFew",
                    new JsonObject("{" +
                            "\"class\":\"com.example.MyVerticle\"," +
                            "\"config\":\"config/MyVerticleConfig.json\"," +
                            "\"instances\":0" +
                            "}"));
        });
    }

    @Test
    public void testInstancesPerCoreTooFew() {
        assertThrows(IllegalStateException.class, () -> {
            new VerticleConfig(
                    "testInstancesPerCoreTooFew",
                    new JsonObject("{" +
                            "\"class\":\"com.example.MyVerticle\"," +
                            "\"config\":\"config/MyVerticleConfig.json\"," +
                            "\"instances\":\"0C\"" +
                            "}"));
        });
    }

    @Test
    public void testInstancesNan() {
        assertThrows(NumberFormatException.class, () -> {
            new VerticleConfig(
                    "testInstancesNan",
                    new JsonObject("{" +
                            "\"class\":\"com.example.MyVerticle\"," +
                            "\"config\":\"config/MyVerticleConfig.json\"," +
                            "\"instances\":\"AB\"" +
                            "}"));
        });
    }

    @Test
    public void testInstancesPerCoreNan() {
        assertThrows(NumberFormatException.class, () -> {
            new VerticleConfig(
                    "testInstancesPerCoreNan",
                    new JsonObject("{" +
                            "\"class\":\"com.example.MyVerticle\"," +
                            "\"config\":\"config/MyVerticleConfig.json\"," +
                            "\"instances\":\"ABC\"" +
                            "}"));
        });
    }
}
