package com.atoook.otsukailist.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public class AppRateLimitProperties {

  /** Enable API rate limiting. */
  private boolean enabled = true;

  /** Maximum burst size per client for non-mutation API requests. */
  @Min(1)
  private long capacity = 120L;

  /** Number of non-mutation tokens refilled each period. */
  @Min(1)
  private long refillTokens = 120L;

  /** Maximum burst size per client for mutation requests. */
  @Min(1)
  private long mutationCapacity = 30L;

  /** Number of mutation tokens refilled each period. */
  @Min(1)
  private long mutationRefillTokens = 30L;

  /** Duration of one mutation refill period. */
  @NotNull private Duration mutationRefillPeriod = Duration.ofMinutes(1);

  /** Duration of one non-mutation refill period. */
  @NotNull private Duration refillPeriod = Duration.ofMinutes(1);

  /** How long inactive client buckets stay in memory. */
  @NotNull private Duration cacheTtl = Duration.ofMinutes(15);

  /** Maximum number of client buckets retained in memory. */
  @Min(1)
  private long maxClients = 10_000L;

  /** Number of trusted reverse proxies that append X-Forwarded-For values. */
  @Min(0)
  private int trustedProxyCount = 0;

  /** Trusted proxy IPs or CIDR ranges allowed to supply X-Forwarded-For. */
  private List<String> trustedProxyCidrs = new ArrayList<>();

  /**
   * Validate that refill period is positive.
   *
   * @return true when refill period is positive
   */
  @AssertTrue(message = "refill-period must be positive")
  public boolean isRefillPeriodPositive() {
    return isPositive(refillPeriod);
  }

  /**
   * Validate that mutation refill period is positive.
   *
   * @return true when mutation refill period is positive
   */
  @AssertTrue(message = "mutation-refill-period must be positive")
  public boolean isMutationRefillPeriodPositive() {
    return isPositive(mutationRefillPeriod);
  }

  /**
   * Validate that cache TTL is positive.
   *
   * @return true when cache TTL is positive
   */
  @AssertTrue(message = "cache-ttl must be positive")
  public boolean isCacheTtlPositive() {
    return isPositive(cacheTtl);
  }

  /**
   * Validate that X-Forwarded-For is only enabled with an explicit proxy allowlist.
   *
   * @return true when trusted proxy settings are safe
   */
  @AssertTrue(message = "trusted-proxy-cidrs must be set when trusted-proxy-count is positive")
  public boolean isTrustedProxyConfigurationValid() {
    return trustedProxyCount <= 0
        || trustedProxyCidrs != null
            && trustedProxyCidrs.stream().anyMatch(value -> !value.isBlank());
  }

  private static boolean isPositive(Duration duration) {
    return duration != null && !duration.isZero() && !duration.isNegative();
  }
}
