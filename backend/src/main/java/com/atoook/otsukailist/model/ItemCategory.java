package com.atoook.otsukailist.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {
  FOOD("food"),
  MEAT("meat"),
  VEGETABLES("vegetables"),
  SEAFOOD("seafood"),
  STAPLE("staple"),
  DRINKS("drinks"),
  SEASONINGS("seasonings"),
  DAILY_GOODS("daily_goods"),
  SUPPLIES("supplies"),
  SWEETS("sweets"),
  OTHER("other");

  private final String value;

  ItemCategory(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ItemCategory fromValue(String value) {
    for (ItemCategory category : values()) {
      if (category.value.equals(value)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown item category: " + value);
  }
}
