package com.atoook.otsukailist.exception;

/** Signals that the client sent a syntactically correct request whose content is invalid. */
public class BadRequestException extends RuntimeException {

  /**
   * Create exception with message.
   *
   * @param message detail text
   */
  public BadRequestException(String message) {
    super(message);
  }

  /**
   * Create exception with message and cause.
   *
   * @param message detail text
   * @param cause root cause
   */
  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
