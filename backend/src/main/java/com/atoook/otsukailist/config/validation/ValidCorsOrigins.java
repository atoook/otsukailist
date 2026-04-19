package com.atoook.otsukailist.config.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CorsOriginsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCorsOrigins {

  /**
   * Default validation error message.
   *
   * @return the validation message template
   */
  String message() default
      "app.cors.allowed-origins must be valid origins and may use '*' only as the single entry";

  /**
   * Validation groups for constraint targeting.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload classes for clients of the Bean Validation API.
   *
   * @return payload metadata
   */
  Class<? extends Payload>[] payload() default {};
}
