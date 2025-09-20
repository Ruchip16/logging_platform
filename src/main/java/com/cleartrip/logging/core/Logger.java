package com.cleartrip.logging.core;

import com.cleartrip.logging.model.LogLevel;

public interface Logger {
    void info(String fmt, Object... args);
    void debug(String fmt, Object... args);
    void warn(String fmt, Object... args);
    void error(String fmt, Object... args);

    void setLevel(LogLevel level);
    LogLevel getLevel();

    void attachAppender(String levelOrGlobal, com.cleartrip.logging.appender.Appender appender);
    void detachAppender(String appenderName);

    String getName();
    void shutdown();
}