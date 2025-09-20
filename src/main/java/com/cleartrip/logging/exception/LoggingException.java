package com.cleartrip.logging.exception;

public class LoggingException extends RuntimeException {
    public LoggingException(String msg) { super(msg); }
    public LoggingException(String msg, Throwable t) { super(msg, t); }
}