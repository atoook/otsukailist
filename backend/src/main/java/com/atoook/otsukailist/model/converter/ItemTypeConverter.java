package com.atoook.otsukailist.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.atoook.otsukailist.model.ItemType;

@Converter(autoApply = true)
public class ItemTypeConverter implements AttributeConverter<ItemType, String> {

  @Override
  public String convertToDatabaseColumn(ItemType attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public ItemType convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    for (ItemType itemType : ItemType.values()) {
      if (itemType.getValue().equals(dbData)) {
        return itemType;
      }
    }
    throw new IllegalArgumentException("Unknown item type: " + dbData);
  }
}
