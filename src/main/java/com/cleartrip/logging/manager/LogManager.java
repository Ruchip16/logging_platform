package com.cleartrip.logging.manager;

import com.cleartrip.logging.config.LoggerConfig;
import com.cleartrip.logging.core.Logger;
import com.cleartrip.logging.core.LoggerImpl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LogManager {
    private static final LogManager INSTANCE = new LogManager();
    private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();
    private LoggerConfig config;

    private LogManager() {}

    public static LogManager getInstance() { return INSTANCE; }

    public void init(LoggerConfig cfg) {
        this.config = cfg;
    }

    public Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, n -> new LoggerImpl(n, config));
    }
}