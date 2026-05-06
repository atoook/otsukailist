package com.atoook.otsukailist.dto;

import com.atoook.otsukailist.model.BaseUnit;
import com.atoook.otsukailist.model.Origin;
import com.atoook.otsukailist.model.RegenerationPolicy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuantifiedItemResponse {
  private long quantity;

  private BaseUnit baseUnit;

  private Origin origin;

  private RegenerationPolicy regenerationPolicy;

  private String generatorKey;
}
