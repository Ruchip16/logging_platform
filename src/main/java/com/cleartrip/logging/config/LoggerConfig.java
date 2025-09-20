package com.cleartrip.logging.config;

import com.cleartrip.logging.appender.Appender;
import com.cleartrip.logging.model.LogLevel;

import java.util.*;

public class LoggerConfig {
    private final Map<LogLevel, List<Appender>> perLevelAppenders = new EnumMap<>(LogLevel.class);
    private LogLevel rootLevel = LogLevel.DEBUG;

    public LoggerConfig() {
        for (var lvl : LogLevel.values()) perLevelAppenders.put(lvl, new ArrayList<>());
    }

    public void setRootLevel(LogLevel level) { this.rootLevel = level; }
    public LogLevel getRootLevel() { return rootLevel; }

    public void addAppenderForLevel(LogLevel level, Appender appender) {
        perLevelAppenders.computeIfAbsent(level, k -> new ArrayList<>()).add(appender);
    }

    public List<Appender> getAppendersFor(LogLevel level) {
        return Collections.unmodifiableList(perLevelAppenders.getOrDefault(level, List.of()));
    }

    public Map<LogLevel, List<Appender>> getMapping() {
        return Collections.unmodifiableMap(perLevelAppenders);
    }
}