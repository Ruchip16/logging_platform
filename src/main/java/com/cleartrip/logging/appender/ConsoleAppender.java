package com.cleartrip.logging.appender;

import com.cleartrip.logging.formatter.Formatter;
import com.cleartrip.logging.formatter.SimpleFormatter;
import com.cleartrip.logging.model.LogRecord;

public class ConsoleAppender implements Appender {
    private Formatter formatter = new SimpleFormatter();
    private final String name = "console";

    @Override
    public void append(LogRecord record) {
        System.out.println(formatter.format(record));
    }

    @Override public void setFormatter(Formatter formatter) { this.formatter = formatter; }
    @Override public String getName() { return name; }
    @Override public void close() { /* nothing to close */ }
}