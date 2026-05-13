package com.atoook.otsukailist.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BaseUnit {
  G("g"),
  ML("ml"),
  PIECE("piece"),
  PACK("pack");

  private final String value;

  BaseUnit(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static BaseUnit fromValue(String value) {
    for (BaseUnit baseUnit : values()) {
      if (baseUnit.value.equals(value)) {
        return baseUnit;
      }
    }
    throw new IllegalArgumentException("Unknown base unit: " + value);
  }
}
