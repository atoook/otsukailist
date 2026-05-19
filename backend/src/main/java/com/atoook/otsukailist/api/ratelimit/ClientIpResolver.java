package com.atoook.otsukailist.api.ratelimit;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.atoook.otsukailist.config.AppRateLimitProperties;

@Component
public class ClientIpResolver {
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";
  private static final String UNKNOWN_CLIENT = "unknown";

  private final AppRateLimitProperties properties;

  /**
   * Creates a resolver using the configured trusted proxy count.
   *
   * @param properties rate-limit settings
   */
  public ClientIpResolver(AppRateLimitProperties properties) {
    this.properties = properties;
  }

  /**
   * Resolves the client IP used as the rate-limit key.
   *
   * @param request current request
   * @return trusted client IP or remote address fallback
   */
  public String resolveClientIp(HttpServletRequest request) {
    String forwardedIp = resolveForwardedIp(request);
    if (forwardedIp != null) {
      return forwardedIp;
    }

    String remoteAddr = request.getRemoteAddr();
    return StringUtils.hasText(remoteAddr) ? remoteAddr : UNKNOWN_CLIENT;
  }

  private String resolveForwardedIp(HttpServletRequest request) {
    int trustedProxyCount = properties.getTrustedProxyCount();
    if (trustedProxyCount <= 0) {
      return null;
    }

    List<String> forwardedIps = forwardedIps(request);
    if (forwardedIps.size() < trustedProxyCount) {
      return null;
    }

    String candidate = forwardedIps.get(forwardedIps.size() - trustedProxyCount);
    return isValidIpAddress(candidate) ? candidate : null;
  }

  private static List<String> forwardedIps(HttpServletRequest request) {
    Enumeration<String> headers = request.getHeaders(X_FORWARDED_FOR);
    if (headers == null) {
      headers = Collections.emptyEnumeration();
    }

    List<String> forwardedIps = new ArrayList<>();
    while (headers.hasMoreElements()) {
      String header = headers.nextElement();
      if (!StringUtils.hasText(header)) {
        continue;
      }
      for (String value : header.split(",")) {
        String candidate = value.trim();
        if (StringUtils.hasText(candidate)) {
          forwardedIps.add(candidate);
        }
      }
    }
    return forwardedIps;
  }

  private static boolean isValidIpAddress(String value) {
    return isValidIpv4Address(value) || isValidIpv6Address(value);
  }

  private static boolean isValidIpv4Address(String value) {
    if (!StringUtils.hasText(value)) {
      return false;
    }

    String[] octets = value.split("\\.", -1);
    if (octets.length != 4) {
      return false;
    }

    for (String octet : octets) {
      if (octet.isEmpty() || octet.length() > 3) {
        return false;
      }
      for (int i = 0; i < octet.length(); i++) {
        if (!Character.isDigit(octet.charAt(i))) {
          return false;
        }
      }
      int parsed = Integer.parseInt(octet);
      if (parsed > 255) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidIpv6Address(String value) {
    if (!StringUtils.hasText(value) || !value.contains(":")) {
      return false;
    }
    if (!value.matches("[0-9A-Fa-f:.]+")) {
      return false;
    }

    try {
      return InetAddress.getByName(value) instanceof Inet6Address;
    } catch (UnknownHostException e) {
      return false;
    }
  }
}
