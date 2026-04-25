package com.atoook.otsukailist.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.atoook.otsukailist.config.AppCorsProperties;

@Configuration
public class WebConfig {

  private final AppCorsProperties corsProperties;

  public WebConfig(AppCorsProperties corsProperties) {
    this.corsProperties = corsProperties;
  }

  @Bean
  public WebMvcConfigurer webMvcConfigurer() {

    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*");
      }
    };
  }
}
