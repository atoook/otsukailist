package com.atoook.otsukailist.config.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates app.cors.allowed-origins entries.
 *
 * <p>Policy:
 *
 * <ul>
 *   <li>Empty/null handling is delegated to @NotEmpty in AppCorsProperties.
 *   <li>Each non-wildcard value must be a valid HTTP(S) origin without
 *       path/query/fragment/user-info.
 *   <li>Wildcard "*" is allowed only when it is the single configured entry.
 * </ul>
 */
public class CorsOriginsValidator implements ConstraintValidator<ValidCorsOrigins, List<String>> {

  @Override
  public boolean isValid(List<String> origins, ConstraintValidatorContext context) {
    if (origins == null || origins.isEmpty()) {
      return true;
    }

    boolean hasWildcard = false;

    for (String rawOrigin : origins) {
      if (rawOrigin == null) {
        return false;
      }

      String origin = rawOrigin.trim();
      if (origin.isEmpty()) {
        return false;
      }

      if ("*".equals(origin)) {
        hasWildcard = true;
        continue;
      }

      if (origin.contains("*")) {
        return false;
      }

      if (!isValidOrigin(origin)) {
        return false;
      }
    }

    return !hasWildcard || origins.size() == 1;
  }

  private boolean isValidOrigin(String origin) {
    try {
      URI uri = new URI(origin);
      String scheme = uri.getScheme();
      if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
        return false;
      }

      if (uri.getHost() == null || uri.getHost().isBlank()) {
        return false;
      }

      if (uri.getRawPath() != null
          && !uri.getRawPath().isEmpty()
          && !"/".equals(uri.getRawPath())) {
        return false;
      }

      return uri.getRawQuery() == null
          && uri.getRawFragment() == null
          && uri.getRawUserInfo() == null;
    } catch (URISyntaxException e) {
      return false;
    }
  }
}
