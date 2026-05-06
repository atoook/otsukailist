package com.atoook.otsukailist.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.atoook.otsukailist.model.ItemCategory;

@Converter(autoApply = true)
public class ItemCategoryConverter implements AttributeConverter<ItemCategory, String> {

  @Override
  public String convertToDatabaseColumn(ItemCategory attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public ItemCategory convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    for (ItemCategory category : ItemCategory.values()) {
      if (category.getValue().equals(dbData)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown item category: " + dbData);
  }
}
