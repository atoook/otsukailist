package com.atoook.otsukailist.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemPreparationType {
  BUY("buy"),
  BRING("bring");

  private final String value;

  ItemPreparationType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ItemPreparationType fromValue(String value) {
    for (ItemPreparationType preparationType : values()) {
      if (preparationType.value.equals(value)) {
        return preparationType;
      }
    }
    throw new IllegalArgumentException("Unknown item preparation type: " + value);
  }
}
