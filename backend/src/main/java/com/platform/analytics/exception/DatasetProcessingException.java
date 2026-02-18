package com.platform.analytics.exception;

public class DatasetProcessingException extends RuntimeException {

    public DatasetProcessingException(String message) {
        super(message);
    }

    public DatasetProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
