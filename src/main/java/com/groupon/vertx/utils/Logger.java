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
package com.groupon.vertx.utils;

/**
 * Log wrapper that includes common fields that we want in all log messages.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @author Gil Markham (gil at groupon dot com)
 * @since 1.0.0
 */
public interface Logger {

    /**
     * Returns a new logger for the specified target class and event source.  If the event source is not specified it
     * will default to the simple name of the target class.
     *
     * @param targetClass target class for logging
     * @param eventSource the eventSource
     * @return a new com.groupon.vertx.utils.Logger
     */
    static Logger getLogger(Class<?> targetClass, String eventSource) {
        return new LoggerImpl(targetClass, eventSource);
    }

    /**
     * Returns a new logger for the specified target class.  The logger's event source is defaulted to the
     * simple name of the target class with the first letter lower cased.
     *
     * @param targetClass target class for logging
     * @return a new com.groupon.vertx.utils.Logger
     */
    static Logger getLogger(Class<?> targetClass) {
        return new LoggerImpl(targetClass, null);
    }

    void error(String method, String event, String reason);

    void error(String method, String event, String reason, Throwable throwable);

    void error(String method, String event, String reason, String[] extraValueNames, Object... extraValues);

    void info(String method, String event);

    void info(String method, String event, String[] extraValueNames, Object... extraValues);

    void warn(String method, String event);

    void warn(String method, String event, Throwable throwable);

    void warn(String method, String event, String[] extraValueNames, Object... extraValues);

    void debug(String method, String event);

    void debug(String method, String event, String[] extraValueNames, Object... extraValues);

    void trace(String method, String event);

    void trace(String method, String event, String[] extraValueNames, Object... extraValues);

    boolean isInfoEnabled();

    boolean isWarnEnabled();

    boolean isTraceEnabled();

    boolean isDebugEnabled();

    boolean isErrorEnabled();
}
