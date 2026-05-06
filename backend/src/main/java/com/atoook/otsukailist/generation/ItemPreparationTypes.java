package com.atoook.otsukailist.generation;

import java.util.Map;

import com.atoook.otsukailist.model.ItemPreparationType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemPreparationTypes {
  public static final Map<ItemPreparationType, ItemPreparationTypeDefinition> VALUES =
      Map.ofEntries(
          Map.entry(
              ItemPreparationType.BUY,
              new ItemPreparationTypeDefinition(ItemPreparationType.BUY, "購入", 10)),
          Map.entry(
              ItemPreparationType.BRING,
              new ItemPreparationTypeDefinition(ItemPreparationType.BRING, "持参", 20)));

  public record ItemPreparationTypeDefinition(
      ItemPreparationType code, String label, int sortOrder) {}
}
