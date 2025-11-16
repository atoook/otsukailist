package com.atoook.otsukailist.mapper;

import com.atoook.otsukailist.dto.CreateItemRequest;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.dto.UpdateItemRequest;
import com.atoook.otsukailist.model.Item;
import com.atoook.otsukailist.model.ShoppingList;

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
                .checked(entity.isChecked())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .listId(entity.getShoppingList() != null ? entity.getShoppingList().getId() : null)
                .build();
    }

    /**
     * Request DTO → Entity 変換（新規作成時）
     */
    public static Item toEntity(CreateItemRequest request, ShoppingList shoppingList) {
        if (request == null) {
            return null;
        }

        Item entity = new Item();
        entity.setName(request.getName());
        entity.setChecked(request.isChecked());
        entity.setShoppingList(shoppingList);

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

        // チェック状態の更新（nullでない場合のみ）
        if (request.getChecked() != null) {
            entity.setChecked(request.getChecked());
        }
    }
}
