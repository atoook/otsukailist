package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.UUID;

import com.atoook.otsukailist.model.ItemCategory;
import com.atoook.otsukailist.model.ItemPreparationType;
import com.atoook.otsukailist.model.ItemType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Item のレスポンス用DTO */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

  private UUID id;

  private String name;

  private long version;

  private ItemType itemType;

  private ItemCategory category;

  private ItemPreparationType preparationType;

  private QuantifiedItemResponse quantified;

  private boolean completed;

  private UUID assignedMemberId;

  private UUID completedByMemberId;

  private Instant completedAt;

  private Instant createdAt;

  private Instant updatedAt;
}
