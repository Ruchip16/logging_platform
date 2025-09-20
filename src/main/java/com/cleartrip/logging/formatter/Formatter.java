package com.cleartrip.logging.formatter;

import com.cleartrip.logging.model.LogRecord;

public interface Formatter {
    String format(LogRecord record);
}