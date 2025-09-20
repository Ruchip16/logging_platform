package com.cleartrip.logging.core;

import com.cleartrip.logging.appender.Appender;
import com.cleartrip.logging.config.LoggerConfig;
import com.cleartrip.logging.formatter.Formatter;
import com.cleartrip.logging.formatter.SimpleFormatter;
import com.cleartrip.logging.model.LogLevel;
import com.cleartrip.logging.model.LogRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CLEARTRIP LOGGING PLATFORM - CORE IMPLEMENTATION
 * ================================================
 * 
 * ARCHITECTURE & DESIGN APPROACH:
 * --------------------------------
 * This logging platform follows a layered architecture with separation of concerns:
 * 
 * 1. CORE LAYER (Logger/LoggerImpl): 
 *    - Handles log message processing, formatting, and routing
 *    - Manages log levels and filtering
 *    - Coordinates with appenders for output
 * 
 * 2. APPENDER LAYER (ConsoleAppender, FileAppender, NetworkAppender):
 *    - Strategy pattern implementation for different output destinations
 *    - Each appender handles its own formatting and I/O operations
 *    - NetworkAppender uses producer-consumer pattern with background threads
 * 
 * 3. CONFIGURATION LAYER (LoggerConfig, LogManager):
 *    - Centralized configuration management
 *    - Singleton pattern for LogManager to ensure single instance
 *    - Factory pattern for creating configured loggers
 * 
 * 4. DATA LAYER (LogRecord, LogLevel, DTOs):
 *    - Immutable data structures for log records
 *    - Enum-based log levels with priority ordering
 *    - DTO pattern for network serialization
 * 
 * THREAD SAFETY APPROACH:
 * -----------------------
 * - ConcurrentHashMap for level-to-appender mappings (thread-safe reads/writes)
 * - Synchronized collections for appender lists (atomic operations)
 * - Volatile keyword for log level (ensures visibility across threads)
 * - Synchronized blocks for critical sections in appender operations
 * - Producer-consumer pattern with BlockingQueue for network appender
 * 
 * KEY FEATURES IMPLEMENTED:
 * -------------------------
 * ✓ Multi-level logging (ERROR, WARNING, INFO, DEBUG) with priority filtering
 * ✓ Multiple output destinations (Console, File, Network) 
 * ✓ Runtime configuration changes (log levels, appender attachment/detachment)
 * ✓ Custom message formatting with placeholder support ("Hello {}")
 * ✓ Exception handling with graceful degradation
 * ✓ Resource management with proper cleanup (shutdown method)
 * ✓ DTO serialization for network transport
 * ✓ Thread-safe concurrent logging from multiple threads
 * 
 * DESIGN PATTERNS USED:
 * ---------------------
 * - Strategy Pattern: Different appender implementations
 * - Factory Pattern: LogManager creates configured loggers
 * - Singleton Pattern: LogManager instance management  
 * - Builder Pattern: LoggerConfig for flexible configuration
 * - Observer Pattern: Appenders observe log events
 * - Producer-Consumer: NetworkAppender background processing
 * 
 * PERFORMANCE CONSIDERATIONS:
 * ---------------------------
 * - Early log level filtering to avoid unnecessary processing
 * - Lazy message formatting (only format if log level permits)
 * - Non-blocking appender operations with fallback handling
 * - Background network processing to avoid I/O blocking
 * - Efficient string formatting with StringBuilder
 */
public class LoggerImpl implements Logger {
    private final String name;
    private volatile LogLevel currentLevel;
    private final Formatter formatter = new SimpleFormatter();

    private final Map<LogLevel, List<Appender>> levelToAppenders = new ConcurrentHashMap<>();
    private final List<Appender> globalAppenders = Collections.synchronizedList(new ArrayList<>());

    /**
     * CONSTRUCTOR APPROACH:
     * - Initialize logger with name and configuration
     * - Pre-populate level-to-appender mappings for all log levels
     * - Use synchronized collections for thread-safe appender management
     */
    public LoggerImpl(String name, LoggerConfig config) {
        this.name = Objects.requireNonNull(name);
        this.currentLevel = config.getRootLevel();
        // Pre-initialize all log levels with their configured appenders
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

    /**
     * CORE LOGGING LOGIC:
     * - Early filtering: Check log level before any processing
     * - Lazy formatting: Only format message if logging is permitted
     * - Dual routing: Send to both level-specific and global appenders
     * - Thread safety: Synchronize on appender collections during iteration
     */
    private void log(LogLevel level, String fmt, Object... args) {
        // Early exit optimization - avoid all processing if log level doesn't permit
        if (!LogLevel.shouldLog(level, currentLevel)) return;

        // Lazy message formatting - only format if we're actually logging
        String message = formatMessage(fmt, args);
        LogRecord record = new LogRecord(level, name, message);

        // Send to level-specific appenders (e.g., ERROR appenders)
        var apps = levelToAppenders.getOrDefault(level, List.of());
        synchronized (apps) {
            for (Appender a : apps) sendToAppender(a, record);
        }
        
        // Send to global appenders (configured for all levels)
        synchronized (globalAppenders) {
            for (Appender a : globalAppenders) sendToAppender(a, record);
        }
    }

    private void sendToAppender(Appender a, LogRecord r) {
        try { a.append(r); }
        catch (Exception ex) { System.err.println("Appender " + a.getName() + " failed: " + ex.getMessage()); }
    }

    /**
     * CUSTOM MESSAGE FORMATTING:
     * - Supports SLF4J-style placeholders: "Hello {}" 
     * - Efficient StringBuilder-based implementation
     * - Handles edge cases: null format, extra arguments
     * - Linear time complexity O(n) where n is format string length
     */
    private String formatMessage(String fmt, Object... args) {
        if (fmt == null) return "null";
        if (args == null || args.length == 0) return fmt;
        
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        
        // Parse format string and replace {} placeholders
        for (int i = 0; i < fmt.length(); i++) {
            char c = fmt.charAt(i);
            if (c == '{' && i + 1 < fmt.length() && fmt.charAt(i + 1) == '}' && argIndex < args.length) {
                sb.append(String.valueOf(args[argIndex++]));
                i++; // Skip the '}'
            } else {
                sb.append(c);
            }
        }
        
        // Append any remaining arguments
        while (argIndex < args.length) { 
            sb.append(" ").append(args[argIndex++]); 
        }
        return sb.toString();
    }

    /**
     * RUNTIME APPENDER ATTACHMENT:
     * - Supports both level-specific ("ERROR") and global ("global") attachment
     * - Thread-safe using computeIfAbsent and synchronized collections
     * - Flexible string-based level specification
     */
    @Override
    public void attachAppender(String levelOrGlobal, Appender appender) {
        if (levelOrGlobal == null || "global".equalsIgnoreCase(levelOrGlobal)) {
            globalAppenders.add(appender);
            return;
        }
        LogLevel lvl = LogLevel.valueOf(levelOrGlobal.toUpperCase());
        levelToAppenders.computeIfAbsent(lvl, k -> Collections.synchronizedList(new ArrayList<>())).add(appender);
    }

    /**
     * RUNTIME APPENDER DETACHMENT:
     * - Removes appender by name from all levels and global list
     * - Thread-safe removal using synchronized blocks
     * - Case-insensitive name matching for user convenience
     */
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

    /**
     * GRACEFUL SHUTDOWN:
     * - Closes all appenders to release resources (files, network connections)
     * - Clears all collections to prevent memory leaks
     * - Thread-safe cleanup using synchronized blocks
     * - Essential for NetworkAppender to stop background threads
     */
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
    }
}