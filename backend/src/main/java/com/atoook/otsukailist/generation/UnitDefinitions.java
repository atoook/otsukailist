package com.atoook.otsukailist.generation;

import java.util.Map;

import com.atoook.otsukailist.model.BaseUnit;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UnitDefinitions {
  public static final Map<String, UnitDefinition> VALUES =
      Map.of(
          "g", new UnitDefinition("g", "g", BaseUnit.G, 1),
          "kg", new UnitDefinition("kg", "kg", BaseUnit.G, 1000),
          "ml", new UnitDefinition("ml", "ml", BaseUnit.ML, 1),
          "l", new UnitDefinition("l", "L", BaseUnit.ML, 1000),
          "piece", new UnitDefinition("piece", "個", BaseUnit.PIECE, 1),
          "pack", new UnitDefinition("pack", "袋", BaseUnit.PACK, 1));

  public record UnitDefinition(String code, String label, BaseUnit baseUnit, long factor) {}
}
