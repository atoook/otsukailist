package com.atoook.otsukailist.api.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.springframework.mock.web.MockHttpServletRequest;

import com.atoook.otsukailist.config.AppRateLimitProperties;

import org.junit.jupiter.api.Test;

class ClientIpResolverTest {

  @Test
  void shouldUseRemoteAddrWhenForwardedHeaderIsMissing() {
    ClientIpResolver resolver = newResolver();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("198.51.100.10");

    assertThat(resolver.resolveClientIp(request)).isEqualTo("198.51.100.10");
  }

  @Test
  void shouldFallbackToRemoteAddrWhenForwardedHeaderIsPresentButProxyTrustIsDisabled() {
    ClientIpResolver resolver = newResolver();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("10.0.0.1");
    request.addHeader("X-Forwarded-For", "203.0.113.20");

    assertThat(resolver.resolveClientIp(request)).isEqualTo("10.0.0.1");
  }

  @Test
  void shouldUseRightmostForwardedIpForSingleTrustedProxy() {
    AppRateLimitProperties properties = trustedProxyProperties("10.0.0.1/32");
    ClientIpResolver resolver = new ClientIpResolver(properties);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("10.0.0.1");
    request.addHeader("X-Forwarded-For", "203.0.113.20");

    assertThat(resolver.resolveClientIp(request)).isEqualTo("203.0.113.20");
  }

  @Test
  void shouldIgnoreSpoofedForwardedPrefixForSingleTrustedProxy() {
    AppRateLimitProperties properties = trustedProxyProperties("10.0.0.0/24");
    ClientIpResolver resolver = new ClientIpResolver(properties);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("10.0.0.1");
    request.addHeader("X-Forwarded-For", "192.0.2.1, 203.0.113.20");

    assertThat(resolver.resolveClientIp(request)).isEqualTo("203.0.113.20");
  }

  @Test
  void shouldFallbackToRemoteAddrWhenProxyIsNotTrusted() {
    AppRateLimitProperties properties = trustedProxyProperties("10.0.0.0/24");
    ClientIpResolver resolver = new ClientIpResolver(properties);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("198.51.100.10");
    request.addHeader("X-Forwarded-For", "203.0.113.20");

    assertThat(resolver.resolveClientIp(request)).isEqualTo("198.51.100.10");
  }

  @Test
  void shouldFallbackToRemoteAddrWhenSelectedForwardedIpIsInvalid() {
    ClientIpResolver resolver = newResolver();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("198.51.100.10");
    request.addHeader("X-Forwarded-For", "not-an-ip");

    assertThat(resolver.resolveClientIp(request)).isEqualTo("198.51.100.10");
  }

  private static ClientIpResolver newResolver() {
    return new ClientIpResolver(new AppRateLimitProperties());
  }

  private static AppRateLimitProperties trustedProxyProperties(String trustedProxyCidr) {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setTrustedProxyCount(1);
    properties.setTrustedProxyCidrs(List.of(trustedProxyCidr));
    return properties;
  }
}
