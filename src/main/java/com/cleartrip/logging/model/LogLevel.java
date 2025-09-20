package com.cleartrip.logging.model;
/**
 * Lower numeric value = more severe.
 * Relationship: ERROR(1) <- WARNING(2) <- INFO(3) <- DEBUG(4)
 */
public enum LogLevel {
    ERROR(1), WARNING(2), INFO(3), DEBUG(4);

    private final int priority;
    LogLevel(int p) { this.priority = p; }
    public int getPriority() { return priority; }

    public static boolean shouldLog(LogLevel recordLevel, LogLevel configuredLevel) {
        return recordLevel.getPriority() <= configuredLevel.getPriority();
    }
}