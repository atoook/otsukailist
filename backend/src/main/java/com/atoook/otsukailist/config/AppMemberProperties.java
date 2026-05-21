package com.atoook.otsukailist.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.member")
public class AppMemberProperties {
  public static final int ABSOLUTE_MAX_MEMBERS_PER_LIST = 1000;

  /** Maximum number of members that can belong to one list. */
  @Min(1)
  @Max(ABSOLUTE_MAX_MEMBERS_PER_LIST)
  private int maxMembersPerList = 20;
}
