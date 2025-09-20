package com.cleartrip.logging.model;

public enum LogLevel {
    ERROR(1), WARNING(2), INFO(3), DEBUG(4);

    private final int priority;
    LogLevel(int p) { this.priority = p; }
    public int getPriority() { return priority; }

    public static boolean shouldLog(LogLevel recordLevel, LogLevel configuredLevel) {
        return recordLevel.getPriority() <= configuredLevel.getPriority();
    }
}