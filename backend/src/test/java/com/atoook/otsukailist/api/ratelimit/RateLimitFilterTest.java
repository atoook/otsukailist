package com.atoook.otsukailist.api.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import jakarta.servlet.FilterChain;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.atoook.otsukailist.config.AppCorsProperties;
import com.atoook.otsukailist.config.AppRateLimitProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RateLimitFilterTest {

  @Test
  void shouldRejectRequestAfterDefaultLimitForSameIp() throws Exception {
    RateLimitFilter filter = newFilter(new AppRateLimitProperties());

    for (int i = 0; i < 120; i++) {
      MockHttpServletResponse response = perform(filter, "/api/lists", "198.51.100.10", "GET");

      assertThat(response.getStatus()).isEqualTo(200);
    }

    MockHttpServletResponse rejected = perform(filter, "/api/lists", "198.51.100.10", "GET");

    assertThat(rejected.getStatus()).isEqualTo(429);
    assertThat(rejected.getHeader("Retry-After")).isNotBlank();
    assertThat(rejected.getHeader("RateLimit-Limit")).isEqualTo("120");
    assertThat(rejected.getHeader("RateLimit-Remaining")).isEqualTo("0");
    assertThat(rejected.getContentAsString(StandardCharsets.UTF_8)).contains("rate_limited");
    assertThat(rejected.getContentAsString(StandardCharsets.UTF_8)).contains("retryAfterSeconds");
  }

  @Test
  void shouldRejectMutationRequestAfterDefaultMutationLimitForSameIp() throws Exception {
    RateLimitFilter filter = newFilter(new AppRateLimitProperties());

    for (int i = 0; i < 30; i++) {
      MockHttpServletResponse response = perform(filter, "/api/lists", "198.51.100.10", "POST");

      assertThat(response.getStatus()).isEqualTo(200);
    }

    MockHttpServletResponse rejected = perform(filter, "/api/lists", "198.51.100.10", "POST");

    assertThat(rejected.getStatus()).isEqualTo(429);
    assertThat(rejected.getHeader("RateLimit-Limit")).isEqualTo("30");
    assertThat(rejected.getHeader("RateLimit-Remaining")).isEqualTo("0");
    assertThat(rejected.getContentAsString(StandardCharsets.UTF_8)).contains("rate_limited");
  }

  @Test
  void shouldKeepReadAndMutationBucketsSeparate() throws Exception {
    AppRateLimitProperties properties = oneMutationRequestLimitProperties();
    RateLimitFilter filter = newFilter(properties);

    assertThat(perform(filter, "/api/lists", "198.51.100.10", "POST").getStatus()).isEqualTo(200);
    assertThat(perform(filter, "/api/lists", "198.51.100.10", "POST").getStatus()).isEqualTo(429);
    assertThat(perform(filter, "/api/lists", "198.51.100.10", "GET").getStatus()).isEqualTo(200);
  }

  @Test
  void shouldLimitEachIpIndependently() throws Exception {
    AppRateLimitProperties properties = oneRequestLimitProperties();
    RateLimitFilter filter = newFilter(properties);

    assertThat(perform(filter, "/api/lists", "198.51.100.10", "GET").getStatus()).isEqualTo(200);
    assertThat(perform(filter, "/api/lists", "198.51.100.10", "GET").getStatus()).isEqualTo(429);
    assertThat(perform(filter, "/api/lists", "198.51.100.11", "GET").getStatus()).isEqualTo(200);
  }

  @Test
  void shouldSkipActuatorAndOptionsRequests() throws Exception {
    AppRateLimitProperties properties = oneRequestLimitProperties();
    RateLimitFilter filter = newFilter(properties);

    assertThat(perform(filter, "/actuator/health/liveness", "198.51.100.10", "GET").getStatus())
        .isEqualTo(200);
    assertThat(perform(filter, "/actuator/health/liveness", "198.51.100.10", "GET").getStatus())
        .isEqualTo(200);
    assertThat(perform(filter, "/api/lists", "198.51.100.10", "OPTIONS").getStatus())
        .isEqualTo(200);
    assertThat(perform(filter, "/api/lists", "198.51.100.10", "OPTIONS").getStatus())
        .isEqualTo(200);
  }

  @Test
  void shouldLimitApiRequestsWithPathParameters() throws Exception {
    AppRateLimitProperties properties = oneRequestLimitProperties();
    RateLimitFilter filter = newFilter(properties);

    assertThat(perform(filter, "/api;v=1/lists", "198.51.100.10", "GET").getStatus())
        .isEqualTo(200);
    assertThat(perform(filter, "/api;v=1/lists", "198.51.100.10", "GET").getStatus())
        .isEqualTo(429);
  }

  @Test
  void shouldPassThroughWhenDisabled() throws Exception {
    AppRateLimitProperties properties = oneRequestLimitProperties();
    properties.setEnabled(false);
    RateLimitFilter filter = newFilter(properties);

    assertThat(perform(filter, "/api/lists", "198.51.100.10", "GET").getStatus()).isEqualTo(200);
    assertThat(perform(filter, "/api/lists", "198.51.100.10", "GET").getStatus()).isEqualTo(200);
  }

  private static MockHttpServletResponse perform(
      RateLimitFilter filter, String requestUri, String remoteAddr, String method)
      throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(method, requestUri);
    request.setRemoteAddr(remoteAddr);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = (servletRequest, servletResponse) -> servletResponse.flushBuffer();

    filter.doFilter(request, response, chain);

    return response;
  }

  private static RateLimitFilter newFilter(AppRateLimitProperties properties) {
    AppCorsProperties corsProperties = new AppCorsProperties();
    corsProperties.setAllowedOrigins(List.of("http://localhost:5173"));
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    return new RateLimitFilter(
        properties, corsProperties, new ClientIpResolver(properties), objectMapper);
  }

  private static AppRateLimitProperties oneRequestLimitProperties() {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setCapacity(1L);
    properties.setRefillTokens(1L);
    properties.setRefillPeriod(Duration.ofHours(1));
    return properties;
  }

  private static AppRateLimitProperties oneMutationRequestLimitProperties() {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setMutationCapacity(1L);
    properties.setMutationRefillTokens(1L);
    properties.setMutationRefillPeriod(Duration.ofHours(1));
    return properties;
  }
}
