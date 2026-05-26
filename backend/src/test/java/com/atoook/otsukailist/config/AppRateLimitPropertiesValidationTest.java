package com.atoook.otsukailist.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppRateLimitPropertiesValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void shouldAcceptDefaultProperties() {
    AppRateLimitProperties properties = new AppRateLimitProperties();

    Set<ConstraintViolation<AppRateLimitProperties>> violations = validator.validate(properties);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectNonPositiveCapacityRefillTokensAndMaxClients() {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setCapacity(0L);
    properties.setRefillTokens(0L);
    properties.setMutationCapacity(0L);
    properties.setMutationRefillTokens(0L);
    properties.setMaxClients(0L);

    Set<ConstraintViolation<AppRateLimitProperties>> violations = validator.validate(properties);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains(
            "capacity", "refillTokens", "mutationCapacity", "mutationRefillTokens", "maxClients");
  }

  @Test
  void shouldRejectNegativeTrustedProxyCount() {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setTrustedProxyCount(-1);

    Set<ConstraintViolation<AppRateLimitProperties>> violations = validator.validate(properties);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("trustedProxyCount");
  }

  @Test
  void shouldRejectTrustedProxyCountWithoutTrustedProxyCidrs() {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setTrustedProxyCount(1);

    Set<ConstraintViolation<AppRateLimitProperties>> violations = validator.validate(properties);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("trustedProxyConfigurationValid");
  }

  @Test
  void shouldAcceptTrustedProxyCountWithTrustedProxyCidrs() {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setTrustedProxyCount(1);
    properties.setTrustedProxyCidrs(List.of("10.0.0.0/24"));

    Set<ConstraintViolation<AppRateLimitProperties>> violations = validator.validate(properties);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectNonPositiveDurations() {
    AppRateLimitProperties properties = new AppRateLimitProperties();
    properties.setRefillPeriod(Duration.ZERO);
    properties.setMutationRefillPeriod(Duration.ZERO);
    properties.setCacheTtl(Duration.ofSeconds(-1));

    Set<ConstraintViolation<AppRateLimitProperties>> violations = validator.validate(properties);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("refillPeriodPositive", "mutationRefillPeriodPositive", "cacheTtlPositive");
  }
}
