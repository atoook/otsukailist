package com.atoook.otsukailist.mapper;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.QuantifiedItemResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.ItemQuantified;
import com.atoook.otsukailist.model.ItemType;

import lombok.experimental.UtilityClass;

/** Item Entity ↔ DTO 変換用マッパー */
@UtilityClass
public class ItemMapper {

  /** Entity → Response DTO 変換 */
  public static ItemResponse toResponse(Item entity) {
    if (entity == null) {
      return null;
    }

    return ItemResponse.builder()
        .id(entity.getId())
        .name(entity.getName().trim())
        .itemType(entity.getItemType() == null ? ItemType.PLAIN : entity.getItemType())
        .category(entity.getCategory())
        .quantified(toQuantifiedResponse(entity.getQuantified()))
        .completed(entity.isCompleted())
        .assignedMemberId(entity.getAssignedMemberId())
        .completedByMemberId(entity.getCompletedByMemberId())
        .completedAt(entity.getCompletedAt())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  /** Request DTO → Entity 変換（新規作成時） */
  public static Item toEntity(CreateItemRequest request, ItemList itemList) {
    if (request == null) {
      return null;
    }

    Item entity = new Item();
    entity.setName(request.getName().trim());
    entity.setItemType(request.getItemType() == null ? ItemType.PLAIN : request.getItemType());
    entity.setCategory(request.getCategory());
    entity.setCompleted(request.isCompleted());
    entity.setItemList(itemList);

    return entity;
  }

  public static ItemQuantified toQuantifiedEntity(QuantifiedItemRequest request) {
    if (request == null) {
      return null;
    }

    ItemQuantified quantified = new ItemQuantified();
    updateQuantifiedEntity(quantified, request);

    return quantified;
  }

  public static void updateQuantifiedEntity(
      ItemQuantified quantified, QuantifiedItemRequest request) {
    if (quantified == null || request == null) {
      return;
    }

    quantified.setName(request.getName().trim());
    quantified.setQuantity(request.getQuantity());
    quantified.setBaseUnit(request.getBaseUnit());
    quantified.setOrigin(request.getOrigin());
    quantified.setRegenerationPolicy(request.getRegenerationPolicy());
    quantified.setGeneratorKey(normalizeNullableText(request.getGeneratorKey()));
  }

  public static QuantifiedItemResponse toQuantifiedResponse(ItemQuantified quantified) {
    if (quantified == null) {
      return null;
    }

    return QuantifiedItemResponse.builder()
        .name(quantified.getName())
        .quantity(quantified.getQuantity())
        .baseUnit(quantified.getBaseUnit())
        .origin(quantified.getOrigin())
        .regenerationPolicy(quantified.getRegenerationPolicy())
        .generatorKey(quantified.getGeneratorKey())
        .build();
  }

  /**
   * Update Request DTO で既存 Entity を更新（名前のみ） 完了/未完了の更新は Service の責務とするためここでは行わない なぜこの分離が重要か（設計的理由） -
   * Mapper：純粋変換 - Service：業務ルールと整合性 この分離をしておくと： - 後で status に拡張しても Mapper を触らずに済む -
   * Socket対応（完了イベント）も Service に閉じ込められる - テストが書きやすい（Mapperは単純、Serviceは業務テスト）
   */
  public static void updateEntity(Item entity, UpdateItemRequest request) {
    if (entity == null || request == null) {
      return;
    }

    // 名前の更新（nullでない場合のみ）
    if (request.getName() != null) {
      entity.setName(request.getName().trim());
    }
    if (request.isCategoryPresent()) {
      entity.setCategory(request.getCategory());
    }
  }

  private static String normalizeNullableText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
