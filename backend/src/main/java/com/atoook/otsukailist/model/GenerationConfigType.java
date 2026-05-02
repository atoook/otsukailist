package com.atoook.otsukailist.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GenerationConfigType {
  BBQ("bbq"),
  CAMPING("camping"),
  HOTPOT("hotpot"),
  TRAVEL("travel");

  private final String value;

  GenerationConfigType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static GenerationConfigType fromValue(String value) {
    for (GenerationConfigType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown generation config type: " + value);
  }
}
