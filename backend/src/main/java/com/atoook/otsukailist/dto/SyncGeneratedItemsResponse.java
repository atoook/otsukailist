package com.atoook.otsukailist.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SyncGeneratedItemsResponse {
  private List<ItemResponse> items;
  private List<UUID> deletedItemIds;
}
