package com.atoook.otsukailist.exception;

/**
 * サービス層でリソース未発見を表す例外。
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Create exception with message.
     *
     * @param message detail text
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Create exception with message and cause.
     *
     * @param message detail text
     * @param cause root cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
