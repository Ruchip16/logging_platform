package com.cleartrip.logging.dto;

import com.cleartrip.logging.model.LogLevel;
import com.cleartrip.logging.model.LogRecord;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class LogRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LogLevel level;
    private final String loggerName;
    private final String message;
    private final LocalDateTime timestamp;
    private final String threadName;

    public LogRecordDTO(LogLevel level, String loggerName, String message, LocalDateTime timestamp, String threadName) {
        this.level = level;
        this.loggerName = loggerName;
        this.message = message;
        this.timestamp = timestamp;
        this.threadName = threadName;
    }

    public LogLevel getLevel() { return level; }
    public String getLoggerName() { return loggerName; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getThreadName() { return threadName; }

    @Override
    public String toString() {
        return "LogRecordDTO{" +
                "level=" + level +
                ", loggerName='" + loggerName + '\'' +
                ", threadName='" + threadName + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogRecordDTO)) return false;
        LogRecordDTO that = (LogRecordDTO) o;
        return level == that.level &&
                Objects.equals(loggerName, that.loggerName) &&
                Objects.equals(message, that.message) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(threadName, that.threadName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, loggerName, message, timestamp, threadName);
    }

    public static LogRecordDTO fromDomain(LogRecord rec) {
        return new LogRecordDTO(
                rec.getLevel(),
                rec.getLoggerName(),
                rec.getMessage(),
                rec.getTimestamp(),
                rec.getThread().getName()
        );
    }
}