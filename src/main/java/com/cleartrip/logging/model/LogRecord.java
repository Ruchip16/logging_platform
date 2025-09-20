package com.cleartrip.logging.model;

import java.time.LocalDateTime;

public class LogRecord {
    private final LogLevel level;
    private final String loggerName;
    private final String message;
    private final LocalDateTime timestamp;
    private final Thread thread;

    public LogRecord(LogLevel level, String loggerName, String message) {
        this.level = level;
        this.loggerName = loggerName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.thread = Thread.currentThread();
    }

    public LogLevel getLevel() { return level; }
    public String getLoggerName() { return loggerName; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Thread getThread() { return thread; }
}