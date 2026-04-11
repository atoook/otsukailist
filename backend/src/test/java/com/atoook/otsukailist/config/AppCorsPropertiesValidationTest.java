package com.atoook.otsukailist.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AppCorsPropertiesValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectEmptyAllowedOrigins() {
        AppCorsProperties properties = new AppCorsProperties();
        properties.setAllowedOrigins(new ArrayList<>());

        Set<ConstraintViolation<AppCorsProperties>> violations = validator.validate(properties);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("must not be empty"));
    }

    @Test
    void shouldAcceptSingleWildcard() {
        AppCorsProperties properties = new AppCorsProperties();
        properties.setAllowedOrigins(List.of("*"));

        Set<ConstraintViolation<AppCorsProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectWildcardMixedWithSpecificOrigins() {
        AppCorsProperties properties = new AppCorsProperties();
        properties.setAllowedOrigins(List.of("*", "https://example.com"));

        Set<ConstraintViolation<AppCorsProperties>> violations = validator.validate(properties);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("single entry"));
    }

    @Test
    void shouldRejectInvalidOriginEntry() {
        AppCorsProperties properties = new AppCorsProperties();
        properties.setAllowedOrigins(List.of("not-a-url"));

        Set<ConstraintViolation<AppCorsProperties>> violations = validator.validate(properties);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .anyMatch(message -> message.contains("valid origins"));
    }

    @Test
    void shouldAcceptValidHttpAndHttpsOrigins() {
        AppCorsProperties properties = new AppCorsProperties();
        properties.setAllowedOrigins(List.of("https://example.com", "http://localhost:5173"));

        Set<ConstraintViolation<AppCorsProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
    }
}
