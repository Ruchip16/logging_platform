package com.cleartrip.logging.core;

import com.cleartrip.logging.appender.Appender;
import com.cleartrip.logging.config.LoggerConfig;
import com.cleartrip.logging.formatter.Formatter;
import com.cleartrip.logging.formatter.SimpleFormatter;
import com.cleartrip.logging.model.LogLevel;
import com.cleartrip.logging.model.LogRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LoggerImpl implements Logger {
    private final String name;
    private volatile LogLevel currentLevel;
    private final Formatter formatter = new SimpleFormatter();

    private final Map<LogLevel, List<Appender>> levelToAppenders = new ConcurrentHashMap<>();
    private final List<Appender> globalAppenders = Collections.synchronizedList(new ArrayList<>());

    public LoggerImpl(String name, LoggerConfig config) {
        this.name = Objects.requireNonNull(name);
        this.currentLevel = config.getRootLevel();
        for (var lvl : LogLevel.values()) {
            var apps = new ArrayList<>(config.getAppendersFor(lvl));
            levelToAppenders.put(lvl, Collections.synchronizedList(apps));
        }
    }

    @Override public String getName() { return name; }
    @Override public LogLevel getLevel() { return currentLevel; }
    @Override public void setLevel(LogLevel level) { this.currentLevel = level; }

    @Override public void info(String fmt, Object... args) { log(LogLevel.INFO, fmt, args); }
    @Override public void debug(String fmt, Object... args) { log(LogLevel.DEBUG, fmt, args); }
    @Override public void warn(String fmt, Object... args) { log(LogLevel.WARNING, fmt, args); }
    @Override public void error(String fmt, Object... args) { log(LogLevel.ERROR, fmt, args); }

    private void log(LogLevel level, String fmt, Object... args) {
        if (!LogLevel.shouldLog(level, currentLevel)) return;

        String message = formatMessage(fmt, args);
        LogRecord record = new LogRecord(level, name, message);

        var apps = levelToAppenders.getOrDefault(level, List.of());
        synchronized (apps) {
            for (Appender a : apps) sendToAppender(a, record);
        }
        synchronized (globalAppenders) {
            for (Appender a : globalAppenders) sendToAppender(a, record);
        }
    }

    private void sendToAppender(Appender a, LogRecord r) {
        try { a.append(r); }
        catch (Exception ex) { System.err.println("Appender " + a.getName() + " failed: " + ex.getMessage()); }
    }

    private String formatMessage(String fmt, Object... args) {
        if (fmt == null) return "null";
        if (args == null || args.length == 0) return fmt;
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        for (int i = 0; i < fmt.length(); i++) {
            char c = fmt.charAt(i);
            if (c == '{' && i + 1 < fmt.length() && fmt.charAt(i + 1) == '}' && argIndex < args.length) {
                sb.append(String.valueOf(args[argIndex++]));
                i++;
            } else {
                sb.append(c);
            }
        }
        while (argIndex < args.length) { sb.append(" ").append(args[argIndex++]); }
        return sb.toString();
    }

    @Override
    public void attachAppender(String levelOrGlobal, Appender appender) {
        if (levelOrGlobal == null || "global".equalsIgnoreCase(levelOrGlobal)) {
            globalAppenders.add(appender);
            return;
        }
        LogLevel lvl = LogLevel.valueOf(levelOrGlobal.toUpperCase());
        levelToAppenders.computeIfAbsent(lvl, k -> Collections.synchronizedList(new ArrayList<>())).add(appender);
    }

    @Override
    public void detachAppender(String appenderName) {
        synchronized (globalAppenders) {
            globalAppenders.removeIf(a -> a.getName().equalsIgnoreCase(appenderName));
        }
        for (var entry : levelToAppenders.entrySet()) {
            var list = entry.getValue();
            synchronized (list) {
                list.removeIf(a -> a.getName().equalsIgnoreCase(appenderName));
            }
        }
    }

    @Override
    public void shutdown() {
        synchronized (globalAppenders) {
            for (Appender a : globalAppenders) a.close();
            globalAppenders.clear();
        }
        for (var entry : levelToAppenders.entrySet()) {
            var list = entry.getValue();
            synchronized (list) {
                for (Appender a : list) {
                    a.close();
                }
                list.clear();
            }
}