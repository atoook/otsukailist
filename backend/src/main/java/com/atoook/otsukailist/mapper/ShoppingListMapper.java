package com.atoook.otsukailist.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.atoook.otsukailist.dto.CreateShoppingListRequest;
import com.atoook.otsukailist.dto.ShoppingListResponse;
import com.atoook.otsukailist.model.ShoppingList;

/**
 * ShoppingList Entity ↔ DTO 変換用マッパー
 */
public class ShoppingListMapper {

    /**
     * Entity → Response DTO 変換
     * 
     * @param entity       ShoppingList Entity
     * @param includeItems アイテム詳細を含めるかどうか
     */
    public static ShoppingListResponse toResponse(ShoppingList entity, boolean includeItems) {
        if (entity == null) {
            return null;
        }

        ShoppingListResponse.ShoppingListResponseBuilder builder = ShoppingListResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .itemCount(entity.getItems() != null ? entity.getItems().size() : 0);

        // アイテムの詳細を含める場合
        if (includeItems && entity.getItems() != null) {
            List<com.atoook.otsukailist.dto.ItemResponse> itemResponses = entity.getItems().stream()
                    .map(ItemMapper::toResponse)
                    .collect(Collectors.toList());
            builder.items(itemResponses);
        }

        return builder.build();
    }

    /**
     * Entity → Response DTO 変換（アイテム詳細なし）
     */
    public static ShoppingListResponse toResponse(ShoppingList entity) {
        return toResponse(entity, false);
    }

    /**
     * Request DTO → Entity 変換（新規作成時）
     */
    public static ShoppingList toEntity(CreateShoppingListRequest request) {
        if (request == null) {
            return null;
        }

        ShoppingList entity = new ShoppingList();
        entity.setName(request.getName());

        return entity;
    }

    /**
     * Request DTO で既存 Entity を更新
     */
    public static void updateEntity(ShoppingList entity, CreateShoppingListRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
    }
}
