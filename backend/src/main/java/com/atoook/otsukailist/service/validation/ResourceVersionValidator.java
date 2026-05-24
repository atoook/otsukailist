package com.atoook.otsukailist.service.validation;

import java.util.UUID;

import com.atoook.otsukailist.exception.PreconditionFailedException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResourceVersionValidator {

  /**
   * Ensure the expected version matches the latest resource version.
   *
   * @param resourceType resource type name
   * @param resourceId resource identifier
   * @param expectedVersion version sent by the client
   * @param currentVersion latest version stored by the server
   */
  public static void requireCurrentVersion(
      String resourceType, UUID resourceId, long expectedVersion, long currentVersion) {
    if (expectedVersion != currentVersion) {
      throw new PreconditionFailedException(
          resourceType, resourceId, expectedVersion, currentVersion);
    }
  }
}
