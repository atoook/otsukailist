package com.atoook.otsukailist.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppMemberPropertiesValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void shouldAcceptDefaultProperties() {
    AppMemberProperties properties = new AppMemberProperties();

    Set<ConstraintViolation<AppMemberProperties>> violations = validator.validate(properties);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectNonPositiveMaxMembersPerList() {
    AppMemberProperties properties = new AppMemberProperties();
    properties.setMaxMembersPerList(0);

    Set<ConstraintViolation<AppMemberProperties>> violations = validator.validate(properties);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("maxMembersPerList");
  }

  @Test
  void shouldRejectMaxMembersPerListAboveAbsoluteLimit() {
    AppMemberProperties properties = new AppMemberProperties();
    properties.setMaxMembersPerList(AppMemberProperties.ABSOLUTE_MAX_MEMBERS_PER_LIST + 1);

    Set<ConstraintViolation<AppMemberProperties>> violations = validator.validate(properties);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("maxMembersPerList");
  }
}
