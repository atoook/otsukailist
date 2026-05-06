package com.atoook.otsukailist.generation;

import java.util.Map;

import com.atoook.otsukailist.model.ItemPreparationType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemPreparationTypes {
  public static final Map<ItemPreparationType, ItemPreparationTypeDefinition> VALUES =
      Map.ofEntries(
          Map.entry(
              ItemPreparationType.BRING,
              new ItemPreparationTypeDefinition(ItemPreparationType.BRING, "持参", 10)));

  public record ItemPreparationTypeDefinition(
      ItemPreparationType code, String label, int sortOrder) {}
}
