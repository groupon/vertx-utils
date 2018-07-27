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

import java.util.Locale;

import com.arpnetworking.logback.StenoMarker;
import org.slf4j.LoggerFactory;

/**
 * Log wrapper that includes common fields that we want in all log messages.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public final class Logger {
    private static final String[] EMPTY_EXTRA_NAMES = new String[0];
    private org.slf4j.Logger slf4jLog;
    private String eventSource;
    private static final String[] BASE_KEYS = new String[]{"eventSource", "method"};
    private static final int BASE_KEYS_LENGTH = BASE_KEYS.length;
    private static final String[] BASE_ERROR_KEYS = new String[]{"eventSource", "method", "reason"};
    private static final int BASE_ERROR_KEYS_LENGTH = BASE_ERROR_KEYS.length;
    private static final int EVENT_SOURCE_INDEX = 0;
    private static final int METHOD_NAME_INDEX = 1;
    private static final int REASON_INDEX = 2;

    private Logger(Class<?> targetClass, String eventSource) {
        this.slf4jLog = LoggerFactory.getLogger(targetClass);

        if (eventSource == null) {
            String simpleName = targetClass.getSimpleName();
            this.eventSource = simpleName.substring(0, 1).toLowerCase(Locale.getDefault()) + simpleName.substring(1);
        } else {
            this.eventSource = eventSource;
        }
    }

    /**
     * Returns a new logger for the specified target class and event source.  If the event source is not specified it
     * will default to the simple name of the target class.
     *
     * @param targetClass target class for logging
     * @param eventSource the eventSource
     * @return a new com.groupon.vertx.utils.Logger
     */
    public static Logger getLogger(Class<?> targetClass, String eventSource) {
        return new Logger(targetClass, eventSource);
    }

    /**
     * Returns a new logger for the specified target class.  The logger's event source is defaulted to the
     * simple name of the target class with the first letter lower cased.
     *
     * @param targetClass target class for logging
     * @return a new com.groupon.vertx.utils.Logger
     */
    public static Logger getLogger(Class<?> targetClass) {
        return new Logger(targetClass, null);
    }

    public void error(String method, String event, String reason) {
        error(method, event, reason, null);
    }

    public void error(String method, String event, String reason, Throwable throwable) {
        error(method, event, reason, EMPTY_EXTRA_NAMES, throwable);
    }

    public void error(String method, String event, String reason, String[] extraValueNames, Object... extraValues) {
        if (isErrorEnabled()) {
            String[] errorKeyArray = buildErrorKeyArray(extraValueNames);
            Object[] errorValueArray = buildErrorValueArray(method, reason, extraValues, errorKeyArray.length);
            Throwable error = extractThrowable(extraValueNames, extraValues);

            if (error != null) {
                slf4jLog.error(StenoMarker.ARRAY_MARKER, event, errorKeyArray, errorValueArray, error);
            } else {
                slf4jLog.error(StenoMarker.ARRAY_MARKER, event, errorKeyArray, errorValueArray);
            }
        }
    }

    public void info(String method, String event) {
        info(method, event, null);
    }

    public void info(String method, String event, String[] extraValueNames, Object... extraValues) {
        if (isInfoEnabled()) {
            String[] keyArray = buildKeyArray(extraValueNames);
            Object[] valueArray = buildValueArray(method, extraValues, keyArray.length);
            Throwable error = extractThrowable(extraValueNames, extraValues);

            if (error != null) {
                slf4jLog.info(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray, error);
            } else {
                slf4jLog.info(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray);
            }
        }
    }

    public void warn(String method, String event) {
        warn(method, event, null);
    }

    public void warn(String method, String event, Throwable throwable) {
        warn(method, event, EMPTY_EXTRA_NAMES, throwable);
    }

    public void warn(String method, String event, String[] extraValueNames, Object... extraValues) {
        if (isWarnEnabled()) {
            String[] keyArray = buildKeyArray(extraValueNames);
            Object[] valueArray = buildValueArray(method, extraValues, keyArray.length);
            Throwable error = extractThrowable(extraValueNames, extraValues);

            if (error != null) {
                slf4jLog.warn(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray, error);
            } else {
                slf4jLog.warn(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray);
            }
        }
    }

    public void debug(String method, String event) {
        debug(method, event, null);
    }

    public void debug(String method, String event, String[] extraValueNames, Object... extraValues) {
        if (isDebugEnabled()) {
            String[] keyArray = buildKeyArray(extraValueNames);
            Object[] valueArray = buildValueArray(method, extraValues, keyArray.length);
            Throwable error = extractThrowable(extraValueNames, extraValues);

            if (error != null) {
                slf4jLog.debug(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray, error);
            } else {
                slf4jLog.debug(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray);
            }
        }
    }

    public void trace(String method, String event) {
        trace(method, event, null);
    }

    public void trace(String method, String event, String[] extraValueNames, Object... extraValues) {
        if (isTraceEnabled()) {
            String[] keyArray = buildKeyArray(extraValueNames);
            Object[] valueArray = buildValueArray(method, extraValues, keyArray.length);
            Throwable error = extractThrowable(extraValueNames, extraValues);

            if (error != null) {
                slf4jLog.trace(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray, error);
            } else {
                slf4jLog.trace(StenoMarker.ARRAY_MARKER, event, keyArray, valueArray);
            }
        }
    }

    private Throwable extractThrowable(String[] keys, Object[] values) {
        Throwable error = null;

        int keyLength = keys == null ? 0 : keys.length;
        if (values != null && values.length > keyLength) {
            if (values[values.length - 1] instanceof Throwable) {
                error = (Throwable) values[values.length - 1];
            }
        }

        return error;
    }

    private String[] buildErrorKeyArray(String[] keys) {
        if (keys == null || keys.length == 0) {
            return BASE_ERROR_KEYS;
        } else {
            String[] newKeyArray = new String[keys.length + BASE_ERROR_KEYS.length];
            int i = 0;
            for (; i < BASE_ERROR_KEYS.length; i++) {
                newKeyArray[i] = BASE_ERROR_KEYS[i];
            }

            for (; i - BASE_ERROR_KEYS.length < keys.length; i++) {
                newKeyArray[i] = keys[i - BASE_ERROR_KEYS.length];
            }

            return newKeyArray;
        }
    }

    private String[] buildKeyArray(String[] keys) {
        if (keys == null || keys.length == 0) {
            return BASE_KEYS;
        } else {
            String[] newKeyArray = new String[keys.length + BASE_KEYS.length];
            int i = 0;
            for (; i < BASE_KEYS.length; i++) {
                newKeyArray[i] = BASE_KEYS[i];
            }

            for (; i - BASE_KEYS.length < keys.length; i++) {
                newKeyArray[i] = keys[i - BASE_KEYS.length];
            }

            return newKeyArray;
        }
    }

    private Object[] buildErrorValueArray(String method, String reason, Object[] values, int keyLength) {
        Object[] newValues;
        if (values == null || values.length == 0) {
            newValues = new Object[BASE_ERROR_KEYS_LENGTH];
            newValues[EVENT_SOURCE_INDEX] = eventSource;
            newValues[METHOD_NAME_INDEX] = method;
            newValues[REASON_INDEX] = reason;
        } else {
            // Length should always be the base error keys plus the number of named value keys.
            int valueLength = Math.min(values.length + BASE_ERROR_KEYS_LENGTH, keyLength);
            newValues = new Object[valueLength];
            newValues[EVENT_SOURCE_INDEX] = eventSource;
            newValues[METHOD_NAME_INDEX] = method;
            newValues[REASON_INDEX] = reason;
            for (int i = 0; i < valueLength - BASE_ERROR_KEYS_LENGTH; i++) {
                newValues[i + BASE_ERROR_KEYS_LENGTH] = values[i];
            }
        }
        return newValues;
    }

    private Object[] buildValueArray(String method, Object[] values, int keyLength) {
        Object[] newValues;
        if (values == null || values.length == 0) {
            newValues = new Object[BASE_KEYS_LENGTH];
            newValues[EVENT_SOURCE_INDEX] = eventSource;
            newValues[METHOD_NAME_INDEX] = method;
        } else {
            // Length should always be the base keys plus the number of named value key pairs.
            int valueLength = Math.min(values.length + BASE_KEYS_LENGTH, keyLength);
            newValues = new Object[valueLength];
            newValues[EVENT_SOURCE_INDEX] = eventSource;
            newValues[METHOD_NAME_INDEX] = method;
            for (int i = 0; i < valueLength - BASE_KEYS_LENGTH; i++) {
                newValues[i + BASE_KEYS_LENGTH] = values[i];
            }
        }
        return newValues;
    }

    public boolean isInfoEnabled() {
        return slf4jLog.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return slf4jLog.isWarnEnabled();
    }

    public boolean isTraceEnabled() {
        return slf4jLog.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return slf4jLog.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return slf4jLog.isErrorEnabled();
    }
}
