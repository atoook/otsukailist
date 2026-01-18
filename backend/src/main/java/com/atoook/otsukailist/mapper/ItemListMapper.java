package com.atoook.otsukailist.mapper;

import com.atoook.otsukailist.dto.CreateItemListRequest;
import com.atoook.otsukailist.dto.ItemListResponse;
import com.atoook.otsukailist.model.ItemList;

import lombok.experimental.UtilityClass;

/**
 * ItemList Entity ↔ DTO 変換用マッパー
 */
@UtilityClass
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

        return ItemListResponse.builder()
                .id(entity.getId())
                .name(entity.getName().trim())
                .revision(entity.getRevision())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Request DTO → Entity 変換（新規作成時）
     */
    public static ItemList toEntity(CreateItemListRequest request) {
        if (request == null) {
            return null;
        }

        ItemList entity = new ItemList();
        entity.setName(request.getName().trim());
        // revision は DB default(0) / entity の初期値(0L) に任せる
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
            entity.setName(request.getName().trim());
        }
    }
}
