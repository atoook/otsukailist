package com.atoook.otsukailist.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.atoook.otsukailist.model.BaseUnit;

@Converter(autoApply = true)
public class BaseUnitConverter implements AttributeConverter<BaseUnit, String> {

  @Override
  public String convertToDatabaseColumn(BaseUnit attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public BaseUnit convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    for (BaseUnit baseUnit : BaseUnit.values()) {
      if (baseUnit.getValue().equals(dbData)) {
        return baseUnit;
      }
    }
    throw new IllegalArgumentException("Unknown base unit: " + dbData);
  }
}
