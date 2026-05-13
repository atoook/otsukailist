package com.atoook.otsukailist.generation;

import java.util.Map;

import com.atoook.otsukailist.model.BaseUnit;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UnitDefinitions {
  public static final Map<String, UnitDefinition> VALUES =
      Map.of(
          "g", new UnitDefinition("g", BaseUnit.G, 1),
          "kg", new UnitDefinition("kg", BaseUnit.G, 1000),
          "ml", new UnitDefinition("ml", BaseUnit.ML, 1),
          "l", new UnitDefinition("l", BaseUnit.ML, 1000),
          "piece", new UnitDefinition("piece", BaseUnit.PIECE, 1),
          "pack", new UnitDefinition("pack", BaseUnit.PACK, 1));

  public record UnitDefinition(String code, BaseUnit baseUnit, long factor) {}
}
