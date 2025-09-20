package com.cleartrip.logging.appender;

import com.cleartrip.logging.dto.LogRecordDTO;
import com.cleartrip.logging.formatter.Formatter;
import com.cleartrip.logging.formatter.SimpleFormatter;
import com.cleartrip.logging.model.LogRecord;
import com.cleartrip.logging.exception.LoggingException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class NetworkAppender implements Appender {
    private final String name = "network";
    private Formatter formatter = new SimpleFormatter();

    // queue preserves insertion order across threads
    private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>();
    // typed network store of DTOs
    private final List<LogRecordDTO> networkDtoStore = new CopyOnWriteArrayList<>();
    private final ExecutorService consumer;

    private volatile boolean running = true;

    public NetworkAppender() {
        consumer = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "network-appender-consumer");
            t.setDaemon(true);
            return t;
        });
        consumer.submit(this::consumeLoop);
    }

    @Override
    public void append(LogRecord record) {
        try {
            boolean offered = queue.offer(record, 50, TimeUnit.MILLISECONDS);
            if (!offered) {
                queue.put(record);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoggingException("Interrupted while enqueueing network log", e);
        }
    }

    private void consumeLoop() {
        try {
            while (running || !queue.isEmpty()) {
                LogRecord r = queue.poll(200, TimeUnit.MILLISECONDS);
                if (r != null) {
                    // convert domain to DTO and store (typed)
                    LogRecordDTO dto = LogRecordDTO.fromDomain(r);
                    networkDtoStore.add(dto);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<LogRecordDTO> getNetworkDtoSnapshot() {
        return new ArrayList<>(networkDtoStore);
    }

    @Override public void setFormatter(Formatter formatter) { this.formatter = formatter; }
    @Override public String getName() { return name; }

    @Override
    public void close() {
        running = false;
        consumer.shutdown();
        try { consumer.awaitTermination(1, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}