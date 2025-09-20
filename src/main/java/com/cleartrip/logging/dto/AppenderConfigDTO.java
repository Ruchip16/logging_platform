package com.cleartrip.logging.dto;

import com.cleartrip.logging.model.LogLevel;

import java.util.List;

public class AppenderConfigDTO {
    private final LogLevel level;
    private final List<String> appenderNames;

    public AppenderConfigDTO(LogLevel level, List<String> appenderNames) {
        this.level = level;
        this.appenderNames = appenderNames;
    }

    public LogLevel getLevel() { return level; }
    public List<String> getAppenderNames() { return appenderNames; }
}