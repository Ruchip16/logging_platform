package com.cleartrip.logging.appender;

import com.cleartrip.logging.formatter.Formatter;
import com.cleartrip.logging.formatter.SimpleFormatter;
import com.cleartrip.logging.model.LogRecord;
import com.cleartrip.logging.exception.LoggingException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileAppender implements Appender {
    private final Path path;
    private BufferedWriter writer;
    private Formatter formatter = new SimpleFormatter();
    private final String name;

    public FileAppender(Path path) {
        this.path = path;
        this.name = "file:" + path.getFileName().toString();
        try {
            this.writer = new BufferedWriter(new FileWriter(path.toFile(), true));
        } catch (IOException e) {
            throw new LoggingException("Failed to open file: " + path, e);
        }
    }

    @Override
    public void append(LogRecord record) {
        String line = formatter.format(record);
        synchronized (this) {
            try {
                writer.write(line);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                throw new LoggingException("Failed to write to file: " + path, e);
            }
        }
    }

    @Override public void setFormatter(Formatter formatter) { this.formatter = formatter; }
    @Override public String getName() { return name; }

    @Override
    public void close() {
        synchronized (this) {
            try { if (writer != null) writer.close(); }
            catch (IOException e) { /* ignore on close */ }
        }
    }
}