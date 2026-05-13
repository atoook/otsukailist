package com.atoook.otsukailist.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.atoook.otsukailist.model.GenerationConfigType;

@Converter(autoApply = true)
public class GenerationConfigTypeConverter
    implements AttributeConverter<GenerationConfigType, String> {

  @Override
  public String convertToDatabaseColumn(GenerationConfigType attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public GenerationConfigType convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    for (GenerationConfigType type : GenerationConfigType.values()) {
      if (type.getValue().equals(dbData)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown generation config type: " + dbData);
  }
}
