package com.atoook.otsukailist.generation;

import java.util.Map;

import com.atoook.otsukailist.model.BaseUnit;
import com.atoook.otsukailist.model.ItemCategory;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BBQGenerationRules {
  public static final Map<String, BBQGenerationRule> VALUES =
      Map.ofEntries(
          Map.entry("beef", new BBQGenerationRule("beef", ItemCategory.MEAT, BaseUnit.G, "g")),
          Map.entry("pork", new BBQGenerationRule("pork", ItemCategory.MEAT, BaseUnit.G, "g")),
          Map.entry(
              "chicken", new BBQGenerationRule("chicken", ItemCategory.MEAT, BaseUnit.G, "g")),
          Map.entry(
              "sausage", new BBQGenerationRule("sausage", ItemCategory.MEAT, BaseUnit.G, "g")),
          Map.entry(
              "vegetable_onion",
              new BBQGenerationRule(
                  "vegetable_onion", ItemCategory.VEGETABLES, BaseUnit.PIECE, "piece")),
          Map.entry(
              "vegetable_bell_pepper",
              new BBQGenerationRule(
                  "vegetable_bell_pepper", ItemCategory.VEGETABLES, BaseUnit.PIECE, "piece")),
          Map.entry(
              "vegetable_corn",
              new BBQGenerationRule(
                  "vegetable_corn", ItemCategory.VEGETABLES, BaseUnit.PIECE, "piece")),
          Map.entry(
              "vegetable_potato",
              new BBQGenerationRule(
                  "vegetable_potato", ItemCategory.VEGETABLES, BaseUnit.PIECE, "piece")),
          Map.entry(
              "vegetable_mushrooms",
              new BBQGenerationRule(
                  "vegetable_mushrooms", ItemCategory.VEGETABLES, BaseUnit.PACK, "pack")),
          Map.entry(
              "seafood_shrimp",
              new BBQGenerationRule("seafood_shrimp", ItemCategory.SEAFOOD, BaseUnit.PACK, "pack")),
          Map.entry(
              "seafood_scallop",
              new BBQGenerationRule(
                  "seafood_scallop", ItemCategory.SEAFOOD, BaseUnit.PACK, "pack")),
          Map.entry(
              "seafood_squid",
              new BBQGenerationRule("seafood_squid", ItemCategory.SEAFOOD, BaseUnit.PACK, "pack")),
          Map.entry(
              "soft_drinks",
              new BBQGenerationRule("soft_drinks", ItemCategory.DRINKS, BaseUnit.ML, "l")),
          Map.entry(
              "alcohol",
              new BBQGenerationRule("alcohol", ItemCategory.DRINKS, BaseUnit.PIECE, "piece")),
          Map.entry(
              "yakisoba",
              new BBQGenerationRule("yakisoba", ItemCategory.STAPLE, BaseUnit.PIECE, "piece")));

  public record BBQGenerationRule(
      String generatorKey, ItemCategory category, BaseUnit baseUnit, String displayUnit)
      implements GenerationRule {}
}
