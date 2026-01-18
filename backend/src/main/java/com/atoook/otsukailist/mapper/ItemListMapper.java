package com.atoook.otsukailist.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.atoook.otsukailist.dto.CreateItemListRequest;
import com.atoook.otsukailist.dto.ItemListResponse;
import com.atoook.otsukailist.dto.ItemResponse;
import com.atoook.otsukailist.model.ItemList;

/**
 * ItemList Entity ↔ DTO 変換用マッパー
 */
public class ItemListMapper {

    /**
     * Entity → Response DTO 変換
     * 
     * @param entity       ItemList Entity
     * @param includeItems アイテム詳細を含めるかどうか
     */
    public static ItemListResponse toResponse(ItemList entity, boolean includeItems) {
        if (entity == null) {
            return null;
        }

        ItemListResponse.ItemListResponseBuilder builder = ItemListResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .itemCount(entity.getItems() != null ? entity.getItems().size() : 0);

        // アイテムの詳細を含める場合
        if (includeItems && entity.getItems() != null) {
            List<ItemResponse> itemResponses = entity.getItems().stream()
                    .map(ItemMapper::toResponse)
                    .collect(Collectors.toList());
            builder.items(itemResponses);
        }

        return builder.build();
    }

    /**
     * Entity → Response DTO 変換（アイテム詳細なし）
     */
    public static ItemListResponse toResponse(ItemList entity) {
        return toResponse(entity, false);
    }

    /**
     * Request DTO → Entity 変換（新規作成時）
     */
    public static ItemList toEntity(CreateItemListRequest request) {
        if (request == null) {
            return null;
        }

        ItemList entity = new ItemList();
        entity.setName(request.getName());

        return entity;
    }

    /**
     * Request DTO で既存 Entity を更新
     */
    public static void updateEntity(ItemList entity, CreateItemListRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
    }
}
