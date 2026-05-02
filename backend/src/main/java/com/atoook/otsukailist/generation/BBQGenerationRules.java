package com.atoook.otsukailist.generation;

import java.util.Map;

import com.atoook.otsukailist.model.BaseUnit;
import com.atoook.otsukailist.model.ItemCategory;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BBQGenerationRules {
  public static final Map<String, BBQGenerationRule> VALUES =
      Map.of(
          "beef", new BBQGenerationRule("beef", ItemCategory.MEAT, BaseUnit.G, "kg"),
          "pork", new BBQGenerationRule("pork", ItemCategory.MEAT, BaseUnit.G, "kg"),
          "vegetables",
              new BBQGenerationRule("vegetables", ItemCategory.VEGETABLES, BaseUnit.G, "g"),
          "yakisoba",
              new BBQGenerationRule("yakisoba", ItemCategory.STAPLE, BaseUnit.PIECE, "piece"));

  public record BBQGenerationRule(
      String generatorKey, ItemCategory category, BaseUnit baseUnit, String displayUnit) {}
}
