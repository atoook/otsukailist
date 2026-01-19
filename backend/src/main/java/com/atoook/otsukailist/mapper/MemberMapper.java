package com.atoook.otsukailist.mapper;

import com.atoook.otsukailist.dto.CreateMemberRequest;
import com.atoook.otsukailist.dto.MemberResponse;
import com.atoook.otsukailist.model.ItemList;
import com.atoook.otsukailist.model.Member;

import lombok.experimental.UtilityClass;

/**
 * Member Entity ↔ DTO 変換用マッパー
 */
@UtilityClass
public class MemberMapper {

    public static MemberResponse toResponse(Member entity) {
        if (entity == null) {
            return null;
        }

        return MemberResponse.builder()
                .id(entity.getId())
                .displayName(entity.getDisplayName().trim())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 新規作成時：list は Service が確定させて渡す（N+1回避＆存在確認）
     */
    public static Member toEntity(CreateMemberRequest request, ItemList itemList) {
        if (request == null) {
            return null;
        }

        Member entity = new Member();
        entity.setDisplayName(request.getDisplayName().trim());
        entity.setItemList(itemList);
        return entity;
    }

    /**
     * 既存更新: displayName のみ
     */
    public static void updateEntity(Member entity, CreateMemberRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getDisplayName() != null) {
            entity.setDisplayName(request.getDisplayName().trim());
        }
    }
}
