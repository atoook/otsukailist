package com.atoook.otsukailist.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RegenerationPolicy {
  NONE("none"),
  AUTO("auto"),
  LOCKED("locked");

  private final String value;

  RegenerationPolicy(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static RegenerationPolicy fromValue(String value) {
    for (RegenerationPolicy policy : values()) {
      if (policy.value.equals(value)) {
        return policy;
      }
    }
    throw new IllegalArgumentException("Unknown regeneration policy: " + value);
  }
}
