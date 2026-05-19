package com.atoook.otsukailist.api.ratelimit;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
    if (!isTrustedProxy(request.getRemoteAddr())) {
      return null;
    }

    List<String> forwardedIps = forwardedIps(request);
    if (forwardedIps.size() < trustedProxyCount) {
      return null;
    }

    String candidate = forwardedIps.get(forwardedIps.size() - trustedProxyCount);
    return isValidIpAddress(candidate) ? candidate : null;
  }

  private boolean isTrustedProxy(String remoteAddr) {
    InetAddress remoteAddress = parseIpAddress(remoteAddr);
    if (remoteAddress == null) {
      return false;
    }

    List<String> trustedProxyCidrs = properties.getTrustedProxyCidrs();
    if (trustedProxyCidrs == null) {
      return false;
    }

    return trustedProxyCidrs.stream()
        .filter(StringUtils::hasText)
        .anyMatch(entry -> trustedProxyMatcher(entry.trim()).matches(remoteAddress));
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

  private static InetAddress parseIpAddress(String value) {
    try {
      if (isValidIpv4Address(value)) {
        String[] octets = value.split("\\.");
        byte[] address = new byte[4];
        for (int i = 0; i < octets.length; i++) {
          address[i] = (byte) Integer.parseInt(octets[i]);
        }
        return InetAddress.getByAddress(address);
      }
      if (isValidIpv6Address(value)) {
        return InetAddress.getByName(value);
      }
      return null;
    } catch (UnknownHostException e) {
      return null;
    }
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

  private static TrustedProxyMatcher trustedProxyMatcher(String entry) {
    String[] parts = entry.split("/", -1);
    InetAddress baseAddress = parseIpAddress(parts[0]);
    if (baseAddress == null) {
      return TrustedProxyMatcher.NEVER;
    }
    if (parts.length == 1) {
      return remoteAddress -> baseAddress.equals(remoteAddress);
    }
    if (parts.length != 2) {
      return TrustedProxyMatcher.NEVER;
    }

    try {
      int prefixLength = Integer.parseInt(parts[1]);
      int maxPrefixLength = baseAddress.getAddress().length * Byte.SIZE;
      if (prefixLength < 0 || prefixLength > maxPrefixLength) {
        return TrustedProxyMatcher.NEVER;
      }
      return remoteAddress -> isInCidrRange(remoteAddress, baseAddress, prefixLength);
    } catch (NumberFormatException e) {
      return TrustedProxyMatcher.NEVER;
    }
  }

  private static boolean isInCidrRange(
      InetAddress remoteAddress, InetAddress baseAddress, int prefixLength) {
    byte[] remoteBytes = remoteAddress.getAddress();
    byte[] baseBytes = baseAddress.getAddress();
    if (remoteBytes.length != baseBytes.length) {
      return false;
    }

    int fullBytes = prefixLength / Byte.SIZE;
    int remainingBits = prefixLength % Byte.SIZE;
    if (!Arrays.equals(
        Arrays.copyOf(remoteBytes, fullBytes), Arrays.copyOf(baseBytes, fullBytes))) {
      return false;
    }
    if (remainingBits == 0) {
      return true;
    }

    int mask = 0xFF << (Byte.SIZE - remainingBits);
    return (remoteBytes[fullBytes] & mask) == (baseBytes[fullBytes] & mask);
  }

  @FunctionalInterface
  private interface TrustedProxyMatcher {
    TrustedProxyMatcher NEVER = remoteAddress -> false;

    boolean matches(InetAddress remoteAddress);
  }
}
