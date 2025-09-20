package com.cleartrip.logging.formatter;

import com.cleartrip.logging.model.LogRecord;

public class SimpleFormatter implements Formatter {
    @Override
    public String format(LogRecord r) {
        return String.format("%s | %s | %s | %s | %s",
                r.getTimestamp().toString(),
                r.getLevel().name(),
                r.getLoggerName(),
                r.getThread().getName(),
                r.getMessage());
    }
}