package com.atoook.otsukailist.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppItemPropertiesValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void shouldAcceptDefaultProperties() {
    AppItemProperties properties = new AppItemProperties();

    Set<ConstraintViolation<AppItemProperties>> violations = validator.validate(properties);

    assertThat(violations).isEmpty();
  }

  @Test
  void shouldRejectNonPositiveMaxItemsPerList() {
    AppItemProperties properties = new AppItemProperties();
    properties.setMaxItemsPerList(0);

    Set<ConstraintViolation<AppItemProperties>> violations = validator.validate(properties);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .contains("maxItemsPerList");
  }
}
