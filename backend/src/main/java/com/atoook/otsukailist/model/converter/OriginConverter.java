package com.atoook.otsukailist.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.atoook.otsukailist.model.Origin;

@Converter(autoApply = true)
public class OriginConverter implements AttributeConverter<Origin, String> {

  @Override
  public String convertToDatabaseColumn(Origin attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public Origin convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    for (Origin origin : Origin.values()) {
      if (origin.getValue().equals(dbData)) {
        return origin;
      }
    }
    throw new IllegalArgumentException("Unknown origin: " + dbData);
  }
}
