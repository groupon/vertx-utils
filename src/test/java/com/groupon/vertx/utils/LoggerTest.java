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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import com.arpnetworking.logback.StenoMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Gil Markham (gil at groupon dot com)
 */
@PrepareForTest(org.slf4j.LoggerFactory.class)
@RunWith(PowerMockRunner.class)
public class LoggerTest {
    private org.slf4j.Logger slf4jLogger;
    private Logger logger;
    @Captor
    private ArgumentCaptor<Object> paramCaptor;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());
        mockStatic(org.slf4j.LoggerFactory.class);
        slf4jLogger = mock(org.slf4j.Logger.class);
        when(org.slf4j.LoggerFactory.class, "getLogger", LoggerTest.class).thenReturn(slf4jLogger);
        logger = Logger.getLogger(LoggerTest.class);
    }

    @Test
    public void logInfoSimpleEvent() throws Exception {
        when(slf4jLogger.isInfoEnabled()).thenReturn(true);
        logger.info("method", "event");
        verify(slf4jLogger, times(1)).info(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method"}, (Object[]) values.get(1));
    }

    @Test
    public void logInfoExtendedEvent() throws Exception {
        when(slf4jLogger.isInfoEnabled()).thenReturn(true);
        logger.info("method", "event", new String[]{"text", "int", "boolean"}, "aValue", 2L, false);
        verify(slf4jLogger, times(1)).info(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "text", "int", "boolean"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "aValue", 2L, false}, (Object[]) values.get(1));
    }

    @Test
    public void logWarnSimpleEvent() throws Exception {
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);
        logger.warn("method", "event");
        verify(slf4jLogger, times(1)).warn(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method"}, (Object[]) values.get(1));
    }

    @Test
    public void logWarnSimpleEventWithError() throws Exception {
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);
        Exception error = new Exception("error");
        logger.warn("method", "event", error);
        verify(slf4jLogger, times(1)).warn(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture(), eq(error));

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertArrayEquals(new String[]{"eventSource", "method"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method"}, (Object[]) values.get(1));
        assertEquals(error, values.get(2));
    }

    @Test
    public void logWarnExtendedEvent() throws Exception {
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);
        logger.warn("method", "event", new String[]{"text", "int", "boolean"}, "aValue", 2L, false);
        verify(slf4jLogger, times(1)).warn(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "text", "int", "boolean"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "aValue", 2L, false}, (Object[]) values.get(1));
    }

    @Test
    public void logWarnExtendedEventWithError() throws Exception {
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);
        Exception error = new Exception("error");
        logger.warn("method", "event", new String[]{"text", "int", "boolean"}, "aValue", 2L, false, error);
        verify(slf4jLogger, times(1)).warn(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture(), eq(error));

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "text", "int", "boolean"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "aValue", 2L, false}, (Object[]) values.get(1));
        assertEquals(error, values.get(2));
    }

    @Test
    public void logWarnExtendedEventWithLessKeysThenValues() throws Exception {
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);
        Exception error = new Exception("error");
        logger.warn("method", "event", new String[]{"text", "int"}, "aValue", 2L, false, error);
        verify(slf4jLogger, times(1)).warn(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture(), eq(error));

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "text", "int"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "aValue", 2L}, (Object[]) values.get(1));
        assertEquals(error, values.get(2));
    }

    @Test
    public void logWarnExtendedEventWithLessValuesThenKeys() throws Exception {
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);
        Exception error = new Exception("error");
        logger.warn("method", "event", new String[]{"text", "int", "boolean"}, "aValue", error);
        verify(slf4jLogger, times(1)).warn(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "text", "int", "boolean"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "aValue", error}, (Object[]) values.get(1));
    }

    @Test
    public void logErrorSimpleEvent() throws Exception {
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        logger.error("method", "event", "reason");
        verify(slf4jLogger, times(1)).error(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "reason"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "reason"}, (Object[]) values.get(1));
    }

    @Test
    public void logErrorSimpleEventWithError() throws Exception {
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        Exception error = new Exception("error");
        logger.error("method", "event", "reason", error);
        verify(slf4jLogger, times(1)).error(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture(), eq(error));

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "reason"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "reason"}, (Object[]) values.get(1));
        assertEquals(error, values.get(2));
    }

    @Test
    public void logErrorExtendedEvent() throws Exception {
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        logger.error("method", "event", "reason", new String[]{"text", "int", "boolean"}, "aValue", 2L, false);
        verify(slf4jLogger, times(1)).error(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "reason", "text", "int", "boolean"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "reason", "aValue", 2L, false}, (Object[]) values.get(1));
    }

    @Test
    public void logErrorExtendedEventWithError() throws Exception {
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        Exception error = new Exception("error");
        logger.error("method", "event", "reason", new String[]{"text", "int", "boolean"}, "aValue", 2L, false, error);
        verify(slf4jLogger, times(1)).error(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture(), eq(error));

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(3, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "reason", "text", "int", "boolean"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "reason", "aValue", 2L, false}, (Object[]) values.get(1));
        assertEquals(error, values.get(2));
    }

    @Test
    public void logErrorExtendedEventLessKeysThenValues() throws Exception {
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        logger.error("method", "event", "reason", new String[]{"text", "int"}, "aValue", 2L, false);
        verify(slf4jLogger, times(1)).error(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "reason", "text", "int"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "reason", "aValue", 2L}, (Object[]) values.get(1));
    }

    @Test
    public void logErrorExtendedEventWithErrorLessValuesThenKeys() throws Exception {
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        Exception error = new Exception("error");
        logger.error("method", "event", "reason", new String[]{"text", "int", "boolean"}, "aValue", error);
        verify(slf4jLogger, times(1)).error(eq(StenoMarker.ARRAY_MARKER), eq("event"), paramCaptor.capture(),
                paramCaptor.capture());

        List<Object> values = paramCaptor.getAllValues();
        assertNotNull(values);
        assertEquals(2, values.size());
        assertArrayEquals(new String[]{"eventSource", "method", "reason", "text", "int", "boolean"}, (String[]) values.get(0));
        assertArrayEquals(new Object[]{"loggerTest", "method", "reason", "aValue", error}, (Object[]) values.get(1));
    }
}
