package com.atoook.otsukailist.config;

import jakarta.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.item")
public class AppItemProperties {

  /** Maximum number of items that can belong to one list. */
  @Min(1)
  private int maxItemsPerList = 100;
}
