package com.atoook.otsukailist.api;

import com.atoook.otsukailist.exception.BadRequestException;
import com.atoook.otsukailist.exception.PreconditionRequiredException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HttpPreconditions {

  private static final String MSG_IF_MATCH_REQUIRED = "If-Matchヘッダが必要です";
  private static final String MSG_IF_MATCH_INVALID = "If-Matchヘッダの形式が不正です";

  /**
   * Build a strong ETag from a resource version.
   *
   * @param version resource version
   * @return quoted ETag token
   */
  public static String toVersionEtag(long version) {
    return "\"" + version + "\"";
  }

  /**
   * Parse a required If-Match header as a resource version.
   *
   * @param ifMatch request If-Match header value
   * @return expected resource version
   */
  public static long requireIfMatchVersion(String ifMatch) {
    if (ifMatch == null || ifMatch.isBlank()) {
      throw new PreconditionRequiredException(MSG_IF_MATCH_REQUIRED);
    }

    return parseVersion(stripResourcePrefix(stripWeakValidators(stripQuotes(ifMatch.trim()))));
  }

  private static String stripWeakValidators(String token) {
    if (token.contains(",") || token.startsWith("W/") || "*".equals(token)) {
      throw new BadRequestException(MSG_IF_MATCH_INVALID);
    }
    return token;
  }

  private static String stripQuotes(String token) {
    if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
      return token.substring(1, token.length() - 1);
    }
    return token;
  }

  private static String stripResourcePrefix(String token) {
    if (token.startsWith("v")) {
      return token.substring(1);
    }
    if (token.contains(":v")) {
      return token.substring(token.lastIndexOf(":v") + 2);
    }
    return token;
  }

  private static long parseVersion(String token) {
    try {
      return Long.parseLong(token);
    } catch (NumberFormatException e) {
      throw new BadRequestException(MSG_IF_MATCH_INVALID, e);
    }
  }
}
