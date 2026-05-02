package com.atoook.otsukailist.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Origin {
  MANUAL("manual"),
  GENERATED("generated");

  private final String value;

  Origin(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static Origin fromValue(String value) {
    for (Origin origin : values()) {
      if (origin.value.equals(value)) {
        return origin;
      }
    }
    throw new IllegalArgumentException("Unknown origin: " + value);
  }
}
