package com.cleartrip.logging.app;

import com.cleartrip.logging.appender.ConsoleAppender;
import com.cleartrip.logging.appender.FileAppender;
import com.cleartrip.logging.appender.NetworkAppender;
import com.cleartrip.logging.config.LoggerConfig;
import com.cleartrip.logging.manager.LogManager;
import com.cleartrip.logging.core.Logger;
import com.cleartrip.logging.model.LogLevel;

import java.nio.file.Path;
import java.util.List;

// extensible logging framework that filters by level and supports runtime reconfiguration
public class Driver {
    public static void main(String[] args) throws Exception {
        LoggerConfig cfg = new LoggerConfig();
        ConsoleAppender console = new ConsoleAppender();
        FileAppender file = new FileAppender(Path.of("app.log"));
        NetworkAppender network = new NetworkAppender();

        cfg.addAppenderForLevel(LogLevel.INFO, console);
        cfg.addAppenderForLevel(LogLevel.INFO, file);

        cfg.addAppenderForLevel(LogLevel.ERROR, console);
        cfg.addAppenderForLevel(LogLevel.ERROR, file);
        cfg.addAppenderForLevel(LogLevel.ERROR, network);

        cfg.addAppenderForLevel(LogLevel.WARNING, console);
        cfg.addAppenderForLevel(LogLevel.DEBUG, console);

        cfg.setRootLevel(LogLevel.INFO);

        LogManager.getInstance().init(cfg);

        Logger log = LogManager.getInstance().getLogger("OrderService");

        int id = 42;
        log.info("This is my first log with id: {}", id);
        log.debug("This debug won't be printed because root level is INFO: {}", "debug-obj");
        log.warn("Be careful: {} is almost out of stock", "Pizza");
        log.error("I got the error {} for id {}", "NullPointer", id);

        Thread.sleep(200);

        System.out.println("Network store snapshot (after initial logs):");
        List<?> snapshot1 = network.getNetworkDtoSnapshot();
        snapshot1.forEach(System.out::println);

        System.out.println("\n-- Changing log level to DEBUG at runtime --");
        log.setLevel(LogLevel.DEBUG);
        log.debug("Now this debug will be saved. details: {}", "detailed-debug");

        Thread.sleep(100);

        System.out.println("\n-- Attaching network appender to INFO at runtime --");
        log.attachAppender("INFO", network);
        log.info("Info after attaching network: {} {}", "two", "args");

        Thread.sleep(200);

        System.out.println("\nNetwork store snapshot (final):");
        network.getNetworkDtoSnapshot().forEach(System.out::println);

        System.out.println("\n-- Detaching file appender (file:app.log) --");
        log.detachAppender("file:app.log");
        log.info("This should not write to file, only console/network");

        log.shutdown();
        network.close();
        System.out.println("\nDriver finished.");
    }
}