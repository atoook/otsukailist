package com.atoook.otsukailist.api.ratelimit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.atoook.otsukailist.api.dto.ApiError;
import com.atoook.otsukailist.api.dto.ApiErrorCode;
import com.atoook.otsukailist.config.AppCorsProperties;
import com.atoook.otsukailist.config.AppRateLimitProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {
  private static final String RATE_LIMIT_MESSAGE = "リクエストが多すぎます。しばらく待ってから再試行してください。";
  private static final String RATE_LIMIT_LIMIT_HEADER = "RateLimit-Limit";
  private static final String RATE_LIMIT_REMAINING_HEADER = "RateLimit-Remaining";
  private static final String RATE_LIMIT_RESET_HEADER = "RateLimit-Reset";
  private static final List<String> EXPOSED_RATE_LIMIT_HEADERS =
      List.of(
          HttpHeaders.RETRY_AFTER,
          RATE_LIMIT_LIMIT_HEADER,
          RATE_LIMIT_REMAINING_HEADER,
          RATE_LIMIT_RESET_HEADER);

  private final AppRateLimitProperties rateLimitProperties;
  private final AppCorsProperties corsProperties;
  private final ClientIpResolver clientIpResolver;
  private final ObjectMapper objectMapper;
  private final Cache<String, ClientBuckets> buckets;

  /**
   * Creates the API rate-limit filter.
   *
   * @param rateLimitProperties rate-limit settings
   * @param corsProperties CORS settings used for rejected responses
   * @param clientIpResolver client IP resolver
   * @param objectMapper JSON mapper
   */
  public RateLimitFilter(
      AppRateLimitProperties rateLimitProperties,
      AppCorsProperties corsProperties,
      ClientIpResolver clientIpResolver,
      ObjectMapper objectMapper) {
    this.rateLimitProperties = rateLimitProperties;
    this.corsProperties = corsProperties;
    this.clientIpResolver = clientIpResolver;
    this.objectMapper = objectMapper;
    this.buckets =
        Caffeine.newBuilder()
            .expireAfterAccess(rateLimitProperties.getCacheTtl())
            .maximumSize(rateLimitProperties.getMaxClients())
            .build(clientIp -> createClientBuckets());
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !rateLimitProperties.isEnabled()
        || HttpMethod.OPTIONS.matches(request.getMethod())
        || !isApiRequest(request);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    RateLimitPolicy policy = RateLimitPolicy.from(request);
    ClientBuckets clientBuckets =
        buckets.get(clientIpResolver.resolveClientIp(request), clientIp -> createClientBuckets());
    Bucket bucket = clientBuckets.bucket(policy);
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      writeRateLimitHeaders(
          response,
          policy.capacity(rateLimitProperties),
          probe.getRemainingTokens(),
          policy.resetSeconds(rateLimitProperties));
      filterChain.doFilter(request, response);
      return;
    }

    long retryAfterSeconds = retryAfterSeconds(probe);
    writeRateLimitHeaders(
        response,
        policy.capacity(rateLimitProperties),
        probe.getRemainingTokens(),
        retryAfterSeconds);
    response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
    writeCorsHeadersForRejectedRequest(request, response);
    writeRateLimitError(response, retryAfterSeconds);
  }

  private ClientBuckets createClientBuckets() {
    return new ClientBuckets(
        createBucket(
            rateLimitProperties.getCapacity(),
            rateLimitProperties.getRefillTokens(),
            rateLimitProperties.getRefillPeriod()),
        createBucket(
            rateLimitProperties.getMutationCapacity(),
            rateLimitProperties.getMutationRefillTokens(),
            rateLimitProperties.getMutationRefillPeriod()));
  }

  private static Bucket createBucket(long capacity, long refillTokens, Duration refillPeriod) {
    return Bucket.builder()
        .addLimit(limit -> limit.capacity(capacity).refillGreedy(refillTokens, refillPeriod))
        .build();
  }

  private boolean isApiRequest(HttpServletRequest request) {
    String path = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
      path = path.substring(contextPath.length());
    }
    return "/api".equals(path) || path.startsWith("/api/") || path.startsWith("/api;");
  }

  private static void writeRateLimitHeaders(
      HttpServletResponse response, long capacity, long remainingTokens, long resetSeconds) {
    response.setHeader(RATE_LIMIT_LIMIT_HEADER, Long.toString(capacity));
    response.setHeader(RATE_LIMIT_REMAINING_HEADER, Long.toString(Math.max(0L, remainingTokens)));
    response.setHeader(RATE_LIMIT_RESET_HEADER, Long.toString(resetSeconds));
  }

  private static long retryAfterSeconds(ConsumptionProbe probe) {
    long nanosToWait = probe.getNanosToWaitForRefill();
    long secondsToWait = (nanosToWait + 999_999_999L) / 1_000_000_000L;
    return Math.max(1L, secondsToWait);
  }

  private void writeCorsHeadersForRejectedRequest(
      HttpServletRequest request, HttpServletResponse response) {
    String origin = request.getHeader(HttpHeaders.ORIGIN);
    if (!StringUtils.hasText(origin)) {
      return;
    }

    List<String> allowedOrigins = corsProperties.getAllowedOrigins();
    if (allowedOrigins.contains("*")) {
      response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    } else if (allowedOrigins.contains(origin)) {
      response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      response.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
    }
    response.setHeader(
        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, String.join(", ", EXPOSED_RATE_LIMIT_HEADERS));
  }

  private void writeRateLimitError(HttpServletResponse response, long retryAfterSeconds)
      throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ApiError error =
        ApiError.of(
            ApiErrorCode.RATE_LIMITED,
            RATE_LIMIT_MESSAGE,
            Map.of("retryAfterSeconds", retryAfterSeconds));
    objectMapper.writeValue(response.getWriter(), error);
  }

  private record ClientBuckets(Bucket apiBucket, Bucket mutationBucket) {
    Bucket bucket(RateLimitPolicy policy) {
      return switch (policy) {
        case API -> apiBucket;
        case MUTATION -> mutationBucket;
      };
    }
  }

  private enum RateLimitPolicy {
    API,
    MUTATION;

    static RateLimitPolicy from(HttpServletRequest request) {
      if (HttpMethod.POST.matches(request.getMethod())
          || HttpMethod.PUT.matches(request.getMethod())
          || HttpMethod.PATCH.matches(request.getMethod())
          || HttpMethod.DELETE.matches(request.getMethod())) {
        return MUTATION;
      }
      return API;
    }

    long capacity(AppRateLimitProperties properties) {
      return switch (this) {
        case API -> properties.getCapacity();
        case MUTATION -> properties.getMutationCapacity();
      };
    }

    long resetSeconds(AppRateLimitProperties properties) {
      Duration refillPeriod =
          switch (this) {
            case API -> properties.getRefillPeriod();
            case MUTATION -> properties.getMutationRefillPeriod();
          };
      return Math.max(1L, refillPeriod.toSeconds());
    }
  }
}
