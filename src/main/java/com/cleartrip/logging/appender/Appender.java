package com.cleartrip.logging.appender;

import com.cleartrip.logging.formatter.Formatter;
import com.cleartrip.logging.model.LogRecord;

public interface Appender {
    void append(LogRecord record);
    void setFormatter(Formatter formatter);
    String getName();
    void close();
}