package com.atoook.otsukailist.mapper;

import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.QuantifiedItemRequest;
import com.atoook.otsukailist.dto.QuantifiedItemResponse;
import com.atoook.otsukailist.model.Item;
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
        .version(entity.getVersion())
        .itemType(entity.getItemType() == null ? ItemType.PLAIN : entity.getItemType())
        .category(entity.getCategory())
        .preparationType(entity.getPreparationType())
        .quantified(toQuantifiedResponse(entity.getQuantified()))
        .completed(entity.isCompleted())
        .assignedMemberId(entity.getAssignedMemberId())
        .completedByMemberId(entity.getCompletedByMemberId())
        .completedAt(entity.getCompletedAt())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
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
        .quantity(quantified.getQuantity())
        .baseUnit(quantified.getBaseUnit())
        .origin(quantified.getOrigin())
        .regenerationPolicy(quantified.getRegenerationPolicy())
        .generatorKey(quantified.getGeneratorKey())
        .build();
  }

  private static String normalizeNullableText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
