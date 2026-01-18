package com.atoook.otsukailist.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Setter
@NoArgsConstructor
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

    private List<MemberResponse> members;
    private List<ItemResponse> items;
}

/**
 * snapshot組立イメージ
 * 
 * @Transactional(readOnly = true)
 *                         public ItemListSnapshotResponse snapshot(UUID listId)
 *                         {
 * 
 *                         ItemList list = itemListRepo.findById(listId)
 *                         .orElseThrow(() -> new NotFoundException("list not
 *                         found"));
 * 
 *                         List<MemberResponse> members =
 *                         memberRepo.findByItemListId(listId).stream()
 *                         .map(MemberMapper::toResponse)
 *                         .toList();
 * 
 *                         List<Item> itemEntities =
 *                         itemRepo.findByItemListId(listId);
 *                         List<ItemResponse> items = itemEntities.stream()
 *                         .map(ItemMapper::toResponse)
 *                         .toList();
 * 
 *                         return ItemListSnapshotResponse.builder()
 *                         .listId(list.getId())
 *                         .name(list.getName())
 *                         .revision(list.getRevision())
 *                         .itemCount(itemEntities.size()) // ← ここ
 *                         .serverTime(Instant.now())
 *                         .members(members)
 *                         .items(items)
 *                         .build();
 *                         }
 * 
 */
