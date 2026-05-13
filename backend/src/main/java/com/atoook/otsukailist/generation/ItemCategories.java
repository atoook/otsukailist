package com.atoook.otsukailist.generation;

import java.util.Map;

import com.atoook.otsukailist.model.ItemCategory;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemCategories {
  public static final Map<ItemCategory, ItemCategoryDefinition> VALUES =
      Map.ofEntries(
          Map.entry(ItemCategory.FOOD, new ItemCategoryDefinition(ItemCategory.FOOD, "食品", 10)),
          Map.entry(ItemCategory.MEAT, new ItemCategoryDefinition(ItemCategory.MEAT, "肉", 20)),
          Map.entry(
              ItemCategory.VEGETABLES,
              new ItemCategoryDefinition(ItemCategory.VEGETABLES, "野菜", 30)),
          Map.entry(
              ItemCategory.SEAFOOD, new ItemCategoryDefinition(ItemCategory.SEAFOOD, "海鮮", 40)),
          Map.entry(ItemCategory.STAPLE, new ItemCategoryDefinition(ItemCategory.STAPLE, "主食", 50)),
          Map.entry(
              ItemCategory.DRINKS, new ItemCategoryDefinition(ItemCategory.DRINKS, "飲み物", 60)),
          Map.entry(
              ItemCategory.SEASONINGS,
              new ItemCategoryDefinition(ItemCategory.SEASONINGS, "調味料", 70)),
          Map.entry(
              ItemCategory.SWEETS, new ItemCategoryDefinition(ItemCategory.SWEETS, "お菓子", 80)),
          Map.entry(
              ItemCategory.DAILY_GOODS,
              new ItemCategoryDefinition(ItemCategory.DAILY_GOODS, "日用品", 90)),
          Map.entry(
              ItemCategory.SUPPLIES, new ItemCategoryDefinition(ItemCategory.SUPPLIES, "消耗品", 100)),
          Map.entry(
              ItemCategory.OTHER, new ItemCategoryDefinition(ItemCategory.OTHER, "その他", 999)));

  public record ItemCategoryDefinition(ItemCategory code, String label, int sortOrder) {}
}
