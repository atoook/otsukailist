package com.atoook.otsukailist.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.atoook.otsukailist.config.validation.ValidCorsOrigins;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.cors")
public class AppCorsProperties {

  /**
   * Allowed CORS origins bound from app.cors.allowed-origins.
   *
   * <p>Validation policy:
   *
   * <ul>
   *   <li>The list must not be empty.
   *   <li>Each entry must be an origin (http/https scheme, no path/query/fragment).
   *   <li>The wildcard "*" is allowed only when it is the single entry.
   * </ul>
   */
  @NotEmpty @ValidCorsOrigins private List<String> allowedOrigins = new ArrayList<>();
}
