package com.atoook.otsukailist.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemType {
  PLAIN("plain"),
  QUANTIFIED("quantified");

  private final String value;

  ItemType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ItemType fromValue(String value) {
    for (ItemType itemType : values()) {
      if (itemType.value.equals(value)) {
        return itemType;
      }
    }
    throw new IllegalArgumentException("Unknown item type: " + value);
  }
}
