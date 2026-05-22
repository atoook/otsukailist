package com.atoook.otsukailist.exception;

import java.util.UUID;

import lombok.Getter;

/** Signals that a write precondition did not match the latest resource version. */
@Getter
public class PreconditionFailedException extends RuntimeException {

  private final String resourceType;
  private final UUID resourceId;
  private final long expectedVersion;
  private final long currentVersion;

  /**
   * Create exception with version details.
   *
   * @param resourceType resource type name
   * @param resourceId resource identifier
   * @param expectedVersion version sent by the client
   * @param currentVersion latest version stored by the server
   */
  public PreconditionFailedException(
      String resourceType, UUID resourceId, long expectedVersion, long currentVersion) {
    super("このリソースは他のメンバーにより更新されています");
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.expectedVersion = expectedVersion;
    this.currentVersion = currentVersion;
  }
}
