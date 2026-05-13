package com.atoook.otsukailist.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.atoook.otsukailist.model.ItemPreparationType;

@Converter(autoApply = true)
public class ItemPreparationTypeConverter
    implements AttributeConverter<ItemPreparationType, String> {

  @Override
  public String convertToDatabaseColumn(ItemPreparationType attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public ItemPreparationType convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    for (ItemPreparationType preparationType : ItemPreparationType.values()) {
      if (preparationType.getValue().equals(dbData)) {
        return preparationType;
      }
    }
    throw new IllegalArgumentException("Unknown item preparation type: " + dbData);
  }
}
