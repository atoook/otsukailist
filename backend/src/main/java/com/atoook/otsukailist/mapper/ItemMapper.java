package com.atoook.otsukailist.mapper;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ItemList;

/**
 * Item Entity ↔ DTO 変換用マッパー
 */
public class ItemMapper {

    /**
     * Entity → Response DTO 変換
     */
    public static ItemResponse toResponse(Item entity) {
        if (entity == null) {
            return null;
        }

        return ItemResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .completed(entity.isCompleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .listId(entity.getItemList() != null ? entity.getItemList().getId() : null)
                .build();
    }

    /**
     * Request DTO → Entity 変換（新規作成時）
     */
    public static Item toEntity(CreateItemRequest request, ItemList itemList) {
        if (request == null) {
            return null;
        }

        Item entity = new Item();
        entity.setName(request.getName());
        entity.setCompleted(request.isCompleted());
        entity.setItemList(itemList);

        return entity;
    }

    /**
     * Update Request DTO で既存 Entity を更新
     */
    public static void updateEntity(Item entity, UpdateItemRequest request) {
        if (entity == null || request == null) {
            return;
        }

        // 名前の更新（nullでない場合のみ）
        if (request.getName() != null) {
            entity.setName(request.getName());
        }

        // 完了状態の更新（nullでない場合のみ）
        if (request.getCompleted() != null) {
            entity.setCompleted(request.getCompleted());
        }
    }
}
