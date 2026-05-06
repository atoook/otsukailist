package com.atoook.otsukailist.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.atoook.otsukailist.model.RegenerationPolicy;

@Converter(autoApply = true)
public class RegenerationPolicyConverter implements AttributeConverter<RegenerationPolicy, String> {

  @Override
  public String convertToDatabaseColumn(RegenerationPolicy attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public RegenerationPolicy convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    for (RegenerationPolicy policy : RegenerationPolicy.values()) {
      if (policy.getValue().equals(dbData)) {
        return policy;
      }
    }
    throw new IllegalArgumentException("Unknown regeneration policy: " + dbData);
  }
}
