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
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCorsOrigins {

    String message() default "app.cors.allowed-origins must be valid origins and may use '*' only as the single entry";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
