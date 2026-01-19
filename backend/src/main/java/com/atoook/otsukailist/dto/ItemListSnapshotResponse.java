package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Item List のSnapshotレスポンス用DTO
 * * <MEMO>
 * * このDTOは Service で
 * * listRepo.findById
 * * memberRepo.findByItemListId
 * * itemRepo.findByItemListId
 * * で集めて作る（Mapperは個別に ItemMapper/MemberMapper を使う）
 * * → N+1を踏まない。
 */
@Getter
@AllArgsConstructor
@Builder
public class ItemListSnapshotResponse {

    private UUID listId;
    private String name;

    /** Socket差分の基準（リスト単位） */
    private long revision;

    /** UI表示用 */
    private int itemCount;

    /** 任意：クライアントの基準時刻 */
    private Instant serverTime;

    @Builder.Default
    private List<MemberResponse> members = List.of();
    @Builder.Default
    private List<ItemResponse> items = List.of();
}
