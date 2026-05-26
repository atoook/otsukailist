package com.atoook.otsukailist.exception;

/** Signals that a write request is missing the required If-Match precondition. */
public class PreconditionRequiredException extends RuntimeException {

  /**
   * Create exception with message.
   *
   * @param message detail text
   */
  public PreconditionRequiredException(String message) {
    super(message);
  }
}
